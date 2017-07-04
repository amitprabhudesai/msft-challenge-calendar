package org.challenge.calendar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generate some mock events.
 */
public class MockEventsDataSource {
    private static Event[] EVENTS = new Event[3];

    static {
        EVENTS[0] = new Event("Breakfast at Tiffany's");
        EVENTS[1] = new Event("Lunch with Dave");
        EVENTS[2] = new Event("Coffee with Sam");
    }
    public static List<Event> generateEvents(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("generateFeedItems - Invalid count:" + count);
        }
        final ArrayList<Event> events = new ArrayList<>(count);
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            events.add(EVENTS[random.nextInt(EVENTS.length)]);
        }
        return events;
    }
}
