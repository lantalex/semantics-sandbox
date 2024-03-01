package io.github.lantalex;

import io.github.lantalex.queue.spsc.SPSC_DisruptedQueue;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 20, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class SPSC_DisruptedQueue_Benchmark {

    /*
            How to run:
            $ ./gradlew clean jmhJar
            $ java -jar build/libs/semantics-in-action-2024-0.1-jmh.jar SPSC_DisruptedQueue_Benchmark

            Results(x86), throughput:
            --------------------------------------------------------------
             consume                          190000.933 ± 1222.272  ops/ms
             produce                          190000.978 ± 1558.687  ops/ms

            Results(arm_v8), throughput:
            ---------------------------------------------------------------
             consume                           20669.726 ±  615.239  ops/ms
             produce                           20666.737 ±  614.877  ops/ms

    */

    @Param(value = {"1048575"})
    private int queueSize;

    @Param(value = {"5"})
    private int consumerTokens;

    @Param(value = {"5"})
    private int producerTokens;

    private SPSC_DisruptedQueue<MyObject> queue;

    private Consumer<MyObject> producerLogic;
    private Consumer<MyObject> consumerLogic;

    @Benchmark
    @Group()
    @GroupThreads()
    public void produce() {
        while (!queue.tryProduce(producerLogic)) {
            Thread.onSpinWait();
        }
    }

    @Benchmark
    @Group()
    @GroupThreads()
    public void consume() {
        while (!queue.tryConsume(consumerLogic)) {
            Thread.onSpinWait();
        }
    }

    @Setup
    public void setup(final Blackhole bh) {

        consumerLogic = (element) -> {
            Blackhole.consumeCPU(consumerTokens);
            bh.consume(element);
        };

        producerLogic = (element) -> {
            Blackhole.consumeCPU(producerTokens);
            bh.consume(element);
        };

        queue = new SPSC_DisruptedQueue<>(queueSize, () -> new MyObject(0));

    }
}
