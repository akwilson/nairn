package com.wilsonak.nairn.eventbus;

import com.wilsonak.nairn.KeyedRunnable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Multi-threaded {@code EventBus} implementation. Subscribers are executed on threads in a thread pool.
 */
public class MultiThreadedEventBus implements EventBus {
    private final Map<Class<? extends Event>, Collection<FilteredCallback<? extends Event>>> events = new ConcurrentHashMap<>();
    private final Executor threadPool;
    private final Consumer<Throwable> unhandledExceptionCallback;

    /**
     * Initialises a new instance of the {@code MultiThreadedEventBus} class
     *
     * @param threadPool                 used to execute subscriber methods
     * @param unhandledExceptionCallback called with unhandled exceptions from the subscribers
     */
    public MultiThreadedEventBus(Executor threadPool, Consumer<Throwable> unhandledExceptionCallback) {
        this.threadPool = threadPool;
        this.unhandledExceptionCallback = unhandledExceptionCallback;
    }

    /**
     * Initialises a new instance of the {@code MultiThreadedEventBus} class
     * without the optional exception handler
     *
     * @param threadPool used to execute subscriber methods
     */
    public MultiThreadedEventBus(Executor threadPool) {
        this(threadPool, null);
    }

    private <T extends Event> void execute(FilteredCallback<T> fc, T event) {
        try {
            if (fc.getFilter() == null || fc.getFilter().test(event)) {
                fc.getConsumer().accept(event);
            }
        } catch (Throwable e) {
            if (unhandledExceptionCallback != null) {
                unhandledExceptionCallback.accept(e);
            }
        }
    }

    private <T extends Event> void doPublish(Class<T> clazz, Event event) {
        @SuppressWarnings("unchecked")
        var subscribers = (Iterable<FilteredCallback<T>>)(Iterable<?>)events.get(clazz);
        if (subscribers != null) {
            subscribers.forEach(s -> threadPool.execute(new KeyedRunnable(event.getId(), () -> execute(s, clazz.cast(event)))));
        }
    }

    @Override
    public void publishEvent(Event event) {
        doPublish(event.getClass(), event);
    }

    @Override
    public <T extends Event> void addSubscriber(Class<T> clazz, Consumer<T> consumer) {
        var consumers = events.computeIfAbsent(clazz, k -> new CopyOnWriteArrayList<>());
        consumers.add(new FilteredCallback<>(consumer, null));
    }

    @Override
    public <T extends Event> void addSubscriberForFilteredEvents(Class<T> clazz, Consumer<T> consumer, Predicate<T> filter) {
        var consumers = events.computeIfAbsent(clazz, k -> new CopyOnWriteArrayList<>());
        consumers.add(new FilteredCallback<>(consumer, filter));
    }
}
