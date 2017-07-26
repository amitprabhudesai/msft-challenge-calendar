package org.challenge.calendar;

import dagger.Component;

@Component(modules = {AgendaDataSourceTestModule.class})
public interface CalendarDataStoreTestComponent {
    AgendaDataSource agendaDataSource();
}
