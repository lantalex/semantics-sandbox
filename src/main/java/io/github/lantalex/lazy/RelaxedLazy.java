package io.github.lantalex.lazy;

import io.github.lantalex.MyObject;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.Supplier;

public class RelaxedLazy implements Lazy {

    private static final VarHandle INITIALIZER;

    static {
        try {
            INITIALIZER = MethodHandles.lookup().findVarHandle(RelaxedLazy.class, "initializer", Supplier.class);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("FieldMayBeFinal")
    private Supplier<MyObject> initializer;
    private MyObject value;

    public RelaxedLazy(Supplier<MyObject> initializer) {
        this.initializer = initializer;
    }

    @Override
    public MyObject get() {
        if (INITIALIZER.getAcquire(this) != null) {
            synchronized (this) {
                if (initializer != null) {
                    value = initializer.get();
                    INITIALIZER.setRelease(this, null);
                }
            }
        }
        return value;
    }
}

