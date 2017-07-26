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

@Module(includes = AgendaDataSourceModule.class)
class MockAgendaDataSourceModule {

    private final SimpleDateFormat sectionHeaderFormatter;
    private final SimpleDateFormat eventTimeFormatter;
    private final Map<Long, List<CalendarEvent>> eventsForTesting;

    public MockAgendaDataSourceModule() {
        this.sectionHeaderFormatter = new SimpleDateFormat("EEE, d MMM", Locale.US);
        this.eventTimeFormatter = new SimpleDateFormat("HH:mm", Locale.US);
        this.eventsForTesting = new LinkedHashMap<>();
        //TODO(amit.prabhudesai) Populate events for testing
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
        return eventsForTesting;
    }
}
