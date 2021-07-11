package com.wilsonak.nairn.throttler;

import com.wilsonak.nairn.RingBuffer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Queue;

/**
 * Checks the calling rate of the {@link #checkThrottle()} method.
 */
class ThrottleChecker {
    private final int maxInPeriod;
    private final ChronoUnit timeUnit;
    private final Queue<LocalDateTime> log;

    /**
     * Initialises a new instance of the {@code ThrottleChecker} class
     *
     * @param maxInPeriod number of calls per time unit
     * @param timeUnit    time units to throttle by
     */
    public ThrottleChecker(int maxInPeriod, ChronoUnit timeUnit) {
        this.log = new RingBuffer<>(maxInPeriod);
        this.maxInPeriod = maxInPeriod;
        this.timeUnit = timeUnit;
    }

    public int getMaxInPeriod() {
        return maxInPeriod;
    }

    public ChronoUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * Counts the number of calls made within the last time period. If the number is within the
     * maximum rate then the we are within the throttle limits.
     *
     * @return true if the call rate is within the desired limit
     */
    public boolean checkThrottle() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodAgo = now.minus(1, timeUnit);

        long numInPeriod = log.stream()
                              .filter(ldt -> ldt.isAfter(periodAgo))
                              .count();

        if (numInPeriod < maxInPeriod) {
            log.offer(now);
            return true;
        }

        return false;
    }
}
