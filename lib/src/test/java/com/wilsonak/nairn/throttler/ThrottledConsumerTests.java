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
    @Test
    public void testFivePerSecond() {
        AtomicInteger result = new AtomicInteger(0);
        ThrottledConsumer<String> cut = new ThrottledConsumer<>(s -> result.incrementAndGet(), 5, ChronoUnit.SECONDS, 10);

        IntStream.range(0, 6).forEach(i -> cut.accept("AAA"));

        assertEquals("Wrong number of calls", 5, result.get());
    }

    @Test
    public void testFivePerSecondWaitBefore() throws Exception {
        AtomicInteger result = new AtomicInteger(0);
        ThrottledConsumer<String> cut = new ThrottledConsumer<>(s -> result.incrementAndGet(), 5, ChronoUnit.SECONDS, 10);

        IntStream.range(0, 5).forEach(i -> cut.accept("AAA"));
        Thread.sleep(1000);
        cut.accept("AAA");

        assertEquals("Wrong number of calls", 6, result.get());
    }

    @Test
    public void testFivePerSecondWaitAfter() throws Exception {
        AtomicInteger result = new AtomicInteger(0);
        ThrottledConsumer<String> cut = new ThrottledConsumer<>(s -> result.incrementAndGet(), 5, ChronoUnit.SECONDS, 10);

        IntStream.range(0, 6).forEach(i -> cut.accept("AAA"));
        Thread.sleep(1500);

        assertEquals("Wrong number of calls", 6, result.get());
    }
}
