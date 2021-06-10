package com.wilsonak.nairn.eventbus;

/**
 * An input to the {@link EventBus}. {@code Event} objects are
 * published to subscribers via the bus.
 */
public interface Event {
    String getId();
}
