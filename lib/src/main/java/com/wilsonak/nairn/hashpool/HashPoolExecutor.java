package com.wilsonak.nairn.hashpool;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HashPoolExecutor implements Executor {
    private final List<ExecutorService> threadPools;
    private final int numThreads;

    private ExecutorService findSlot(String id) {
        int slot = Math.abs(id.hashCode() % numThreads);
        return threadPools.get(slot);
    }

    public HashPoolExecutor(int numThreads) {
        this.threadPools = IntStream.range(0, numThreads)
                                    .mapToObj(i -> Executors.newSingleThreadExecutor())
                                    .collect(Collectors.toUnmodifiableList());
        this.numThreads = numThreads;
    }

    @Override
    public void execute(Runnable runnable) {
        if (runnable instanceof KeyedRunnable) {
            KeyedRunnable keyedRunnable = (KeyedRunnable)runnable;
            ExecutorService svc = findSlot(keyedRunnable.getId());
            svc.execute(keyedRunnable.getRunnable());
        } else {
            threadPools.get(0).execute(runnable);
        }
    }
}
