package io.github.lantalex;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.Consumer;

public class MyObjectVersioned extends MyObject {

    private static final VarHandle VERSION;

    private static final VarHandle A;
    private static final VarHandle B;
    private static final VarHandle C;
    private static final VarHandle D;

    static {
        try {
            VERSION = MethodHandles.lookup().findVarHandle(MyObjectVersioned.class, "version", long.class);
            A = MethodHandles.lookup().findVarHandle(MyObjectVersioned.class, "a", int.class);
            B = MethodHandles.lookup().findVarHandle(MyObjectVersioned.class, "b", int.class);
            C = MethodHandles.lookup().findVarHandle(MyObjectVersioned.class, "c", int.class);
            D = MethodHandles.lookup().findVarHandle(MyObjectVersioned.class, "d", int.class);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    long version;

    public MyObjectVersioned(int value) {
        super(value);
    }

    public boolean tryRead(MyObject into) {
        final long beforeCopyVersion = (long) VERSION.getAcquire(this);
        if ((beforeCopyVersion & 1) == 1) {
            return false;
        }

        into.a = (int) A.getOpaque(this);
        into.b = (int) B.getOpaque(this);
        into.c = (int) C.getOpaque(this);
        into.d = (int) D.getOpaque(this);

        VarHandle.fullFence();

        final long afterCopyVersion = (long) VERSION.getOpaque(this);
        return afterCopyVersion == beforeCopyVersion;
    }

    public void update(MyObject from) {
        final long oldVersion = (long) VERSION.getOpaque(this);
        VERSION.setOpaque(this, oldVersion + 1);

        VarHandle.fullFence();

        A.setOpaque(this, from.a);
        B.setOpaque(this, from.b);
        C.setOpaque(this, from.c);
        D.setOpaque(this, from.d);

        VERSION.setRelease(this, oldVersion + 2);
    }
}
