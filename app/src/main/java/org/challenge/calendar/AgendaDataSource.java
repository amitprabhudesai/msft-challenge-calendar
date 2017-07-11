package org.challenge.calendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AgendaDataSource {
    private final List<String> days;
    private final Map<String, List<CalendarEvent>> events;

    public AgendaDataSource(final List<String> days, final Map<String, List<CalendarEvent>> events) {
        this.days = new ArrayList<>(days);
        this.events = new HashMap<>(events);
    }

    public final Iterable<String> days() {
        return days;
    }

    public final Map<String, List<CalendarEvent>> events() {
        return events;
    }
}
