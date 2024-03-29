package io.github.lantalex;

import io.github.lantalex.queue.spsc.basic.ArrayBlockingQueueWrapper;
import io.github.lantalex.queue.spsc.basic.SPSC_AcqRelQueue;
import io.github.lantalex.queue.spsc.SPSC_BoundedQueue;
import io.github.lantalex.queue.spsc.basic.SPSC_VolatileQueue;
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
public class BasicQueueBenchmark {

        /*
            How to run:
            $ ./gradlew clean jmhJar
            $ java -jar build/libs/semantics-in-action-2024-0.1-jmh.jar BasicQueueBenchmark

            Results(x86), throughput:
            --------------------------------------------------------------
             SPSC_AcqRelQueue
             consume                          37472.958 ± 2069.287  ops/ms
             produce                          37575.527 ± 2086.828  ops/ms

             SPSC_VolatileQueue
             consume                          17002.478 ±  447.400  ops/ms
             produce                          17080.611 ±  494.639  ops/ms

             ArrayBlockingQueueWrapper
             consume                           6178.488 ±  451.600  ops/ms
             produce                           6151.243 ±  455.382  ops/ms


            Results(arm_v8), throughput:
            ---------------------------------------------------------------
             SPSC_AcqRelQueue
             consume                           16254.709 ± 135.621  ops/ms
             produce                           16254.621 ± 135.592  ops/ms

             SPSC_VolatileQueue
             consume                           11071.364 ±  62.709  ops/ms
             produce                           11073.434 ±  61.850  ops/ms

             ArrayBlockingQueueWrapper
             consume                            2951.280 ±  67.844  ops/ms
             produce                            2947.686 ±  63.394  ops/ms


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

        @Param(value = {"1023"})
        private int queueCapacity;

        @Param(value = {"5"})
        private int consumerTokens;

        @Param(value = {"5"})
        private int producerTokens;

        @Param(value = {"SPSC_VolatileQueue", "SPSC_AcqRelQueue", "ArrayBlockingQueueWrapper"})
        private String implementation;

        private SPSC_BoundedQueue<String> queue;
        private Consumer<String> consumerLogic;
        private String element;

        @Setup(Level.Trial)
        public void setup(final Blackhole bh) {
            queue = switch (implementation) {
                case "SPSC_VolatileQueue" -> new SPSC_VolatileQueue<>(queueCapacity);
                case "SPSC_AcqRelQueue" -> new SPSC_AcqRelQueue<>(queueCapacity);
                case "ArrayBlockingQueueWrapper" -> new ArrayBlockingQueueWrapper<>(queueCapacity);
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
