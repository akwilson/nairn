package com.wilsonak.nairn.eventbus;

import com.wilsonak.nairn.throttler.ThrottledCallback;
import com.wilsonak.nairn.throttler.Throttler;
import com.wilsonak.nairn.throttler.ThrottlerImpl;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link Throttler} classes.
 */
public class ThrottlerTests {
    /**
     * Test polling on a 100ms Throttler
     */
    @Test
    public void testThrottle() throws Exception {
        Throttler throttler = new ThrottlerImpl(100);
        assertEquals("Should start in a throttled state", Throttler.ThrottleResult.DO_NOT_PROCEED, throttler.shouldProceed());

        // Begin throttling
        throttler.throttle();
        assertEquals("Should still be throttled", Throttler.ThrottleResult.DO_NOT_PROCEED, throttler.shouldProceed());

        // Wait >100ms for throttled call to complete
        Thread.sleep(150);
        assertEquals("Should no longer be throttled", Throttler.ThrottleResult.PROCEED, throttler.shouldProceed());
    }

    /**
     * Tests an {@link EventBus} running in conjunction with a {@link ThrottledCallback}. The throttled consumer
     * should only call the callback every 100ms.
     */
    @Test
    public void testThrottledEventBus() throws Exception {
        EventBus eb = new SingleThreadedEventBus();
        AtomicInteger ai = new AtomicInteger(0);
        Consumer<Person> callback = new ThrottledCallback<>(new ThrottlerImpl(100), p -> ai.incrementAndGet());
        eb.addSubscriber(Person.class, callback);

        // Publish two events in quick succession
        Person p = new Person("ALLAN", 21, "LONDON");
        eb.publishEvent(p);
        eb.publishEvent(p);

        // First event returns immediately
        assertEquals("Did not throttle", 1, ai.get());

        // Wait >100ms for throttled call to complete
        Thread.sleep(150);
        assertEquals("Did not make second call", 2, ai.get());
    }
}
