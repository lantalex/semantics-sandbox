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

@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 30, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(1)
public class DisruptorIdleBenchmark {

    /*
            How to run:
            $ ./gradlew clean jmhJar
            $ java -jar build/libs/semantics-in-action-2024-0.1-jmh.jar DisruptorIdleBenchmark

            Results(x86), throughput:
            --------------------------------------------------------------
            DisruptorIdleBenchmark.produce      1048576  thrpt   30  45.390 ± 0.224  ops/us


    */

    @Param(value = {"1048576"})
    private int queueSize;

    private RingBuffer<MyObject> ringBuffer;
    private Disruptor<MyObject> disruptor;


    @Setup
    public void setup(Blackhole bh) {

        disruptor = new Disruptor<>(
                () -> new MyObject(0),
                queueSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new BusySpinWaitStrategy());

        disruptor.handleEventsWith((event, sequence, endOfBatch) -> bh.consume(event));

        ringBuffer = disruptor.start();
    }

    @Benchmark
    public void produce() {
        long sequence = ringBuffer.next();
        MyObject object = ringBuffer.get(sequence);

        object.a = 1;
        object.b = 2;
        object.c = 3;
        object.d = 4;

        ringBuffer.publish(sequence);
    }

    @TearDown
    public void tearDown() {
        disruptor.shutdown();
    }
}
