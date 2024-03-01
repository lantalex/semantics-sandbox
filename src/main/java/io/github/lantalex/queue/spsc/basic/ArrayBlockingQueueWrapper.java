package io.github.lantalex.queue.spsc.basic;

import io.github.lantalex.queue.spsc.SPSC_BoundedQueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class ArrayBlockingQueueWrapper<E> implements SPSC_BoundedQueue<E> {

    private final BlockingQueue<E> queue;

    public ArrayBlockingQueueWrapper(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public boolean tryProduce(E value) {
        return queue.offer(value);
    }

    @Override
    public boolean tryConsume(Consumer<E> consumer) {
        E element = queue.poll();
        if (element != null) {
            consumer.accept(element);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        queue.clear();
    }
}
