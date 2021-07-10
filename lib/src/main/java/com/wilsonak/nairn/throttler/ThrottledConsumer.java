package com.wilsonak.nairn.throttler;

import com.wilsonak.nairn.RingBuffer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Throttle calls to a {@code Consumer}.
 *
 * @param <T> type of the parameter to be consumed
 */
public class ThrottledConsumer<T> implements Consumer<T> {
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
    private final Set<LocalDateTime> log = new TreeSet<>();
    private final Queue<T> buffer;
    private final Consumer<T> consumer;
    private final int rate;
    private final ChronoUnit timeUnit;

    /**
     * Initialises a new instance of the {@code ThrottledConsumer} class
     *
     * @param consumer consumer call to throttle
     * @param rate     number of calls per time unit
     * @param timeUnit time units to throttle by
     * @param backlog  number of items to queue while throttling
     */
    public ThrottledConsumer(Consumer<T> consumer, int rate, ChronoUnit timeUnit, int backlog) {
        this.buffer = new RingBuffer<>(backlog);
        this.consumer = consumer;
        this.rate = rate;
        this.timeUnit = timeUnit;
    }

    private boolean checkTimes(T t) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodAgo = now.minus(1, timeUnit);

        int cnt = 0;
        for (LocalDateTime ldt : log) {
            if (ldt.isAfter(periodAgo)) {
                cnt++;
            }
        }

        log.add(now);

        if (cnt < rate) {
            return true;
        } else {
            buffer.offer(t);
            scheduler.schedule(() -> accept(buffer.poll()), 1, TimeUnit.of(timeUnit));
        }

        return false;
    }

    @Override
    public void accept(T t) {
        if (checkTimes(t)) {
            consumer.accept(t);
        }
    }
}
