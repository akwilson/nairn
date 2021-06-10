package com.wilsonak.nairn.eventbus;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the multi threaded {@link EventBus}
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiThreadedEventBusTests {
    @Mock private ExecutorService threadPoolMock;

    /**
     * Verify that the subscribers are called via the thread pool.
     */
    @Test
    public void testCallback() {
        EventBus mteb = EventBus.newMultiThreadedEventBus(threadPoolMock);

        mteb.addSubscriber(Person.class, e -> {});
        mteb.addSubscriber(Person.class, e -> {});

        mteb.publishEvent(new Person("ALLAN", 21, "London"));

        verify(threadPoolMock, times(2)).execute((Runnable) any());
    }

    /**
     * Check that a publish event can still call two subscribers
     */
    @Test
    public void testMTCallback() throws Exception {
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        EventBus mteb = EventBus.newMultiThreadedEventBus(threadPool);

        AtomicInteger ai = new AtomicInteger(0);
        mteb.addSubscriber(Person.class, p -> ai.incrementAndGet());
        mteb.addSubscriber(Person.class, p -> ai.incrementAndGet());

        mteb.publishEvent(new Person("ALLAN", 21, "London"));

        // Allow threads to complete
        Thread.sleep(10);
        assertEquals("Wrong number of events", 2, ai.get());

        threadPool.shutdown();
    }
}
