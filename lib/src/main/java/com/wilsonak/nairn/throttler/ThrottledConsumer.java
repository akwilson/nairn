package com.wilsonak.nairn.throttler;

import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

/**
 * Throttle calls to a {@code Consumer} at the specified rate.
 *
 * @param <T> type of the parameter to be consumed
 */
public class ThrottledConsumer<T> implements Consumer<T> {
    private final ThrottleChecker throttleChecker;
    private final Consumer<T> consumer;

    /**
     * Initialises a new instance of the {@code ThrottledConsumer} class
     *
     * @param consumer    consumer call to throttle
     * @param maxInPeriod number of calls per time unit
     * @param timeUnit    time units to throttle by
     */
    public ThrottledConsumer(Consumer<T> consumer, int maxInPeriod, ChronoUnit timeUnit) {
        this.throttleChecker = new ThrottleChecker(maxInPeriod, timeUnit);
        this.consumer = consumer;
    }

    @Override
    public void accept(T t) {
        if (throttleChecker.checkThrottle()) {
            consumer.accept(t);
        }
    }
}
