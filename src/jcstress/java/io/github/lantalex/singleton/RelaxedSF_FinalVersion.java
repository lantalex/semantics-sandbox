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
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;


@JCStressTest
@Outcome(id = "-1", expect = FORBIDDEN, desc = "Single read, impossible")
@Outcome(id = {"0", "1", "2", "3"}, expect = FORBIDDEN, desc = "Violation of causality")
@Outcome(id = "4", expect = ACCEPTABLE, desc = "Expected result")
public class RelaxedSF_FinalVersion {

    /*

        How to run:
        $ ./gradlew clean jcstress --tests  "io.github.lantalex.singleton.RelaxedSF_FinalVersion"

        Results(arm_v8):
        ----------------------------------------------------------------
          RESULT     SAMPLES     FREQ      EXPECT  DESCRIPTION
              -1           0    0.00%   Forbidden  Single read, impossible
               0           0    0.00%   Forbidden  Violation of causality
               1           0    0.00%   Forbidden  Violation of causality
               2           0    0.00%   Forbidden  Violation of causality
               3           0    0.00%   Forbidden  Violation of causality
               4  84,945,048  100.00%  Acceptable  Expected result


    */

    @Actor
    public final void actor1(Singleton s) {
        s.getInstance();
    }

    @Actor
    public final void actor2(Singleton s, I_Result r) {
        MyObject instance = s.getInstance();

        if (instance == null) {
            r.r1 = -1;
        } else {
            r.r1 = instance.getSum();
        }

    }

    @State
    public static class Singleton {

        private static final VarHandle INSTANCE;

        static {
            try {
                INSTANCE = MethodHandles.lookup().findVarHandle(Singleton.class, "instance", MyObject.class);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }

        private MyObject instance;

        public MyObject getInstance() {
            MyObject p = (MyObject) INSTANCE.getAcquire(this);
            if (p == null) {
                synchronized (this) {
                    if (instance == null) {
                        INSTANCE.setRelease(this, new MyObject(1));
                    }
                    p = instance;
                }
            }
            return p;
        }
    }
}
