package org.challenge.calendar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * A calendar event.
 */
public class CalendarEvent {
    private final long id;
    private final long calId;
    private final String title;
    private final String location;
    private final boolean allDay;

    private String beginTime;
    private String endTime;

    public CalendarEvent(final long id,
                         final long calId,
                         final String title,
                         final String location,
                         final int allDay) {
        this.id = id;
        this.calId = calId;
        this.title = title;
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

    public final String getBeginTime() {
        return beginTime;
    }

    public final void setBeginTime(final String begin) {
        this.beginTime = begin;
    }

    public final String getEndTime() {
        return endTime;
    }

    public final void setEndTime(final String end) {
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
}
