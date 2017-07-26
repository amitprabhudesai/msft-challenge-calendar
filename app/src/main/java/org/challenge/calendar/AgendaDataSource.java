package org.challenge.calendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * The data source that feeds the adapter.
 * This maintains all the sections in the AgendaView
 * with the events in those sections.
 */
public final class AgendaDataSource {

    @Inject Calendar calendar;
    @Inject
    @Named("sectionHeaderFormatter")
    SimpleDateFormat sectionHeaderFormatter;
    @Named("eventTimeFormatter")
    @Inject SimpleDateFormat eventTimeFormatter;

    // Maintain a list of events keyed by the day represented
    // as time in millis since epoch. This is done so we can
    // easily compute rank()/floor()/ceil() queries.

    // For display, we use appropriate {@link SimpleDateFormat}
    // formatters to convert from the this to the display text
    // Vis-a-vis the AgendaView, the keys represent the sections,
    // while the values (list of events) represent the collection
    // of events in that section
    @Inject
    Map<Long, List<CalendarEvent>> events;

    static final long INVALID_TIME = Long.MIN_VALUE;

    public AgendaDataSource(Calendar calendar,
                            SimpleDateFormat sectionHeaderFormatter,
                            SimpleDateFormat eventTimeFormatter) {
        this.calendar = calendar;
        this.sectionHeaderFormatter = sectionHeaderFormatter;
        this.eventTimeFormatter = eventTimeFormatter;
        this.events = new LinkedHashMap<>();
    }

    public AgendaDataSource(Calendar calendar,
                            SimpleDateFormat sectionHeaderFormatter,
                            SimpleDateFormat eventTimeFormatter,
                            Map<Long, List<CalendarEvent>> events) {
        this.calendar = calendar;
        this.sectionHeaderFormatter = sectionHeaderFormatter;
        this.eventTimeFormatter = eventTimeFormatter;
        this.events = new LinkedHashMap<>(events);
    }

    /**
     * Clear all data.
     */
    public void clear() {
        events.clear();
    }

    /**
     * Get the {@link Calendar} that this data source was created with.
     * @return
     */
    public Calendar getCalendar() {
        return calendar;
    }

    /**
     * Get the section formatter (an {@link java.text.DateFormat} instance)
     * that this data source was created with.
     * @return
     */
    public SimpleDateFormat getSectionHeaderFormatter() {
        return sectionHeaderFormatter;
    }

    /**
     * Get the event time formatter (an {@link java.text.DateFormat} instance)
     * that this data source was created with.
     * @return
     */
    public SimpleDateFormat getEventTimeFormatter() {
        return eventTimeFormatter;
    }

    /**
     * Add a {@link CalendarEvent}.
     * @param time  the day the event begins, in millis since epoch
     * @param event  the {@link CalendarEvent}. Must be non-{@code null}
     */
    public void addEvent(long time, CalendarEvent event) {
        getEvents(time).add(event);
    }

    /**
     * Get the section corresponding to the first event that occurs
     * no earlier than the specified time.
     * Useful in scrolling to the correct date in the agenda when the
     * user selects a date in the Calendar View.
     * @param time
     * @return
     */
    public int getSectionCeil(long time) {
        int ceil = 0;
        boolean stop = false;
        for (Map.Entry<Long, List<CalendarEvent>> entry : events.entrySet()) {
            for (CalendarEvent event : entry.getValue()) {
                if (event.getBeginTime() >= time) {
                    stop = true;
                    break;
                }
            }
            if (stop) break;
            ceil++;
        }
        return ceil;
    }

    /**
     * Get the section corresponding to the last event that occurs
     * no later than the specified time.
     * Useful in scrolling to the correct date in the agenda when
     * the app starts.
     * @param time
     * @return
     */
    public int getSectionFloor(long time) {
        int floor = 0;
        boolean stop = false;
        for (Map.Entry<Long, List<CalendarEvent>> entry : events.entrySet()) {
            for (CalendarEvent event : entry.getValue()) {
                if (event.getBeginTime() > time) {
                    stop = true;
                    break;
                }
            }
            if (stop) break;
            floor++;
        }
        return floor;
    }

    /**
     * Get the number of sections.
     * @return
     */
    public int getSectionCount() {
        return events.size();
    }

    /**
     * Get the number of events in this section
     * @param section
     * @return
     */
    public int getEventCount(int section) {
        long time = getTime(section);
        if (INVALID_TIME == time) return 0;
        return events.get(time).size();
    }

    /**
     * Get the time in millis since epoch for this section
     * @param section
     * @return
     */
    public long getTime(int section) {
        return section <= getSectionCount() ? fromSection(section) : INVALID_TIME;
    }

    /**
     * Format the section header for display.
     * @param section
     * @return
     */
    public String getHeaderAsDisplayText(int section) {
        calendar.setTimeInMillis(getTime(section));
        return sectionHeaderFormatter.format(calendar.getTime());
    }

    /**
     * Get the {@link CalendarEvent} at the specified offset in a
     * given section.
     * @param section
     * @param position
     * @return
     */
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
