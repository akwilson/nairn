package com.wilsonak.nairn.eventbus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Simplest implementation of the {@code EventBus}. All subscribers are
 * processed on the same thread that events are published on.
 */
public class SingleThreadedEventBus implements EventBus {
    private final Map<Class<? extends Event>, Collection<FilteredCallback<? extends Event>>> events = new HashMap<>();
    private final Consumer<Throwable> unhandledExceptionCallback;

    /**
     * Initialises a new instance of the {@code SingleThreadedEventBus} class
     *
     * @param unhandledExceptionCallback called with unhandled exceptions from the subscribers
     */
    public SingleThreadedEventBus(Consumer<Throwable> unhandledExceptionCallback) {
        this.unhandledExceptionCallback = unhandledExceptionCallback;
    }

    /**
     * Initialises a new instance of the {@code SingleThreadedEventBus} class
     * without the optional exception handler
     */
    public SingleThreadedEventBus() {
        this(null);
    }

    private <T extends Event> void doPublish(Class<T> clazz, Event event) {
        /*
         * This double cast is certainly not ideal. It is safe however - we know what the type of
         * object is in the map because we put it there. Also the subscriber methods guaranteed
         * that the clazz and consumer are the same type.
         */
        @SuppressWarnings("unchecked")
        var subscribers = (Iterable<FilteredCallback<T>>)(Iterable<?>)events.get(clazz);

        if (subscribers != null) {
            subscribers.forEach(s -> {
                try {
                    T item = clazz.cast(event);
                    if (s.getFilter() == null || s.getFilter().test(item)) {
                        s.getConsumer().accept(item);
                    }
                } catch (Throwable e) {
                    if (unhandledExceptionCallback != null) {
                        unhandledExceptionCallback.accept(e);
                    }
                }
            });
        }
    }

    @Override
    public void publishEvent(Event event) {
        doPublish(event.getClass(), event);
    }

    @Override
    public <T extends Event> void addSubscriber(Class<T> clazz, Consumer<T> consumer) {
        var consumers = events.computeIfAbsent(clazz, k -> new ArrayList<>());
        consumers.add(new FilteredCallback<>(consumer, null));
    }

    @Override
    public <T extends Event> void addSubscriberForFilteredEvents(Class<T> clazz, Consumer<T> consumer, Predicate<T> filter) {
        var consumers = events.computeIfAbsent(clazz, k -> new ArrayList<>());
        consumers.add(new FilteredCallback<>(consumer, filter));
    }
}
