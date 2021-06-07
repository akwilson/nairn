package com.wilsonak.nairn.eventbus;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Multi-threaded {@code EventBus} implementation. Subscribers are executed on threads in a thread pool.
 */
public class MultiThreadedEventBus implements EventBus, AutoCloseable {
    private final Map<Class<? extends Event>, Collection<FilteredCallback<? extends Event>>> events = new ConcurrentHashMap<>();
    private final ExecutorService threadPool;

    public MultiThreadedEventBus(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    private static <T extends Event> void execute(FilteredCallback<T> fc, T item) {
        if (fc.getFilter() == null || fc.getFilter().test(item)) {
            fc.getConsumer().accept(item);
        }
    }

    private <T extends Event> void doPublish(Class<T> clazz, Event event) {
        @SuppressWarnings("unchecked")
        Iterable<FilteredCallback<T>> subscribers = (Iterable<FilteredCallback<T>>) (Iterable<?>) events.get(clazz);
        if (subscribers != null) {
            subscribers.forEach(s -> threadPool.submit(() -> execute(s, clazz.cast(event))));
        }
    }

    @Override
    public void publishEvent(Event event) {
        doPublish(event.getClass(), event);
    }

    @Override
    public <T extends Event> void addSubscriber(Class<T> clazz, Consumer<T> consumer) {
        Collection<FilteredCallback<? extends Event>> consumers = events.computeIfAbsent(clazz, k -> new CopyOnWriteArrayList<>());
        consumers.add(new FilteredCallback<>(consumer, null));
    }

    @Override
    public <T extends Event> void addSubscriberForFilteredEvents(Class<T> clazz, Consumer<T> consumer, Predicate<T> filter) {
        Collection<FilteredCallback<? extends Event>> consumers = events.computeIfAbsent(clazz, k -> new CopyOnWriteArrayList<>());
        consumers.add(new FilteredCallback<>(consumer, filter));
    }

    @Override
    public void close() {
        threadPool.shutdown();
    }
}
