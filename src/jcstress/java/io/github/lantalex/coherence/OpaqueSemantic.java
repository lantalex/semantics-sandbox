package io.github.lantalex.coherence;


import io.github.lantalex.MyObject;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

@JCStressTest
@Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "ref1 == null, ref2 == null")
@Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "ref1 != null, ref2 != null")
@Outcome(id = "0, 1", expect = ACCEPTABLE, desc = "ref1 == null, ref2 != null")
@Outcome(id = "1, 0", expect = FORBIDDEN, desc = "ref1 != null, ref2 == null; coherence violation")
@State
public class OpaqueSemantic {

    /*
        Original test: https://github.com/openjdk/jcstress/blob/master/jcstress-samples/src/main/java/org/openjdk/jcstress/samples/jmm/basic/BasicJMM_05_Coherence.java

        How to run:
        $ ./gradlew clean jcstress --tests  "io.github.lantalex.coherence.OpaqueSemantic"

        Results(x86):
        -----------------------------------------------------------------
          RESULT      SAMPLES     FREQ      EXPECT  DESCRIPTION
            0, 0  545,506,588   44.37%  Acceptable  ref1 == null, ref2 == null
            0, 1      582,185    0.05%  Acceptable  ref1 == null, ref2 != null
            1, 0            0    0.00%   Forbidden  ref1 != null, ref2 == null; coherence violation
            1, 1  683,485,371   55.59%  Acceptable  ref1 != null, ref2 != null


        Results(arm_v8):
        ----------------------------------------------------------------
          RESULT     SAMPLES     FREQ      EXPECT  DESCRIPTION
            0, 0     514,516    1.19%  Acceptable  ref1 == null, ref2 == null
            0, 1       9,158    0.02%  Acceptable  ref1 == null, ref2 != null
            1, 0           0    0.00%   Forbidden  ref1 != null, ref2 == null; coherence violation
            1, 1  42,715,750   98.79%  Acceptable  ref1 != null, ref2 != null


     */

    static final VarHandle VH;

    static {
        try {
            VH = MethodHandles.lookup().findVarHandle(Holder.class, "instance", MyObject.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private final Holder h1 = new Holder();
    private final Holder h2 = h1;

    @Actor
    public void actor1() {
        VH.setOpaque(h1, new MyObject(42));
    }

    @Actor
    public void actor2(II_Result r) {
        Holder h1 = this.h1;
        Holder h2 = this.h2;

        //Please read original test for explanation
        h1.trap = 0;
        h2.trap = 0;

        r.r1 = (MyObject) VH.getOpaque(h1) == null ? 0 : 1;
        r.r2 = (MyObject) VH.getOpaque(h2) == null ? 0 : 1;
    }

    private static class Holder {
        MyObject instance;
        int trap;
    }
}