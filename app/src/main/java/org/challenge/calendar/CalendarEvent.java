package org.challenge.calendar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A calendar event.
 */
public class CalendarEvent {
    private final long id;
    private final long calId;
    private final String title;
    private final String location;
    private final boolean allDay;
    // The begin and end times are stored as millis
    // to be formatted as date/time for display
    private final long beginTime;
    private long endTime;

    public CalendarEvent(final long id,
                         final long calId,
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

    public final long getId() {
        return id;
    }

    public final long getCalendarId() {
        return calId;
    }

    public final String getTitle() {
        return title;
    }

    public final String getLocation() {
        return location;
    }

    public final long getBeginTime() {
        return beginTime;
    }

    public final long getEndTime() {
        return endTime;
    }

    public final void setEndTime(final long end) {
        this.endTime = end;
    }

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
