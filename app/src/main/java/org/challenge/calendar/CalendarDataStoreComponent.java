package org.challenge.calendar;

import dagger.Component;

@Component(modules = CalendarFragmentModule.class)
public interface CalendarDataStoreComponent {
    AgendaDataSource dataSource();
}
