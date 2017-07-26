package org.challenge.calendar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
class AgendaDataSourceModule {

    private SimpleDateFormat sectionHeaderFormatter;
    private SimpleDateFormat eventTimeFormatter;

    public AgendaDataSourceModule() {
        this.sectionHeaderFormatter = new SimpleDateFormat("EEE, d MMM", Locale.US);
        this.eventTimeFormatter = new SimpleDateFormat("HH:mm", Locale.US);
    }

    @Provides
    Calendar provideCalendar() {
        return Calendar.getInstance();
    }

    @Provides
    @Named("sectionHeaderFormatter")
    SimpleDateFormat provideSectionHeaderFormatter() {
        return sectionHeaderFormatter;
    }

    @Provides
    @Named("eventTimeFormatter")
    SimpleDateFormat provideEventTimeFormatter() {
        return eventTimeFormatter;
    }

    @Provides
    Map<Long, List<CalendarEvent>> provideEvents() {
        return new LinkedHashMap<>();
    }
}
