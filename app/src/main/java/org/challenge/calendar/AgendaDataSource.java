package org.challenge.calendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AgendaDataSource {

    private final Calendar calendar;
    private final SimpleDateFormat sectionHeaderFormatter;
    private final SimpleDateFormat eventTimeFormatter;
    private final List<Long> headers;
    private final Map<Long, List<CalendarEvent>> events;

    static final long INVALID_HEADER = Long.MIN_VALUE;

    public AgendaDataSource(Calendar calendar,
                            SimpleDateFormat sectionHeaderFormatter,
                            SimpleDateFormat eventTimeFormatter) {
        this.calendar = calendar;
        this.sectionHeaderFormatter = sectionHeaderFormatter;
        this.eventTimeFormatter = eventTimeFormatter;
        this.headers = new ArrayList<>();
        this.events = new HashMap<>();
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

    public void addItem(long header, CalendarEvent item) {
        getItems(header).add(item);
    }

    public int getHeaderCount() {
        return headers.size();
    }

    public int getEventCount(int section) {
        long header = getHeader(section);
        if (INVALID_HEADER == header) return 0;
        return events.get(header).size();
    }

    public long getHeader(int section) {
        return section <= headers.size() ? headers.get(section) : INVALID_HEADER;
    }

    public String getHeaderAsDisplayText(int section) {
        calendar.setTimeInMillis(getHeader(section));
        return sectionHeaderFormatter.format(calendar.getTime());
    }

    public CalendarEvent getEventItem(int section, int position) {
        return events.get(getHeader(section)).get(position);
    }

    private boolean hasHeader(long header) {
        return headers.contains(header);
    }

    private void addHeader(long header) {
        headers.add(header);
        events.put(header, new ArrayList<CalendarEvent>());
    }

    private List<CalendarEvent> getItems(long header) {
        if (!hasHeader(header)) {
            addHeader(header);
        }
        return events.get(header);
    }
}
