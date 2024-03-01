package io.github.lantalex;

import io.github.lantalex.queue.spsc.seqlock.StampedQueueForMyObject;
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

@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 20, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class StampedQueueIdleBenchmark {

    /*
            How to run:
            $ ./gradlew clean jmhJar
            $ java -jar build/libs/semantics-in-action-2024-0.1-jmh.jar StampedQueueIdleBenchmark

            Results(x86), throughput:
            --------------------------------------------------------------
            Benchmark                                (queueSize)   Mode  Cnt    Score    Error   Units
            StampedQueueIdleBenchmark.group:consume      1048575  thrpt  100  197.111 ± 43.286  ops/us
            StampedQueueIdleBenchmark.group:produce      1048575  thrpt  100  220.734 ± 39.598  ops/us


    */

    @Param(value = {"1048575"})
    private int queueSize;

    private StampedQueueForMyObject queue;

    private MyObject element;

    private MyObject tmp;

    @Benchmark
    @Group()
    @GroupThreads()
    public void produce() {
        queue.produce(element);
    }

    @Benchmark
    @Group()
    @GroupThreads()
    public void consume(Blackhole bh) {
        queue.consume((e) -> bh.consume(e), tmp);
    }

    @Setup
    public void setup(final Blackhole bh) {
        queue = new StampedQueueForMyObject(queueSize);

        element = new MyObject(1);
        element.b = 2;
        element.c = 3;
        element.d = 4;

        tmp = new MyObject(0);
    }
}
