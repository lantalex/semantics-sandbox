package io.github.lantalex;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 30, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(1)
public class DisruptorBenchmark {

    /*
            How to run:
            $ ./gradlew clean jmhJar
            $ java -jar build/libs/semantics-in-action-2024-0.1-jmh.jar DisruptorBenchmark

            Results(x86), throughput:
            --------------------------------------------------------------
             produce                          183308.914 ± 1558.687  ops/ms

            Results(arm_v8), throughput:
            ---------------------------------------------------------------
             produce                            20830.169 ± 467.517  ops/ms

    */

    @Param(value = {"1048576"})
    private int queueSize;

    @Param(value = {"0", "5"})
    private int consumerTokens;

    @Param(value = {"0", "5"})
    private int producerTokens;

    private RingBuffer<MyObject> ringBuffer;
    private Disruptor<MyObject> disruptor;

    private Consumer<MyObject> producerLogic;
    private Consumer<MyObject> consumerLogic;

    @Setup
    public void setup(Blackhole bh) {

        consumerLogic = (element) -> {
            Blackhole.consumeCPU(consumerTokens);
            bh.consume(element);
        };

        producerLogic = (element) -> {
            Blackhole.consumeCPU(producerTokens);
            bh.consume(element);
        };

        disruptor = new Disruptor<>(
                () -> new MyObject(0),
                queueSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new BusySpinWaitStrategy());

        disruptor.handleEventsWith((event, sequence, endOfBatch) -> consumerLogic.accept(event));

        ringBuffer = disruptor.start();
    }

    @Benchmark
    public void produce() {
        long sequence = ringBuffer.next();
        MyObject object = ringBuffer.get(sequence);
        producerLogic.accept(object);
        ringBuffer.publish(sequence);
    }

    @TearDown
    public void tearDown() {
        disruptor.shutdown();
    }
}
