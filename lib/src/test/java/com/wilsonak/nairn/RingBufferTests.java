package com.wilsonak.nairn;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.util.Queue;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link RingBuffer} class.
 */
public class RingBufferTests {
    @Test
    public void testInit() {
        Queue<String> buf = new RingBuffer<>(10);
        assertTrue("Should be empty", buf.isEmpty());
        assertEquals("Wrong buffer size", 0, buf.size());
    }

    @Test
    public void testAddUnderCapacity() {
        Queue<String> buf = new RingBuffer<>(10);
        buf.offer("AAA");
        buf.offer("AAB");
        buf.offer("AAC");
        buf.offer("AAD");

        assertFalse("Should not be empty", buf.isEmpty());
        assertEquals("Wrong buffer size", 4, buf.size());
        assertEquals("Wrong peek value", "AAA", buf.peek());
        MatcherAssert.assertThat("Wrong content through iterator", buf, contains("AAA", "AAB", "AAC", "AAD"));
    }

    @Test
    public void testAddOverCapacity() {
        Queue<String> buf = new RingBuffer<>(5);

        IntStream.range(0, 7).forEach(i -> buf.offer("AA" + (char)('A' + i)));

        assertFalse("Should not be empty", buf.isEmpty());
        assertEquals("Wrong buffer size", 5, buf.size());
        assertEquals("Wrong peek value", "AAC", buf.peek());
        MatcherAssert.assertThat("Wrong content through iterator", buf, contains("AAC", "AAD", "AAE", "AAF", "AAG"));
    }

    @Test
    public void testPoll() {
        Queue<String> buf = new RingBuffer<>(5);

        IntStream.range(0, 7).forEach(i -> buf.offer("AA" + (char)('A' + i)));

        assertFalse("Should not be empty", buf.isEmpty());
        assertEquals("Wrong buffer size", 5, buf.size());
        assertEquals("Wrong poll value", "AAC", buf.poll());
        assertEquals("Wrong poll value", "AAD", buf.poll());
        assertEquals("Wrong poll value", "AAE", buf.poll());
        assertEquals("Wrong poll value", "AAF", buf.poll());
        assertEquals("Wrong poll value", "AAG", buf.poll());
        assertNull("Wrong poll value", buf.poll());
        assertTrue("Should be empty", buf.isEmpty());
        assertEquals("Wrong buffer size", 0, buf.size());
    }

    /**
     * A general test mixing poll() and offer() calls.
     */
    @Test
    public void testMixOfferPoll() {
        Queue<String> buf = new RingBuffer<>(5);

        buf.offer("AAA");
        buf.offer("AAB");
        buf.offer("AAC");

        assertEquals("Wrong poll value", "AAA", buf.poll());
        assertEquals("Wrong poll value", "AAB", buf.poll());
        assertEquals("Wrong buffer size", 1, buf.size());

        buf.offer("AAD");
        buf.offer("AAE");
        buf.offer("AAF");

        assertEquals("Wrong poll value", "AAC", buf.poll());
        assertEquals("Wrong poll value", "AAD", buf.poll());
        assertEquals("Wrong buffer size", 2, buf.size());

        IntStream.range(0, 7).forEach(i -> buf.offer("AA" + (char)('G' + i)));

        assertEquals("Wrong buffer size", 5, buf.size());
        assertEquals("Wrong poll value", "AAI", buf.poll());
        assertEquals("Wrong poll value", "AAJ", buf.poll());
        assertEquals("Wrong poll value", "AAK", buf.poll());
        assertEquals("Wrong poll value", "AAL", buf.poll());
        assertEquals("Wrong poll value", "AAM", buf.poll());
        assertNull("Wrong poll value", buf.poll());
        assertEquals("Wrong buffer size", 0, buf.size());
        assertTrue("Should be empty", buf.isEmpty());
    }
}
