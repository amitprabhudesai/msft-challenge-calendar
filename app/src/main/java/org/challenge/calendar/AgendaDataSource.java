package org.challenge.calendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AgendaDataSource {

    private final Calendar calendar;
    private final SimpleDateFormat sectionHeaderFormatter;
    private final SimpleDateFormat eventTimeFormatter;
    // Maintain a list of events keyed by the day represented
    // as time in millis since epoch
    // This is done to be able to easily compute rank() query
    // where we want to get count of events that occur earlier
    // than a given time
    // For display, we use appropriate {@link SimpleDateFormat}
    // formatters to convert from the this to the display text
    // Vis-a-vis the AgendaView, the keys represent the sections,
    // while the values (list of events) represent the collection
    // of events in that section
    private final Map<Long, List<CalendarEvent>> events;

    static final long INVALID_TIME = Long.MIN_VALUE;

    public AgendaDataSource(Calendar calendar,
                            SimpleDateFormat sectionHeaderFormatter,
                            SimpleDateFormat eventTimeFormatter) {
        this.calendar = calendar;
        this.sectionHeaderFormatter = sectionHeaderFormatter;
        this.eventTimeFormatter = eventTimeFormatter;
        this.events = new LinkedHashMap<>();
    }

    public void clear() {
        events.clear();
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public SimpleDateFormat getSectionHeaderFormatter() {
        return sectionHeaderFormatter;
    }

    public SimpleDateFormat getEventTimeFormatter() {
        return eventTimeFormatter;
    }

    public void addEvent(long time, CalendarEvent event) {
        getEvents(time).add(event);
    }

    // count of events that occur earlier than a given time
    public int rank(long time) {
        int rank = 0;
        for (long val : events.keySet()) {
            if (val <= time) {
                rank += events.get(val).size();
            }
        }
        return rank;
    }

    public int getSectionCount() {
        return events.size();
    }

    public int getEventCount(int section) {
        long time = getTime(section);
        if (INVALID_TIME == time) return 0;
        return events.get(time).size();
    }

    public long getTime(int section) {
        return section <= getSectionCount() ? fromSection(section) : INVALID_TIME;
    }

    public String getHeaderAsDisplayText(int section) {
        calendar.setTimeInMillis(getTime(section));
        return sectionHeaderFormatter.format(calendar.getTime());
    }

    public CalendarEvent getEventItem(int section, int position) {
        return events.get(getTime(section)).get(position);
    }

    private boolean hasSection(long time) {
        return events.keySet().contains(time);
    }

    private long fromSection(int section) {
        int i = 0;
        for (long val : events.keySet()) {
            if (i++ == section) return val;
        }
        return INVALID_TIME;
    }

    private void addSection(long time) {
        events.put(time, new ArrayList<CalendarEvent>());
    }

    private List<CalendarEvent> getEvents(long time) {
        if (!hasSection(time)) {
            addSection(time);
        }
        return events.get(time);
    }
}
