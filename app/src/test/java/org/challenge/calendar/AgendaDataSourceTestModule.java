package org.challenge.calendar;

import dagger.Module;
import dagger.Provides;

@Module
class AgendaDataSourceTestModule {
    private final MockAgendaDataSourceModule agendaDataSourceModule;

    public AgendaDataSourceTestModule() {
        agendaDataSourceModule = new MockAgendaDataSourceModule();
    }

    @Provides
    AgendaDataSource provideAgendaDataSource() {
        return new AgendaDataSource(agendaDataSourceModule.provideCalendar(),
                agendaDataSourceModule.provideSectionHeaderFormatter(),
                agendaDataSourceModule.provideEventTimeFormatter(),
                agendaDataSourceModule.provideEvents());
    }
}
