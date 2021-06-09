package com.wilsonak.nairn;

import com.wilsonak.nairn.hashpool.HashPoolExecutorService;

/**
 * An identifiable {@code Runnable}. Identity used to select a thread pool by {@link HashPoolExecutorService}.
 */
public class KeyedRunnable implements Runnable {
    private final String id;
    private final Runnable runnable;

    public KeyedRunnable(String id, Runnable runnable) {
        this.id = id;
        this.runnable = runnable;
    }

    public String getId() {
        return id;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    @Override
    public void run() {
        runnable.run();
    }
}
