package io.github.lantalex.queue.spsc;

import java.util.function.Consumer;

public interface SPSC_BoundedQueue<E> {

    boolean tryProduce(E value);

    boolean tryConsume(Consumer<E> consumer);

    void clear();
}
