package com.wilsonak.nairn.throttler;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * Uses a {@link Throttler} to throttle calls to a {@code Consumer}.
 *
 * @param <T> type of the parameter to be consumed
 */
public class ThrottledCallback<T> implements Consumer<T> {
    private final Object locker = new Object(); // Ensures the ThrottledCallback is maintained in a consistent state
    private final Deque<T> queuedItems = new LinkedList<>();
    private final Throttler throttler;
    private final Consumer<T> callback;
    private final int backlog;
    private boolean running;

    /**
     * Initialises a new instance of the {@code ThrottledCallback} class
     *
     * @param throttler mechanism to throttle the consumer
     * @param callback  consumer call to throttle
     * @param backlog   number of items to queue while throttling
     */
    public ThrottledCallback(Throttler throttler, Consumer<T> callback, int backlog) {
        this.throttler = throttler;
        this.callback = callback;
        this.backlog = backlog;

        this.throttler.notifyWhenCanProceed(() -> {
            synchronized (locker) {
                if (!queuedItems.isEmpty()) {
                    callback.accept(queuedItems.pollFirst());
                    throttler.throttle();
                }
            }
        });
    }

    /**
     * Initialises a new instance of the {@code ThrottledCallback} class
     *
     * @param throttler mechanism to throttle the consumer
     * @param callback  consumer call to throttle
     */
    public ThrottledCallback(Throttler throttler, Consumer<T> callback) {
        this(throttler, callback, 0);
    }

    @Override
    public void accept(T t) {
        /*
         * TODO:
         * Store log of timestamps of calls to accept.
         * When new call is made count number of calls within the last time period
         * Delete everything else
         * if num calls < desired number then add to queue, else execute
         */
        synchronized (locker) {
            // While throttled, add the parameter to the queue
            if (running && throttler.shouldProceed() == Throttler.ThrottleResult.DO_NOT_PROCEED) {
                // Remove old items from the front of the queue. These will be lost to the consumer.
                if (backlog > 0 && queuedItems.size() == backlog) {
                    queuedItems.pollFirst();
                }

                queuedItems.offerLast(t);
            } else {
                callback.accept(t);
                throttler.throttle();
                running = true;
            }
        }
    }
}
