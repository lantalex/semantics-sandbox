package io.github.lantalex;

import io.github.lantalex.lazy.PlainLazy;
import io.github.lantalex.lazy.RelaxedLazy;
import io.github.lantalex.lazy.VolatileLazy;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Fork(5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class LazyBenchmark {

     /*
            How to run:
            $ ./gradlew clean jmhJar
            $ java -jar build/libs/semantics-in-action-2024-0.1-jmh.jar LazyBenchmark

            Results(x86)
            --------------------------------------------------------------
              Benchmark                            Mode  Cnt   Score   Error  Units
              LazyBenchmark.continuousBrokenPlain  avgt   50  0.388 ± 0.001  ns/op
              LazyBenchmark.continuousRelaxed      avgt   50  0.389 ± 0.001  ns/op
              LazyBenchmark.continuousVolatile     avgt   50  0.416 ± 0.001  ns/op
              LazyBenchmark.firstCallBrokenPlain   avgt   50  2.491 ± 0.008  ns/op
              LazyBenchmark.firstCallRelaxed       avgt   50  2.490 ± 0.014  ns/op
              LazyBenchmark.firstCallVolatile      avgt   50  2.481 ± 0.012  ns/op

            Results(arm_v8)
            ---------------------------------------------------------------
              Benchmark                            Mode  Cnt   Score   Error  Units
              LazyBenchmark.continuousBrokenPlain  avgt   50   8.530 ± 0.004  ns/op
              LazyBenchmark.continuousRelaxed      avgt   50  11.764 ± 0.057  ns/op
              LazyBenchmark.continuousVolatile     avgt   50  11.746 ± 0.032  ns/op
              LazyBenchmark.firstCallBrokenPlain   avgt   50  39.913 ± 0.120  ns/op
              LazyBenchmark.firstCallRelaxed       avgt   50  37.682 ± 0.270  ns/op
              LazyBenchmark.firstCallVolatile      avgt   50  39.938 ± 0.176  ns/op


     */

    private PlainLazy plainLazy;
    private VolatileLazy volatileLazy;
    private RelaxedLazy relaxedLazy;

    @Setup
    public void setup() {
        plainLazy = new PlainLazy(() -> new MyObject(1));
        volatileLazy = new VolatileLazy(() -> new MyObject(2));
        relaxedLazy = new RelaxedLazy(() -> new MyObject(3));
    }

    @Benchmark
    public void firstCallBrokenPlain(Blackhole bh) {
        bh.consume(new PlainLazy(() -> new MyObject(1)).get());
    }

    @Benchmark
    public void firstCallVolatile(Blackhole bh) {
        bh.consume(new VolatileLazy(() -> new MyObject(2)).get());
    }

    @Benchmark
    public void firstCallRelaxed(Blackhole bh) {
        bh.consume(new RelaxedLazy(() -> new MyObject(3)).get());
    }

    @Benchmark
    public void continuousBrokenPlain(Blackhole bh) {
        bh.consume(plainLazy.get());
    }

    @Benchmark
    public void continuousVolatile(Blackhole bh) {
        bh.consume(volatileLazy.get());
    }

    @Benchmark
    public void continuousRelaxed(Blackhole bh) {
        bh.consume(relaxedLazy.get());
    }

}
