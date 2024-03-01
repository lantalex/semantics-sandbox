package io.github.lantalex.coherence;


import io.github.lantalex.MyObject;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;

@JCStressTest
@Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "ref1 == null, ref2 == null")
@Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "ref1 != null, ref2 != null")
@Outcome(id = "0, 1", expect = ACCEPTABLE, desc = "ref1 == null, ref2 != null")
@Outcome(id = "1, 0", expect = ACCEPTABLE_INTERESTING, desc = "ref1 != null, ref2 == null")
@State
public class PlainSemantic {

    /*
        Original test: https://github.com/openjdk/jcstress/blob/master/jcstress-samples/src/main/java/org/openjdk/jcstress/samples/jmm/basic/BasicJMM_05_Coherence.java

        How to run:
        $ ./gradlew clean jcstress --tests  "io.github.lantalex.coherence.PlainSemantic"

        Results(x86):
        -----------------------------------------------------------------
          RESULT      SAMPLES     FREQ       EXPECT  DESCRIPTION
            0, 0  407,881,645   33.27%   Acceptable  ref1 == null, ref2 == null
            0, 1      262,815    0.02%   Acceptable  ref1 == null, ref2 != null
            1, 0      242,070    0.02%  Interesting  ref1 != null, ref2 == null
            1, 1  817,419,294   66.68%   Acceptable  ref1 != null, ref2 != null


        Results(arm_v8):
        ----------------------------------------------------------------
          RESULT     SAMPLES     FREQ       EXPECT  DESCRIPTION
            0, 0     676,471    1.56%   Acceptable  ref1 == null, ref2 == null
            0, 1       2,261   <0.01%   Acceptable  ref1 == null, ref2 != null
            1, 0       2,659   <0.01%  Interesting  ref1 != null, ref2 == null
            1, 1  42,732,113   98.43%   Acceptable  ref1 != null, ref2 != null


     */

    private final Holder h1 = new Holder();
    private final Holder h2 = h1;

    @Actor
    public void actor1() {
        h1.instance = new MyObject(42);
    }

    @Actor
    public void actor2(II_Result r) {
        Holder ref1 = this.h1;
        Holder ref2 = this.h2;

        //Please read original test for explanation
        ref1.trap = 0;
        ref2.trap = 0;

        r.r1 = ref1.instance == null ? 0 : 1;
        r.r2 = ref2.instance == null ? 0 : 1;
    }

    private static class Holder {
        MyObject instance;
        int trap;
    }
}