package org.challenge.calendar;

import dagger.Module;
import dagger.Provides;

@Module
public class CalendarFragmentModule {

    private final AgendaDataSourceModule agendaDataSourceModule;

    public CalendarFragmentModule() {
        agendaDataSourceModule = new AgendaDataSourceModule();
    }

    @Provides
    public AgendaDataSource provideAgendaDataSource() {
        return new AgendaDataSource(agendaDataSourceModule.provideCalendar(),
                agendaDataSourceModule.provideSectionHeaderFormatter(),
                agendaDataSourceModule.provideEventTimeFormatter());
    }
}
