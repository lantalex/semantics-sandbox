package io.github.lantalex.seqlock;

import io.github.lantalex.MyObject;
import io.github.lantalex.MyObjectVersioned;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.IIIII_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;


@JCStressTest
@Outcome(id = "0, .*", expect = ACCEPTABLE, desc = "Failed to read optimistically")
@Outcome(id = "1, 1, 2, 3, 4", expect = ACCEPTABLE_INTERESTING, desc = "Success read after write")
@Outcome(id = "1, 6, 6, 6, 6", expect = ACCEPTABLE, desc = "Success read before write")
@State
public class SimpleSeqlockTest {

    /*

        How to run:
        $ ./gradlew clean jcstress --tests  "io.github.lantalex.seqlock.SimpleSeqlockTest"

        Results(x86):
        ----------------------------------------------------------------
          RESULT      SAMPLES     FREQ       EXPECT  DESCRIPTION
            0, 1, 2, 3, 4    1,325,830    0.14%   Acceptable  Failed to read optimistically
            0, 1, 2, 3, 6        1,195   <0.01%   Acceptable  Failed to read optimistically
            0, 1, 2, 6, 4        2,540   <0.01%   Acceptable  Failed to read optimistically
            0, 1, 2, 6, 6        9,836   <0.01%   Acceptable  Failed to read optimistically
            0, 1, 6, 3, 4           84   <0.01%   Acceptable  Failed to read optimistically
            0, 1, 6, 3, 6          124   <0.01%   Acceptable  Failed to read optimistically
            0, 1, 6, 6, 4           35   <0.01%   Acceptable  Failed to read optimistically
            0, 1, 6, 6, 6        1,494   <0.01%   Acceptable  Failed to read optimistically
            0, 6, 2, 3, 4      209,914    0.02%   Acceptable  Failed to read optimistically
            0, 6, 2, 3, 6        2,930   <0.01%   Acceptable  Failed to read optimistically
            0, 6, 2, 6, 4          183   <0.01%   Acceptable  Failed to read optimistically
            0, 6, 2, 6, 6        1,329   <0.01%   Acceptable  Failed to read optimistically
            0, 6, 6, 3, 4       39,745   <0.01%   Acceptable  Failed to read optimistically
            0, 6, 6, 3, 6          841   <0.01%   Acceptable  Failed to read optimistically
            0, 6, 6, 6, 4      277,259    0.03%   Acceptable  Failed to read optimistically
            0, 6, 6, 6, 6      763,964    0.08%   Acceptable  Failed to read optimistically
            0, 9, 9, 9, 9    4,202,773    0.43%   Acceptable  Failed to read optimistically
            1, 1, 2, 3, 4  753,477,130   77.45%  Interesting  Success read after write
            1, 6, 6, 6, 6  212,507,522   21.84%   Acceptable  Success read before write


        Results(arm_v8):
        ----------------------------------------------------------------
          RESULT     SAMPLES     FREQ       EXPECT  DESCRIPTION
            0, 1, 2, 3, 4     102,529    0.37%   Acceptable  Failed to read optimistically
            0, 1, 2, 3, 6       1,966   <0.01%   Acceptable  Failed to read optimistically
            0, 1, 2, 6, 4       1,107   <0.01%   Acceptable  Failed to read optimistically
            0, 1, 2, 6, 6       1,309   <0.01%   Acceptable  Failed to read optimistically
            0, 1, 6, 3, 4           8   <0.01%   Acceptable  Failed to read optimistically
            0, 1, 6, 3, 6          39   <0.01%   Acceptable  Failed to read optimistically
            0, 1, 6, 6, 4         313   <0.01%   Acceptable  Failed to read optimistically
            0, 1, 6, 6, 6       2,397   <0.01%   Acceptable  Failed to read optimistically
            0, 6, 2, 3, 4         116   <0.01%   Acceptable  Failed to read optimistically
            0, 6, 2, 3, 6          17   <0.01%   Acceptable  Failed to read optimistically
            0, 6, 2, 6, 6          27   <0.01%   Acceptable  Failed to read optimistically
            0, 6, 6, 3, 4         230   <0.01%   Acceptable  Failed to read optimistically
            0, 6, 6, 3, 6           3   <0.01%   Acceptable  Failed to read optimistically
            0, 6, 6, 6, 4         137   <0.01%   Acceptable  Failed to read optimistically
            0, 6, 6, 6, 6      31,857    0.11%   Acceptable  Failed to read optimistically
            0, 9, 9, 9, 9      65,354    0.23%   Acceptable  Failed to read optimistically
            1, 1, 2, 3, 4  26,707,821   95.80%  Interesting  Success read after write
            1, 6, 6, 6, 6     962,298    3.45%   Acceptable  Success read before write


     */

    MyObjectVersioned sharedObject = new MyObjectVersioned(6);

    @Actor
    public final void writer() {

        MyObject from = new MyObject(0);
        from.a = 1;
        from.b = 2;
        from.c = 3;
        from.d = 4;

        sharedObject.update(from);
    }

    @Actor
    public final void reader(IIIII_Result r) {
        MyObject tmp = new MyObject(9);

        r.r1 = sharedObject.tryRead(tmp) ? 1 : 0;

        r.r2 = tmp.a;
        r.r3 = tmp.b;
        r.r4 = tmp.c;
        r.r5 = tmp.d;
    }
}
