package com.wilsonak.nairn.throttler;

/**
 * Indicates if an operation should proceed based on recent usage.
 */
public interface Throttler {
    enum ThrottleResult { PROCEED, DO_NOT_PROCEED }

    /**
     * Poll the resource to see if the operation should proceed
     */
    ThrottleResult shouldProceed();

    /**
     * Register a callback when the operation should succeed
     */
    void notifyWhenCanProceed(Runnable callback);

    /**
     * Begin throttling
     */
    void throttle();
}
