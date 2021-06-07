package com.wilsonak.nairn.eventbus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link SingleThreadedEventBus} class
 */
public class SingleThreadedEventBusTests {
    /**
     * Adds two Person subscribers and confirms that they both receive the published data
     */
    @Test
    public void testCallback() {
        EventBus steb = new SingleThreadedEventBus();

        List<Person> results = new ArrayList<>();
        steb.addSubscriber(Person.class, results::add);
        steb.addSubscriber(Person.class, results::add);

        steb.publishEvent(new Person("ALLAN", 21, "London"));

        assertEquals("Wrong number of events", 2, results.size());
        assertEquals("Wrong event", "ALLAN", results.get(0).getName());
        assertEquals("Wrong event", "ALLAN", results.get(1).getName());
    }

    /**
     * Adds Person and String subscribers. Publishes a Person and verifies that only the Person subscriber is called.
     */
    @Test
    public void testSingleCallback() {
        EventBus steb = new SingleThreadedEventBus();

        List<Person> pResults = new ArrayList<>();
        List<StringWrapper> strResults = new ArrayList<>();
        steb.addSubscriber(Person.class, pResults::add);
        steb.addSubscriber(StringWrapper.class, strResults::add);

        steb.publishEvent(new Person("ALLAN", 21, "London"));

        assertEquals("Wrong number of Person events", 1, pResults.size());
        assertEquals("Wrong number of String events", 0, strResults.size());
        assertEquals("Wrong person event", "ALLAN", pResults.get(0).getName());
    }

    /**
     * Subscribes to a Person with a filter. Only Person objects that match the filter should be received.
     */
    @Test
    public void testFilterCallback() {
        EventBus steb = new SingleThreadedEventBus();

        List<Person> results = new ArrayList<>();
        steb.addSubscriberForFilteredEvents(Person.class, results::add, p -> p.getAge() > 20);

        steb.publishEvent(new Person("ALLAN", 21, "London"));
        steb.publishEvent(new Person("FRODO", 18, "The Shire"));
        steb.publishEvent(new Person("GANDALF", 101, "Middle Earth"));

        // FRODO should be filtered out
        assertEquals("Wrong number of filtered events", 2, results.size());
        assertEquals("Wrong event", "ALLAN", results.get(0).getName());
        assertEquals("Wrong event", "GANDALF", results.get(1).getName());
    }

    private static class StringWrapper implements Event {
        private final String str;

        public StringWrapper(String str) {
            this.str = str;
        }

        @Override
        public String getId() {
            return str;
        }

        public String getStr() {
            return str;
        }
    }
}
