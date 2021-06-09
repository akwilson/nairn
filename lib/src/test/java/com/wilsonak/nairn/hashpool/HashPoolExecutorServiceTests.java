package com.wilsonak.nairn.hashpool;

import com.wilsonak.nairn.KeyedRunnable;
import org.junit.Test;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link HashPoolExecutorService} class.
 */
public class HashPoolExecutorServiceTests {
    @Test
    public void testSingleThreadHit() throws Exception {
        var hpe = new HashPoolExecutorService(3);
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
        var hpe = new HashPoolExecutorService(3);
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

    @Test
    public void testShutdown() throws Exception {
        var hpe = new HashPoolExecutorService(3);
        Runnable r = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        hpe.execute(r);
        hpe.shutdown();
        assertTrue("Shutdown flag incorrect", hpe.isShutdown());
        assertFalse("Terminate flag incorrect", hpe.isTerminated());
        Thread.sleep(1000);
        assertTrue("Shutdown flag incorrect", hpe.isShutdown());
        assertTrue("Terminate flag incorrect", hpe.isTerminated());
    }

    @Test
    public void testAwaitShutdown() throws Exception {
        var hpe = new HashPoolExecutorService(3);
        Runnable r = () -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        hpe.execute(r);
        hpe.shutdown();
        boolean res = hpe.awaitTermination(550, TimeUnit.MILLISECONDS);
        assertTrue(res);
    }

    @Test
    public void testAwaitShutdownFail() throws Exception {
        var hpe = new HashPoolExecutorService(3);
        Runnable r = () -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        hpe.execute(r);
        hpe.shutdown();
        boolean res = hpe.awaitTermination(100, TimeUnit.MILLISECONDS);
        assertFalse(res);
    }
}
