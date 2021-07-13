package com.wilsonak.nairn.cache;

import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link CacheFunction} class.
 */
public class CacheFunctionTests {
    private final CountDownLatch latch = new CountDownLatch(1);

    private int pauseFunction(int arg) {
        try {
            latch.await();
            return arg * arg;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFunctionCall() {
        var callCount = new AtomicInteger(0);
        var results = new ConcurrentHashMap<Integer, Integer>();
        var cache = new CacheFunction<Integer, Integer>(i -> {
            callCount.incrementAndGet();
            return i * i;
        });

        IntStream.rangeClosed(1, 5).forEach(i -> results.put(i, cache.apply(i)));

        assertEquals("Wrong call count", 5, callCount.get());
        assertThat("Wrong results", results, hasEntry(1, 1));
        assertThat("Wrong results", results, hasEntry(2, 4));
        assertThat("Wrong results", results, hasEntry(3, 9));
        assertThat("Wrong results", results, hasEntry(4, 16));
        assertThat("Wrong results", results, hasEntry(5, 25));

        // Do it again, call count should remain the same
        IntStream.rangeClosed(1, 5).forEach(i -> results.put(i, cache.apply(i)));
        assertEquals("Wrong call count", 5, callCount.get());

        assertEquals("Wrong cache size", 5, cache.size());
    }

    /**
     * Call the cached function three times simultaneously. Only
     * one call should be made to the cached function.
     */
    @Test
    public void testMultiThreaded() throws Exception {
        var callCount = new AtomicInteger(0);
        var results = new ConcurrentHashMap<Integer, Integer>();
        var cache = new CacheFunction<Integer, Integer>(i -> {
            callCount.incrementAndGet();
            return pauseFunction(i);
        });
        Runnable r = () -> results.put(3, cache.apply(3));

        new Thread(r).start();
        new Thread(r).start();
        new Thread(r).start();
        latch.countDown();
        Thread.sleep(10);

        assertEquals("Wrong call count", 1, callCount.get());
        assertThat("Wrong results", results, hasEntry(3, 9));
        assertEquals("Wrong cache size", 1, cache.size());
    }

    @Test
    public void testClear() {
        var cache = new CacheFunction<Integer, Integer>(i -> i * i);
        IntStream.rangeClosed(1, 5).forEach(cache::apply);
        assertEquals("Wrong cache size", 5, cache.size());

        cache.clear();
        assertEquals("Wrong cache size after clear", 0, cache.size());
    }
}
