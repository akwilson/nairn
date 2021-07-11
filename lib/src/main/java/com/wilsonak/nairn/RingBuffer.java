package com.wilsonak.nairn;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A Ring Buffer is a fixed size circular collection which wraps
 * around itself. Old, unread items are overwritten as new items are added.
 * <p/>
 * Should be thread safe. The iterator takes a copy of the Ring Buffer which
 * may be costly, but iterating over a Ring Buffer is a less frequently
 * used operation than offer/poll.
 *
 * @param <T> the type of data to store in the Ring Buffer
 */
public class RingBuffer<T> extends AbstractQueue<T> {
    private final Object locker = new Object();
    private final T[] data;
    private final int capacity;
    private int readPos = 0;
    private int writePos = -1;
    private int size = 0;

    @SuppressWarnings("unchecked")
    public RingBuffer(int capacity) {
        this.data = (T[])new Object[capacity];
        this.capacity = capacity;
    }

    private RingBuffer(RingBuffer<T> orig) {
        synchronized (locker) {
            this.data = Arrays.copyOf(orig.data, orig.capacity);
            this.readPos = orig.readPos;
            this.writePos = orig.writePos;
            this.capacity = orig.capacity;
            this.size = orig.size;
        }
    }

    private void addSize() {
        if (size < capacity) {
            size++;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new RingBufferIterable<>(this);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean offer(T t) {
        synchronized (locker) {
            data[++writePos] = t;
            addSize();

            // loop back around
            if (writePos + 1 == capacity) {
                writePos = -1;
            }

            // push readPos round to the oldest value in the buffer
            if (size() == capacity) {
                readPos = writePos + 1;
                if (readPos >= capacity) {
                    readPos = 0;
                }
            }

            return true;
        }
    }

    @Override
    public T poll() {
        synchronized (locker) {
            if (isEmpty()) {
                return null;
            }

            T rv = data[readPos];
            data[readPos++] = null;

            // loop back around
            if (readPos >= capacity) {
                readPos = 0;
            }

            size--;
            return rv;
        }
    }

    @Override
    public T peek() {
        synchronized (locker) {
            return data[readPos];
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * An {@code Iterator} to read values from the Ring Buffer.
     */
    private static class RingBufferIterable<T> implements Iterator<T> {
        private final RingBuffer<T> copy;

        public RingBufferIterable(RingBuffer<T> original) {
            copy = new RingBuffer<>(original);
        }

        @Override
        public boolean hasNext() {
            return copy.size() != 0;
        }

        @Override
        public T next() {
            return copy.poll();
        }
    }
}
