package com.wilsonak.nairn.eventbus;

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
}
