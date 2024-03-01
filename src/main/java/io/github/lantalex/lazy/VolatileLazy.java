package io.github.lantalex.lazy;

import io.github.lantalex.MyObject;

import java.util.function.Supplier;

public class VolatileLazy implements Lazy {

    private volatile Supplier<MyObject> initializer;
    private MyObject value;

    public VolatileLazy(Supplier<MyObject> initializer) {
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

