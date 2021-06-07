package com.wilsonak.nairn.hashpool;

import org.junit.Test;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link HashPoolExecutor} class.
 */
public class HashPoolExecutorTests {
    @Test
    public void testSingleThreadHit() throws Exception {
        var hpe = new HashPoolExecutor(3);
        var counter = new AtomicInteger(0);
        var threadIds = new CopyOnWriteArraySet<String>();
        Runnable r = () -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            threadIds.add(Thread.currentThread().getName());
            counter.incrementAndGet();
        };

        hpe.execute(new KeyedRunnable("AAA", r));
        hpe.execute(new KeyedRunnable("AAA", r));
        hpe.execute(new KeyedRunnable("AAA", r));

        Thread.sleep(350);
        assertEquals("Wrong thread used", 1, threadIds.size());
        assertEquals("Tasks incomplete", 3, counter.get());
    }

    @Test
    public void testMultiThreadHit() throws Exception {
        var hpe = new HashPoolExecutor(3);
        var counter = new AtomicInteger(0);
        var threadIds = new CopyOnWriteArraySet<String>();
        Runnable r = () -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            threadIds.add(Thread.currentThread().getName());
            counter.incrementAndGet();
        };

        hpe.execute(new KeyedRunnable("AAA", r));
        hpe.execute(new KeyedRunnable("AAB", r));
        hpe.execute(new KeyedRunnable("AAC", r));

        Thread.sleep(150);
        assertEquals("Wrong thread used", 3, threadIds.size());
        assertEquals("Tasks incomplete", 3, counter.get());
    }
}
