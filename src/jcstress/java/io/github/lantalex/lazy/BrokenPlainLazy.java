package io.github.lantalex.lazy;

import io.github.lantalex.MyObject;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;


@JCStressTest
@Outcome(id = "-1", expect = ACCEPTABLE_INTERESTING, desc = "NPE")
@Outcome(id = {"0", "1", "2", "3"}, expect = ACCEPTABLE_INTERESTING, desc = "Problem with causality")
@Outcome(id = "4", expect = ACCEPTABLE, desc = "Expected result")
@State
public class BrokenPlainLazy {

    /*

        How to run:
        $ ./gradlew clean jcstress --tests  "io.github.lantalex.lazy.BrokenPlainLazy"

        Results(arm_v8):
        ----------------------------------------------------------------
          RESULT     SAMPLES     FREQ       EXPECT  DESCRIPTION
              -1      44,560    0.06%  Interesting  NPE
               0         149   <0.01%  Interesting  Problem with causality
               1           4   <0.01%  Interesting  Problem with causality
               2          43   <0.01%  Interesting  Problem with causality
               3           3   <0.01%  Interesting  Problem with causality
               4  78,070,209   99.94%   Acceptable  Expected result


     */

    PlainLazy lazy = new PlainLazy(() -> new MyObject(1));

    @Actor
    public final void actor1() {
        lazy.get();
    }

    @Actor
    public final void actor2(I_Result r) {
        MyObject instance = lazy.get();

        if (instance == null) {
            r.r1 = -1;
        } else {
            r.r1 = instance.getSum();
        }
    }
}
