package com.wilsonak.nairn.throttler;

import com.wilsonak.nairn.RingBuffer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Throttle calls to a {@code Consumer} at the specified rate.
 *
 * @param <T> type of the parameter to be consumed
 */
public class ThrottledConsumer<T> implements Consumer<T> {
    private final Object locker = new Object();
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
    private final Queue<T> buffer;
    private final Consumer<T> consumer;
    private final int maxInPeriod;
    private final ChronoUnit timeUnit;
    private Set<LocalDateTime> log;

    /**
     * Initialises a new instance of the {@code ThrottledConsumer} class
     *
     * @param consumer    consumer call to throttle
     * @param maxInPeriod number of calls per time unit
     * @param timeUnit    time units to throttle by
     * @param backlog     number of items to queue while throttling
     */
    public ThrottledConsumer(Consumer<T> consumer, int maxInPeriod, ChronoUnit timeUnit, int backlog) {
        this.buffer = new RingBuffer<>(backlog);
        this.log = new TreeSet<>();
        this.consumer = consumer;
        this.maxInPeriod = maxInPeriod;
        this.timeUnit = timeUnit;
    }

    /**
     * Counts the number of calls made within the last time period. If the number is within the
     * maximum rate then the we are within the throttle limits.
     *
     * @return true if the call rate is within the desired limit
     */
    private boolean checkThrottle() {
        synchronized (locker) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime periodAgo = now.minus(1, timeUnit);

            Set<LocalDateTime> newLog = log.stream()
                                           .filter(ldt -> ldt.isAfter(periodAgo))
                                           .collect(Collectors.toCollection(TreeSet::new));
            newLog.add(now);
            log = newLog;

            return newLog.size() <= maxInPeriod;
        }
    }

    @Override
    public void accept(T t) {
        if (checkThrottle()) {
            consumer.accept(t);
        } else {
            buffer.offer(t);
            scheduler.schedule(() -> accept(buffer.poll()), 1, TimeUnit.of(timeUnit));
        }
    }
}
