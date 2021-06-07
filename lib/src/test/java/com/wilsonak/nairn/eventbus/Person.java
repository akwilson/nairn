package com.wilsonak.nairn.eventbus;

/**
 * POJO for input to the event bus for testing
 */
class Person implements Event {
    private final String name;
    private final int age;
    private final String address;

    public Person(String name, int age, String address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }

    @Override
    public String getId() {
        return getName();
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getAddress() {
        return address;
    }
}
