package io.github.lantalex.queue.spsc.improved;

import io.github.lantalex.PaddedAtomicInteger;
import io.github.lantalex.queue.spsc.SPSC_BoundedQueue;

import java.util.Arrays;
import java.util.function.Consumer;

public class SPSC_ImprovedQueue<E> implements SPSC_BoundedQueue<E> {

    private final PaddedAtomicInteger consumerIdx = new PaddedAtomicInteger(0);
    private final PaddedAtomicInteger consumerIdxCached = new PaddedAtomicInteger(0);

    private final PaddedAtomicInteger producerIdx = new PaddedAtomicInteger(0);
    private final PaddedAtomicInteger producerIdxCached = new PaddedAtomicInteger(0);

    private final E[] buffer;

    public SPSC_ImprovedQueue(int capacity) {
        //noinspection unchecked
        this.buffer = (E[]) new Object[capacity + 1];
    }

    @Override
    public boolean tryProduce(E value) {
        int pIdx = producerIdx.getPlain();
        int pNextIdx = next(pIdx);

        if (pNextIdx == consumerIdxCached.getPlain()) {
            consumerIdxCached.setPlain(consumerIdx.getAcquire());
            if (isFull(consumerIdxCached.getPlain(), pNextIdx)) {
                return false;
            }
        }

        buffer[pIdx] = value;

        producerIdx.setRelease(next(pIdx));
        return true;
    }

    @Override
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

    @Override
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
