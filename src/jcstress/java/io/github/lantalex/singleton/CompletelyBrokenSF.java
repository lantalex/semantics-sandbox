package io.github.lantalex.singleton;

import io.github.lantalex.MyObject;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;


@JCStressTest
@Outcome(id = "-1", expect = ACCEPTABLE_INTERESTING, desc = "Problem with coherence")
@Outcome(id = {"0", "1", "2", "3"}, expect = ACCEPTABLE_INTERESTING, desc = "Problem with causality")
@Outcome(id = "4", expect = ACCEPTABLE, desc = "Expected result")
public class CompletelyBrokenSF {

    /*

        How to run:
        $ ./gradlew clean jcstress --tests  "io.github.lantalex.singleton.CompletelyBrokenSF"

        Results(arm_v8):
        ----------------------------------------------------------------
          RESULT     SAMPLES     FREQ       EXPECT  DESCRIPTION
              -1           0    0.00%  Interesting  Problem with coherence
               0         289   <0.01%  Interesting  Problem with causality
               1           3   <0.01%  Interesting  Problem with causality
               2          18   <0.01%  Interesting  Problem with causality
               3           1   <0.01%  Interesting  Problem with causality
               4  97,376,098  100.00%   Acceptable  Expected result

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

    @SuppressWarnings("DoubleCheckedLocking")
    @State
    public static class SingletonFactory {
        private MyObject instance;

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
