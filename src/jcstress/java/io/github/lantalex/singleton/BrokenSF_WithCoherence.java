package io.github.lantalex.singleton;

import io.github.lantalex.MyObject;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;


@JCStressTest
@Outcome(id = "-1", expect = FORBIDDEN, desc = "Violation of coherence")
@Outcome(id = {"0", "1", "2", "3"}, expect = ACCEPTABLE_INTERESTING, desc = "Problem with causality")
@Outcome(id = "4", expect = ACCEPTABLE, desc = "Expected result")
public class BrokenSF_WithCoherence {

    /*

        How to run:
        $ ./gradlew clean jcstress --tests  "io.github.lantalex.singleton.BrokenSF_WithCoherence"

        Results(arm_v8):
        ----------------------------------------------------------------
          RESULT     SAMPLES     FREQ       EXPECT  DESCRIPTION
              -1           0    0.00%    Forbidden  Violation of coherence
               0          31   <0.01%  Interesting  Problem with causality
               1           1   <0.01%  Interesting  Problem with causality
               2           9   <0.01%  Interesting  Problem with causality
               3           1   <0.01%  Interesting  Problem with causality
               4  94,990,446  100.00%   Acceptable  Expected result


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

        private static final VarHandle INSTANCE;

        static {
            try {
                INSTANCE = MethodHandles.lookup().findVarHandle(SingletonFactory.class, "instance", MyObject.class);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }

        private MyObject instance;

        public MyObject getInstance() {
            if (INSTANCE.getOpaque(this) == null) {
                synchronized (this) {
                    if (instance == null)
                        instance = new MyObject(1);
                }
            }
            return (MyObject) INSTANCE.getOpaque(this);
        }
    }
}
