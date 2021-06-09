package com.wilsonak.nairn.hashpool;

import com.wilsonak.nairn.KeyedRunnable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of {@link ExecutorService} with a number of thread pools. Pools are selected
 * by hashing an identifier on a {@link KeyedRunnable} passed to the {@link #execute(Runnable)}
 * method. In other words, all {@code KeyedRunnable}s with the same identifer are executed on the
 * same thread in the order they are presented to the {@code HashPoolExecutorService}. If the type
 * of the parameter to {@code execute} is not {@code KeyedRunnable} then requests use the first
 * thread pool.
 */
public class HashPoolExecutorService extends AbstractExecutorService {
    private final List<ExecutorService> threadPools;
    private final int numThreads;

    public HashPoolExecutorService(int numThreads) {
        this.numThreads = numThreads;
        this.threadPools = IntStream.range(0, numThreads)
                                    .mapToObj(i -> Executors.newSingleThreadExecutor())
                                    .collect(Collectors.toUnmodifiableList());
    }

    private ExecutorService findSlot(String id) {
        int slot = Math.abs(id.hashCode() % numThreads);
        return threadPools.get(slot);
    }

    @Override
    public void shutdown() {
        threadPools.forEach(ExecutorService::shutdown);
    }

    @Override
    public List<Runnable> shutdownNow() {
        return threadPools.stream()
                          .flatMap(es -> es.shutdownNow().stream())
                          .collect(Collectors.toList());
    }

    @Override
    public boolean isShutdown() {
        return threadPools.stream().allMatch(ExecutorService::isShutdown);
    }

    @Override
    public boolean isTerminated() {
        return threadPools.stream().allMatch(ExecutorService::isTerminated);
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        Queue<ExecutorService> queue = new ArrayDeque<>(threadPools);
        long timeoutMs = unit.toMillis(timeout);
        long start = System.currentTimeMillis();
        boolean result = true;
        ExecutorService service;
        while (result && (service = queue.poll()) != null) {
            long currentTimeout = timeoutMs - (System.currentTimeMillis() - start);
            if (currentTimeout > 0) {
                result = service.awaitTermination(currentTimeout, TimeUnit.MILLISECONDS);
            } else {
                result = false;
            }
        }

        return result;
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
