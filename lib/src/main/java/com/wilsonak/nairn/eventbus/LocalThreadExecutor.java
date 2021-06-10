package com.wilsonak.nairn.eventbus;

import java.util.concurrent.Executor;

/**
 * {@code Executor} that executes the {@link Runnable} the the calling thread.
 */
class LocalThreadExecutor implements Executor {
    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
