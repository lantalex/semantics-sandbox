package io.github.lantalex.queue.spsc.seqlock;

import io.github.lantalex.MyObject;
import io.github.lantalex.PaddedAtomicInteger;
import io.github.lantalex.PaddedAtomicLong;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.Consumer;

public class StampedQueueForMyObject {

    private static final VarHandle BUFFER_ARRAY = MethodHandles.arrayElementVarHandle(int[].class);
    private static final VarHandle VERSIONS_ARRAY = MethodHandles.arrayElementVarHandle(long[].class);

    private final PaddedAtomicInteger producerIdx = new PaddedAtomicInteger(0);

    private final PaddedAtomicInteger consumerIdx = new PaddedAtomicInteger(0);
    private final PaddedAtomicLong lastKnownVersion = new PaddedAtomicLong(2);

    private final int[] buffer;
    private final long[] versions;

    public StampedQueueForMyObject(int capacity) {
        this.buffer = new int[capacity << 2];

        this.versions = new long[capacity];
    }

    public void produce(MyObject object) {
        final int pIdx = producerIdx.getPlain();
        final long oldVersion = (long) VERSIONS_ARRAY.getOpaque(versions, pIdx);
        VERSIONS_ARRAY.setOpaque(versions, pIdx, oldVersion + 1);

        VarHandle.storeStoreFence();

        final int bIdx = pIdx << 2;
        BUFFER_ARRAY.setOpaque(buffer, bIdx, object.a);
        BUFFER_ARRAY.setOpaque(buffer, bIdx + 1, object.b);
        BUFFER_ARRAY.setOpaque(buffer, bIdx + 2, object.c);
        BUFFER_ARRAY.setOpaque(buffer, bIdx + 3, object.d);

        VERSIONS_ARRAY.setRelease(versions, pIdx, oldVersion + 2);
        producerIdx.setPlain(next(pIdx));
    }

    public void consume(Consumer<MyObject> consumer, MyObject object) {
        while (!Thread.interrupted()) {

            final int cIdx = consumerIdx.getPlain();
            final long beforeCopyVersion = (long) VERSIONS_ARRAY.getAcquire(versions, cIdx);

            if ((beforeCopyVersion & 1) == 1) {
                Thread.yield();
                continue;
            }

            final long lag = beforeCopyVersion - lastKnownVersion.getPlain();

            if (lag < 0) {
                Thread.yield();
                continue;
            } else if (lag > 2) {
                throw new IllegalStateException("Too huge lag, can't recover");
            } else if (lag == 2) {
                lastKnownVersion.setPlain(beforeCopyVersion);
            }

            final int bIdx = cIdx << 2;
            object.a = (int) BUFFER_ARRAY.getOpaque(buffer, bIdx);
            object.b = (int) BUFFER_ARRAY.getOpaque(buffer, bIdx + 1);
            object.c = (int) BUFFER_ARRAY.getOpaque(buffer, bIdx + 2);
            object.d = (int) BUFFER_ARRAY.getOpaque(buffer, bIdx + 3);

            VarHandle.loadLoadFence();

            final long afterCopyVersion = (long) VERSIONS_ARRAY.getOpaque(versions, cIdx);

            if (afterCopyVersion != beforeCopyVersion) {
                continue;
            }

            consumer.accept(object);
            consumerIdx.setPlain(next(cIdx));
            return;
        }

    }

    private int next(int idx) {
        return idx + 1 == versions.length ? 0 : idx + 1;
    }
}
