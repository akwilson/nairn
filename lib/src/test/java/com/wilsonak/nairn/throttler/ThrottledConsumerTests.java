package com.wilsonak.nairn.throttler;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.containsInAnyOrder;
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
        ThrottledConsumer<String> cut = new ThrottledConsumer<>(s -> result.incrementAndGet(), 5, ChronoUnit.SECONDS);

        IntStream.range(0, 10).forEach(i -> cut.accept("AAA"));

        assertEquals("Wrong number of calls", 5, result.get());
    }

    /**
     * Throttle to five per second. Call 10, wait, then repeat. First five from each loop should return.
     */
    @Test
    public void testFivePerSecondTwice() throws Exception {
        Map<String, LocalDateTime> res = new ConcurrentHashMap<>();
        ThrottledConsumer<String> cut = new ThrottledConsumer<>(s -> res.put(s, LocalDateTime.now()), 5, ChronoUnit.SECONDS);

        for (int i = 0; i < 10; i++) {
            cut.accept("AA" + (char)('A' + i));
            Thread.sleep(10);
        }

        Thread.sleep(1000);
        for (int i = 10; i < 20; i++) {
            cut.accept("AA" + (char)('A' + i));
            Thread.sleep(10);
        }

        assertEquals("Wrong number of calls", 10, res.size());
        MatcherAssert.assertThat(
                "Wrong keys",
                res.keySet(),
                containsInAnyOrder("AAA", "AAB", "AAC", "AAD", "AAE", "AAK", "AAL", "AAM", "AAN", "AAO"));
    }
}
