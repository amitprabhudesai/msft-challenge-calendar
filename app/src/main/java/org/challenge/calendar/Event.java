package org.challenge.calendar;

import java.util.Date;

/**
 * A calendar event.
 */
public class Event {
    private final String description;

    public Event(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
