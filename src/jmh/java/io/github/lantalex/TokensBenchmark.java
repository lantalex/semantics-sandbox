package io.github.lantalex;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(1)
public class TokensBenchmark {

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

    @Param(value = {"0", "5", "10", "50", "100"})
    private int tokens;

    @Benchmark
    public void produce() {
        Blackhole.consumeCPU(tokens);
    }
}
