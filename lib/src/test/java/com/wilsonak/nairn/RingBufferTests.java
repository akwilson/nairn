package com.wilsonak.nairn;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.util.Queue;

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
    public void testAddFour() {
        Queue<String> buf = new RingBuffer<>(10);
        buf.add("AAA");
        buf.add("AAB");
        buf.add("AAC");
        buf.add("AAD");

        assertFalse("Should not be empty", buf.isEmpty());
        assertEquals("Wrong buffer size", 4, buf.size());
        assertEquals("Wrong peek value", "AAA", buf.peek());
        MatcherAssert.assertThat("Wrong content through iterator", buf, contains("AAA", "AAB", "AAC", "AAD"));
    }

    @Test
    public void testAddOverCapacity() {
        Queue<String> buf = new RingBuffer<>(5);
        buf.add("AAA");
        buf.add("AAB");
        buf.add("AAC");
        buf.add("AAD");
        buf.add("AAE");
        buf.add("AAF");
        buf.add("AAG");

        assertFalse("Should not be empty", buf.isEmpty());
        assertEquals("Wrong buffer size", 5, buf.size());
        assertEquals("Wrong peek value", "AAF", buf.peek());
        MatcherAssert.assertThat("Wrong content through iterator", buf, contains("AAF", "AAG", "AAC", "AAD", "AAE"));
    }
}
