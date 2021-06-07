package com.wilsonak.nairn.throttler;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Uses a {@link ScheduledExecutorService} to throttle for a given delay.
 */
public class ThrottlerImpl implements Throttler, AutoCloseable {
    private final Collection<Runnable> callbacks = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
    private final long delay;

    private ThrottleResult ready = ThrottleResult.DO_NOT_PROCEED;

    public ThrottlerImpl(long delay) {
        this.delay = delay;
    }

    private void markReady() {
        ready = ThrottleResult.PROCEED;
        callbacks.forEach(Runnable::run);
    }

    @Override
    public ThrottleResult shouldProceed() {
        return ready;
    }

    @Override
    public void notifyWhenCanProceed(Runnable callback) {
        callbacks.add(callback);
    }

    @Override
    public void throttle() {
        scheduler.schedule(this::markReady, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }
}
