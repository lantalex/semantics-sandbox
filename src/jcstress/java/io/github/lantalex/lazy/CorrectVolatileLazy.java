package io.github.lantalex.lazy;

import io.github.lantalex.MyObject;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;


@JCStressTest
@Outcome(id = "-1", expect = FORBIDDEN, desc = "NPE is forbidden")
@Outcome(id = {"0", "1", "2", "3"}, expect = FORBIDDEN, desc = "Violation of causality")
@Outcome(id = "4", expect = ACCEPTABLE, desc = "Expected result")
@State
public class CorrectVolatileLazy {

    /*

        How to run:
        $ ./gradlew clean jcstress --tests  "io.github.lantalex.lazy.CorrectVolatileLazy"

        Results(arm_v8):
        ----------------------------------------------------------------
          RESULT     SAMPLES     FREQ      EXPECT  DESCRIPTION
              -1           0    0.00%   Forbidden  NPE is forbidden
               0           0    0.00%   Forbidden  Violation of causality
               1           0    0.00%   Forbidden  Violation of causality
               2           0    0.00%   Forbidden  Violation of causality
               3           0    0.00%   Forbidden  Violation of causality
               4  76,855,448  100.00%  Acceptable  Expected result


     */

    VolatileLazy lazy = new VolatileLazy(() -> new MyObject(1));

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
