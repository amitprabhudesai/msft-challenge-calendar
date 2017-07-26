package org.challenge.calendar;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class AgendaDataSourceTest {
    AgendaDataSource agendaDataSource;

    @Before
    public void setUp() {
        CalendarDataStoreTestComponent component =
                DaggerCalendarDataStoreTestComponent.builder()
                        .agendaDataSourceTestModule(new AgendaDataSourceTestModule())
                        .build();
        this.agendaDataSource = component.agendaDataSource();
    }

    @Test
    public void testGetSectionCount() {
        assertTrue(0 == agendaDataSource.getSectionCount());
    }
}
