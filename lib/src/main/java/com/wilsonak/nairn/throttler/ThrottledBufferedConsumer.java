package com.wilsonak.nairn.throttler;

import com.wilsonak.nairn.RingBuffer;

import java.time.temporal.ChronoUnit;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Throttle calls to a {@code Consumer} at the specified rate, hold
 * skipped calls in a buffer to be called back later.
 *
 * @param <T> type of the parameter to be consumed
 */
public class ThrottledBufferedConsumer <T> implements Consumer<T> {
    private final Object bufferLocker = new Object();
    private final ThrottleChecker throttleChecker;
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
    private final Queue<T> buffer;
    private final Consumer<T> consumer;

    /**
     * Initialises a new instance of the {@code ThrottledBufferedConsumer} class
     *
     * @param consumer    consumer call to throttle
     * @param maxInPeriod number of calls per time unit
     * @param timeUnit    time units to throttle by
     * @param backlog     number of items to queue while throttling
     */
    public ThrottledBufferedConsumer(Consumer<T> consumer, int maxInPeriod, ChronoUnit timeUnit, int backlog) {
        this.throttleChecker = new ThrottleChecker(maxInPeriod, timeUnit);
        this.buffer = new RingBuffer<>(backlog);
        this.consumer = consumer;
    }

    /**
     * Determines the next item to be sent to the consumer.
     * <p/>
     * If something is already in the queue then prioritise that to ensure the order of
     * items outgoing is the same as the order incoming.
     *
     * @param item      the next item incoming
     * @param maybeSwap false to disable the prioritisation behaviour
     */
    private T getNextData(T item, boolean maybeSwap) {
        synchronized (bufferLocker) {
            if (maybeSwap && !buffer.isEmpty()) {
                buffer.offer(item);
                return buffer.poll();
            }

            return item;
        }
    }

    /**
     * Called by the scheduler to read the next item from the buffer. Don't bother
     * prioritising queue items in this case.
     */
    private void readBuffer() {
        T item;
        synchronized (bufferLocker) {
            item = buffer.poll();
        }

        if (item != null) {
            doAccept(item, false);
        }
    }

    private void doAccept(T t, boolean maybeSwap) {
        if (throttleChecker.checkThrottle()) {
            consumer.accept(getNextData(t, maybeSwap));
        } else {
            synchronized (bufferLocker) {
                buffer.offer(t);
            }

            scheduler.schedule(this::readBuffer, 1, TimeUnit.of(throttleChecker.getTimeUnit()));
        }
    }

    @Override
    public void accept(T t) {
        doAccept(t, true);
    }
}
