package io.github.lantalex.singleton;

import io.github.lantalex.MyObject;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;


@JCStressTest
@Outcome(id = "-1", expect = FORBIDDEN, desc = "Violation of coherence")
@Outcome(id = {"0", "1", "2", "3"}, expect = FORBIDDEN, desc = "Violation of causality")
@Outcome(id = "4", expect = ACCEPTABLE, desc = "Expected result")
public class VolatileSF {

    /*

        How to run:
        $ ./gradlew clean jcstress --tests  "io.github.lantalex.singleton.VolatileSF"

        Results(arm_v8):
        ----------------------------------------------------------------
          RESULT     SAMPLES     FREQ      EXPECT  DESCRIPTION
              -1           0    0.00%   Forbidden  Violation of coherence
               0           0    0.00%   Forbidden  Violation of causality
               1           0    0.00%   Forbidden  Violation of causality
               2           0    0.00%   Forbidden  Violation of causality
               3           0    0.00%   Forbidden  Violation of causality
               4  93,239,448  100.00%  Acceptable  Expected result


     */

    @Actor
    public final void actor1(SingletonFactory s) {
        s.getInstance();
    }

    @Actor
    public final void actor2(SingletonFactory s, I_Result r) {
        MyObject instance = s.getInstance();

        if (instance == null) {
            r.r1 = -1;
        } else {
            r.r1 = instance.getSum();
        }

    }

    @State
    public static class SingletonFactory {
        private volatile MyObject instance;

        public MyObject getInstance() {
            if (instance == null) {
                synchronized (this) {
                    if (instance == null)
                        instance = new MyObject(1);
                }
            }
            return instance;
        }
    }
}
