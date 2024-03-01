package io.github.lantalex.lazy;

import io.github.lantalex.MyObject;

import java.util.function.Supplier;

@SuppressWarnings("DoubleCheckedLocking")
public class PlainLazy implements Lazy {

    private Supplier<MyObject> initializer;
    private MyObject value;

    public PlainLazy(Supplier<MyObject> initializer) {
        this.initializer = initializer;
    }

    @Override

    public MyObject get() {
        if (initializer != null) {
            synchronized (this) {
                if (initializer != null) {
                    value = initializer.get();
                    initializer = null;
                }
            }
        }
        return value;
    }
}

