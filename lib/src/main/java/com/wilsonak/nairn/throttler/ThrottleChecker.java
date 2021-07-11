package com.wilsonak.nairn.throttler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Checks the calling rate of the {@link #checkThrottle()} method.
 */
class ThrottleChecker {
    private final Object logLocker = new Object();
    private final int maxInPeriod;
    private final ChronoUnit timeUnit;
    private Set<LocalDateTime> log;

    /**
     * Initialises a new instance of the {@code ThrottleChecker} class
     *
     * @param maxInPeriod number of calls per time unit
     * @param timeUnit    time units to throttle by
     */
    public ThrottleChecker(int maxInPeriod, ChronoUnit timeUnit) {
        this.log = new TreeSet<>();
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
        synchronized (logLocker) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime periodAgo = now.minus(1, timeUnit);

            Set<LocalDateTime> newLog = log.stream()
                                           .filter(ldt -> ldt.isAfter(periodAgo))
                                           .collect(Collectors.toCollection(TreeSet::new));
            log = newLog;

            if (newLog.size() < maxInPeriod) {
                log.add(now);
                return true;
            }

            return false;
        }
    }
}
