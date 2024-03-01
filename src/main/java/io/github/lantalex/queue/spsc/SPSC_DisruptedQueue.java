package io.github.lantalex.queue.spsc;

import io.github.lantalex.PaddedAtomicInteger;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SPSC_DisruptedQueue<E> {

    private final PaddedAtomicInteger consumerIdx = new PaddedAtomicInteger(0);
    private final PaddedAtomicInteger consumerIdxCached = new PaddedAtomicInteger(0);

    private final PaddedAtomicInteger producerIdx = new PaddedAtomicInteger(0);
    private final PaddedAtomicInteger producerIdxCached = new PaddedAtomicInteger(0);

    private final E[] buffer;

    public SPSC_DisruptedQueue(int capacity, Supplier<E> factory) {
        //noinspection unchecked
        this.buffer = (E[]) new Object[capacity + 1];
        Arrays.setAll(buffer, value -> factory.get());
    }

    public boolean tryProduce(Consumer<E> producer) {
        int pIdx = producerIdx.getPlain();
        int pNextIdx = next(pIdx);

        if (pNextIdx == consumerIdxCached.getPlain()) {
            consumerIdxCached.setPlain(consumerIdx.getAcquire());
            if (isFull(consumerIdxCached.getPlain(), pNextIdx)) {
                return false;
            }
        }

        //reuse existing object instead creating new
        producer.accept(buffer[pIdx]);

        producerIdx.setRelease(next(pIdx));
        return true;
    }

    public boolean tryConsume(Consumer<E> consumer) {
        int cIdx = consumerIdx.getPlain();

        if (cIdx == producerIdxCached.getPlain()) {
            producerIdxCached.setPlain(producerIdx.getAcquire());
            if (isEmpty(cIdx, producerIdxCached.getPlain())) {
                return false;
            }
        }

        consumer.accept(buffer[cIdx]);

        consumerIdx.setRelease(next(cIdx));
        return true;
    }

    public void clear() {
        Arrays.fill(buffer, null);
    }

    private int next(int idx) {
        return idx + 1 == buffer.length ? 0 : idx + 1;
    }

    private boolean isFull(int consumerIdx, int producerNextIdx) {
        return producerNextIdx == consumerIdx;
    }

    private boolean isEmpty(int consumerIdx, int producerIdx) {
        return producerIdx == consumerIdx;
    }
}
