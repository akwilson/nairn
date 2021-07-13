package com.wilsonak.nairn.cache;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Implementation of a {@code Function} which wraps a function
 * and caches the result in a thread-safe manner.
 *
 * @param <T> the input type
 * @param <R> the result type
 * @see "Brian Goetz, Java Concurreny in Practice, p. 108"
 */
public class CacheFunction<T, R> implements Function<T, R> {
    private final Map<T, Future<R>> cache = new ConcurrentHashMap<>();
    private final Function<T, R> function;

    /**
     * Initialise a new instance of the {@code CacheFunction} class.
     *
     * @param function the function to wrap, results will be cached
     */
    public CacheFunction(Function<T, R> function) {
        this.function = function;
    }

    @Override
    public R apply(T t) {
        while (true) {
            Future<R> result = cache.get(t);
            if (result == null) {
                FutureTask<R> task = new FutureTask<>(() -> function.apply(t));
                result = cache.putIfAbsent(t, task);
                if (result == null) {
                    result = task;
                    task.run();
                }
            }

            try {
                return result.get();
            } catch (CancellationException ex) {
                cache.remove(t);
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * @return the number of items cached
     */
    public int size() {
        return cache.size();
    }

    /**
     * Clear the contents of the cache.
     */
    public void clear() {
        cache.clear();
    }
}
