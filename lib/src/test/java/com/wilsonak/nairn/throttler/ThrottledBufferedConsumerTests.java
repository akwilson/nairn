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
 * Unit tests for the {@link ThrottledBufferedConsumer} class.
 */
public class ThrottledBufferedConsumerTests {

    /**
     * Throttle to five per second. Should return five times from six calls.
     */
    @Test
    public void testFivePerSecond() {
        AtomicInteger result = new AtomicInteger(0);
        ThrottledBufferedConsumer<String> cut = new ThrottledBufferedConsumer<>(s -> result.incrementAndGet(), 5, ChronoUnit.SECONDS, 10);

        IntStream.range(0, 6).forEach(i -> cut.accept("AAA"));

        assertEquals("Wrong number of calls", 5, result.get());
    }

    /**
     * Throttle to five per second. Call five, wait, then call again. All six should return.
     */
    @Test
    public void testFivePerSecondWaitBefore() throws Exception {
        AtomicInteger result = new AtomicInteger(0);
        ThrottledBufferedConsumer<String> cut = new ThrottledBufferedConsumer<>(s -> result.incrementAndGet(), 5, ChronoUnit.SECONDS, 10);

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
        ThrottledBufferedConsumer<String> cut = new ThrottledBufferedConsumer<>(s -> result.incrementAndGet(), 5, ChronoUnit.SECONDS, 10);

        IntStream.range(0, 6).forEach(i -> cut.accept("AAA"));
        Thread.sleep(1100);

        assertEquals("Wrong number of calls", 6, result.get());
    }

    /**
     * Throttle to 5 per second. Should return 15 of 20 calls in 3 seconds.
     */
    @Test
    public void testLongRunning() throws Exception {
        Map<String, LocalDateTime> res = new ConcurrentHashMap<>();
        ThrottledBufferedConsumer<String> cut = new ThrottledBufferedConsumer<>(s -> res.put(s, LocalDateTime.now()), 5, ChronoUnit.SECONDS, 50);

        for (int i = 0; i < 20; i++) {
            cut.accept("AA" + (char)('A' + i));
            Thread.sleep(10);
        }

        assertEquals("Wrong number of calls", 5, res.size());
        MatcherAssert.assertThat("Wrong keys", res.keySet(), containsInAnyOrder("AAA", "AAB", "AAC", "AAD", "AAE"));
        res.clear();

        Thread.sleep(1000);
        assertEquals("Wrong number of calls", 5, res.size());
        MatcherAssert.assertThat("Wrong keys", res.keySet(), containsInAnyOrder("AAF", "AAG", "AAH", "AAI", "AAJ"));
        res.clear();

        Thread.sleep(1000);
        assertEquals("Wrong number of calls", 5, res.size());
        MatcherAssert.assertThat("Wrong keys", res.keySet(), containsInAnyOrder("AAK", "AAL", "AAM", "AAN", "AAO"));
        res.clear();

        Thread.sleep(1000);
        assertEquals("Wrong number of calls", 5, res.size());
        MatcherAssert.assertThat("Wrong keys", res.keySet(), containsInAnyOrder("AAP", "AAQ", "AAR", "AAS", "AAT"));
    }

    @Test
    public void testLaterAddWhileItemsQueued() throws Exception {
        Map<String, LocalDateTime> res = new ConcurrentHashMap<>();
        ThrottledBufferedConsumer<String> cut = new ThrottledBufferedConsumer<>(s -> res.put(s, LocalDateTime.now()), 5, ChronoUnit.SECONDS, 50);

        for (int i = 0; i < 10; i++) {
            cut.accept("AA" + (char)('A' + i));
            Thread.sleep(10);
        }

        assertEquals("Wrong number of calls", 5, res.size());
        MatcherAssert.assertThat("Wrong keys", res.keySet(), containsInAnyOrder("AAA", "AAB", "AAC", "AAD", "AAE"));
        res.clear();

        // Currently there are 5 items queued. Sleep and call again before first queued item called back.
        Thread.sleep(900);
        cut.accept("ZZZ");
        // Original order should be preserved
        MatcherAssert.assertThat("Wrong keys", res.keySet(), containsInAnyOrder("AAF"));
        res.clear();

        Thread.sleep(1000);
        assertEquals("Wrong number of calls", 4, res.size());
        MatcherAssert.assertThat("Wrong keys", res.keySet(), containsInAnyOrder("AAG", "AAH", "AAI", "AAJ"));
        res.clear();

        Thread.sleep(100);
        assertEquals("Wrong number of calls", 1, res.size());
        MatcherAssert.assertThat("Wrong keys", res.keySet(), containsInAnyOrder("ZZZ"));
    }
}
