package com.wilsonak.nairn.eventbus;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An event bus which registers subscribers and publishes events to them
 */
public interface EventBus {
    /**
     * Publish an event to all relevant subscribers
     *
     * @param event the event to publish
     */
    void publishEvent(Event event);

    /**
     * Make a subscription. Events published of the given type will be sent to the consumer.
     *
     * @param clazz    subscribe to events of this class
     * @param consumer to be called when the class is published
     * @param <T>      the type of event to subscribe to
     */
    <T extends Event> void addSubscriber(Class<T> clazz, Consumer<T> consumer);

    /**
     * Make a subscription. Events published of the given type will be sent to the consumer if they pass the filter.
     *
     * @param clazz    subscribe to events of this class
     * @param consumer to be called when the class is published
     * @param filter   events that pass the filter will be sent to the consumer
     * @param <T>      the type of event to subscribe to
     */
    <T extends Event> void addSubscriberForFilteredEvents(Class<T> clazz, Consumer<T> consumer, Predicate<T> filter);

    /**
     * Initialises a new single threaded {@code EventBus} instance. All subscribers are
     * processed on the same thread that the event is published on.
     */
    static EventBus newSingleThreadedEventBus() {
        return new EventBusImpl(new LocalThreadExecutor(), null);
    }

    /**
     * Initialises a new single threaded {@code EventBus} instance. All subscribers are
     * processed on the same thread that the event is published on.
     *
     * @param unhandledExceptionCallback called with unhandled exceptions from the subscribers
     */
    static EventBus newSingleThreadedEventBus(Consumer<Throwable> unhandledExceptionCallback) {
        return new EventBusImpl(new LocalThreadExecutor(), unhandledExceptionCallback);
    }

    /**
     * Initialises a new multi threaded {@code EventBus} instance. Subscribers are executed
     * on threads in a thread pool.
     */
    static EventBus newMultiThreadedEventBus(Executor executor) {
        return new EventBusImpl(executor, null);
    }

    /**
     * Initialises a new multi threaded {@code EventBus} instance. Subscribers are executed
     * on threads in a thread pool.
     *
     * @param unhandledExceptionCallback called with unhandled exceptions from the subscribers
     */
    static EventBus newMultiThreadedEventBus(Executor executor, Consumer<Throwable> unhandledExceptionCallback) {
        return new EventBusImpl(executor, unhandledExceptionCallback);
    }
}
