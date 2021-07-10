package com.wilsonak.nairn.throttler;

import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link ThrottledConsumer} class.
 */
public class ThrottledConsumerTests {

    /**
     * Throttle to five per second. Should return five times from six calls.
     */
    @Test
    public void testFivePerSecond() {
        AtomicInteger result = new AtomicInteger(0);
        ThrottledConsumer<String> cut = new ThrottledConsumer<>(s -> result.incrementAndGet(), 5, ChronoUnit.SECONDS, 10);

        IntStream.range(0, 6).forEach(i -> cut.accept("AAA"));

        assertEquals("Wrong number of calls", 5, result.get());
    }

    /**
     * Throttle to five per second. Call five, wait, then call again. All six should return.
     */
    @Test
    public void testFivePerSecondWaitBefore() throws Exception {
        AtomicInteger result = new AtomicInteger(0);
        ThrottledConsumer<String> cut = new ThrottledConsumer<>(s -> result.incrementAndGet(), 5, ChronoUnit.SECONDS, 10);

        IntStream.range(0, 5).forEach(i -> cut.accept("AAA"));
        Thread.sleep(1000);
        cut.accept("AAA");

        assertEquals("Wrong number of calls", 6, result.get());
    }

    /**
     * Throttle to five per second. Call six. All six should return after a second's wait.
     */
    @Test
    public void testFivePerSecondWaitAfter() throws Exception {
        AtomicInteger result = new AtomicInteger(0);
        ThrottledConsumer<String> cut = new ThrottledConsumer<>(s -> result.incrementAndGet(), 5, ChronoUnit.SECONDS, 10);

        IntStream.range(0, 6).forEach(i -> cut.accept("AAA"));
        Thread.sleep(1100);

        assertEquals("Wrong number of calls", 6, result.get());
    }

    /**
     * Throttle to 5 per second. Should return 15 of 20 calls in 3 seconds.
     */
    @Test
    public void testLongRunning() throws Exception {
        AtomicInteger result = new AtomicInteger(0);
        ThrottledConsumer<String> cut = new ThrottledConsumer<>(s -> result.incrementAndGet(), 5, ChronoUnit.SECONDS, 10);

        IntStream.range(0, 20).forEach(i -> cut.accept("AAA"));
        Thread.sleep(3000);

        assertEquals("Wrong number of calls", 15, result.get());
    }
}
