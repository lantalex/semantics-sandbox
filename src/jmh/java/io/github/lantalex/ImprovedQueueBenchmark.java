package io.github.lantalex;

import io.github.lantalex.queue.spsc.basic.SPSC_AcqRelQueue;
import io.github.lantalex.queue.spsc.SPSC_BoundedQueue;
import io.github.lantalex.queue.spsc.improved.SPSC_ImprovedQueue;
import io.github.lantalex.queue.spsc.improved.SPSC_ImprovedVolatileQueue;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
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
@Measurement(iterations = 20, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
public class ImprovedQueueBenchmark {

        /*
            How to run:
            $ ./gradlew clean jmhJar
            $ java -jar build/libs/semantics-in-action-2024-0.1-jmh.jar ImprovedQueueBenchmark

            Results(x86), throughput:
            --------------------------------------------------------------
             SPSC_AcqRelQueue
             consume                          39393.494 ±  1563.209  ops/ms
             produce                          39476.691 ±  1575.767  ops/ms

             SPSC_ImprovedQueue
             consume                         165217.269 ±  7575.185  ops/ms
             produce                         165162.172 ±  7672.950  ops/ms

             SPSC_ImprovedQueueVolatile
             consume                          48678.157 ±  1569.728  ops/ms
             produce                          47456.517 ±  1653.379  ops/ms

            Results(arm_v8), throughput:
            ---------------------------------------------------------------
             SPSC_AcqRelQueue
             consume                           16314.753 ±  350.793  ops/ms
             produce                           16313.430 ±  347.284  ops/ms

             SPSC_ImprovedQueue
             consume                           24184.212 ±  160.240  ops/ms
             produce                           24191.916 ±  160.015  ops/ms

             SPSC_ImprovedQueueVolatile
             consume                           12179.180 ±  526.045  ops/ms
             produce                           12192.965 ±  530.242  ops/ms
     */

    @Benchmark
    @Group()
    @GroupThreads()
    public void produce(BenchmarkState state, ProducerCounters counters) {
        if (!state.queue.tryProduce(state.element)) {
            counters.tryProduceFailed++;
            Thread.yield();
        } else {
            counters.tryProduceSucceed++;
            Blackhole.consumeCPU(state.producerTokens);
        }
    }

    @Benchmark
    @Group()
    @GroupThreads()
    public void consume(BenchmarkState state, ConsumerCounters counters) {
        if (!state.queue.tryConsume(state.consumerLogic)) {
            counters.tryConsumeFailed++;
            Thread.yield();
        } else {
            counters.tryConsumeSucceed++;
        }
    }

    @State(Scope.Group)
    public static class BenchmarkState {

        @Param(value = {"100000"})
        private int queueCapacity;

        @Param(value = {"5"})
        private int consumerTokens;

        @Param(value = {"5"})
        private int producerTokens;

        @Param(value = {"SPSC_ImprovedQueue", "SPSC_AcqRelQueue", "SPSC_ImprovedVolatileQueue"})
        private String implementation;

        private SPSC_BoundedQueue<String> queue;
        private Consumer<String> consumerLogic;
        private String element;

        @Setup(Level.Trial)
        public void setup(final Blackhole bh) {
            queue = switch (implementation) {
                case "SPSC_ImprovedQueue" -> new SPSC_ImprovedQueue<>(queueCapacity);
                case "SPSC_AcqRelQueue" -> new SPSC_AcqRelQueue<>(queueCapacity);
                case "SPSC_ImprovedVolatileQueue" -> new SPSC_ImprovedVolatileQueue<>(queueCapacity);
                default -> throw new IllegalArgumentException("Unsupported queue: " + implementation);
            };

            consumerLogic = (element) -> {
                Blackhole.consumeCPU(consumerTokens);
                bh.consume(element);
            };

            element = String.valueOf(System.nanoTime());
        }

        @TearDown(Level.Iteration)
        public synchronized void tearDown() {
            queue.clear();
        }
    }

    @AuxCounters
    @State(Scope.Thread)
    public static class ConsumerCounters {
        public long tryConsumeFailed;
        public long tryConsumeSucceed;
    }

    @AuxCounters
    @State(Scope.Thread)
    public static class ProducerCounters {
        public long tryProduceFailed;
        public long tryProduceSucceed;
    }
}
