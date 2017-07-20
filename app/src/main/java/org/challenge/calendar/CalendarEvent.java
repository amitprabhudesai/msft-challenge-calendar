package org.challenge.calendar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A single instance of an event in the Agenda View.
 */
public class CalendarEvent {

    private final int id;
    private final int calId;
    private final String title;
    private final String location;
    private final boolean allDay;
    // The begin and end times are stored as millis
    // to be formatted as date/time for display
    private final long beginTime;
    private long endTime;

    public CalendarEvent(final int id,
                         final int calId,
                         final String title,
                         final long beginTime,
                         final String location,
                         final int allDay) {
        this.id = id;
        this.calId = calId;
        this.title = title;
        this.beginTime = beginTime;
        this.location = location;
        this.allDay = 1 == allDay;
    }

    /**
     *
     * @return the event ID
     */
    public final int getId() {
        return id;
    }

    /**
     *
     * @return the ID of the calendar that this event belongs to
     */
    public final int getCalendarId() {
        return calId;
    }

    /**
     * Get the event title.
     * @return
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Get the event location.
     * @return
     */
    public final String getLocation() {
        return location;
    }

    /**
     * Get the event begin time, as millis since epoch.
     * @return
     */
    public final long getBeginTime() {
        return beginTime;
    }

    /**
     * Get the event end time, as millis since epoch.
     * @return
     */
    public final long getEndTime() {
        return endTime;
    }

    /**
     * Set the event end time as millis since epoch.
     * @param end
     */
    public final void setEndTime(final long end) {
        this.endTime = end;
    }

    /**
     *
     * @return {@code true} if this is an all day event.
     */
    public final boolean isAllDay() {
        return allDay;
    }

    @Override
    public String toString() {
        String result = "";
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("calId", calId);
            json.put("title", title);
            json.put("location", location);
            json.put("allDay", allDay);
            json.put("begin", beginTime);
            json.put("end", endTime);
        } catch (JSONException e) {
            // ignore
        } finally {
            result = json.toString();
        }
        return result;
    }

    /**
     * Formatter to format a {@link CalendarEvent} for display.
     */
    static final class Formatter {
        private final Calendar calendar;
        private final SimpleDateFormat formatter;

        public Formatter(Calendar calendar, SimpleDateFormat formatter) {
            this.calendar = calendar;
            this.formatter = formatter;
        }

        public String beginTimeAsDisplayText(CalendarEvent event) {
            calendar.setTimeInMillis(event.getBeginTime());
            return formatter.format(calendar.getTime());
        }

        public String endTimeAsDisplayText(CalendarEvent event) {
            calendar.setTimeInMillis(event.getEndTime());
            return formatter.format(calendar.getTime());
        }
    }
}
