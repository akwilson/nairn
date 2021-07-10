package com.wilsonak.nairn;

import java.util.AbstractQueue;
import java.util.Iterator;

/**
 * A Ring Buffer is a fixed size circular collection which wraps
 * around itself. Old, unread items are overwritten as new items are added.
 *
 * @param <T> the type of data to store in the Ring Buffer
 */
public class RingBuffer<T> extends AbstractQueue<T> {
    private final T[] data;
    private final int capacity;
    private int readPos = 0;
    private int writePos = -1;

    @SuppressWarnings("unchecked")
    public RingBuffer(int capacity) {
        this.data = (T[])new Object[capacity];
        this.capacity = capacity;
    }

    @Override
    public Iterator<T> iterator() {
        return new RingBufferIterable();
    }

    @Override
    public int size() {
        int sz = (writePos - readPos) + 1;
        return Math.min(sz, capacity);
    }

    @Override
    public boolean offer(T t) {
        data[++writePos % capacity] = t;
        return true;
    }

    @Override
    public T poll() {
        return data[readPos++ % capacity];
    }

    @Override
    public T peek() {
        return data[readPos % capacity];
    }

    @Override
    public boolean isEmpty() {
        return writePos < readPos;
    }

    /**
     * An {@code Iterator} to read values from the Ring Buffer.
     */
    private class RingBufferIterable implements Iterator<T> {
        private final int endPtr;
        private int readPosIter;

        public RingBufferIterable() {
            this.endPtr = size() - 1;
            this.readPosIter = readPos % capacity;
        }

        @Override
        public boolean hasNext() {
            return endPtr >= readPosIter;
        }

        @Override
        public T next() {
            return data[readPosIter++];
        }
    }
}
