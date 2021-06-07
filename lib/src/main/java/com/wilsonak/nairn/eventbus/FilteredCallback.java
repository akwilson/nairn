package com.wilsonak.nairn.eventbus;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * References a Consumer and a possible filter for the Event Bus implementation.
 *
 * @param <T> the type of object to be processed by the consumer
 */
class FilteredCallback<T> {
    private final Consumer<T> consumer;
    private final Predicate<T> filter;

    public FilteredCallback(Consumer<T> consumer, Predicate<T> filter) {
        this.consumer = consumer;
        this.filter = filter;
    }

    public Consumer<T> getConsumer() {
        return consumer;
    }

    public Predicate<T> getFilter() {
        return filter;
    }
}
