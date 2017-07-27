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
    public void testClear() {
        agendaDataSource.clear();
        assertTrue(0 == agendaDataSource.getSectionCount());
    }

    @Test
    public void testAddEvent_ExistingSection() {
        fail("Not implemented yet!");
    }

    @Test
    public void testAddEvent_NewSection() {
        fail("Not implemented yet!");
    }

    @Test
    public void testGetSectionCeil() {
        fail("Not implemented yet!");
    }

    @Test
    public void testGetSectionFloor() {
        fail("Not implemented yet!");
    }

    @Test
    public void testGetSectionCount() {
        assertTrue(0 == agendaDataSource.getSectionCount());
    }

    @Test
    public void testGetEventCount_ValidSectionIndex() {
        fail("Not implemented yet!");
    }

    @Test
    public void testGetEventCount_InvalidSectionIndex() {
        fail("Not implemented yet!");
    }

    @Test
    public void testGetTime_ValidSectionIndex() {
        fail("Not implemented yet!");
    }

    @Test
    public void testGetTime_InvalidSectionIndex() {
        fail("Not implemented yet!");
    }

    @Test
    public void testGetEventItem_ValidSectionIndexAndOffset() {
        fail("Not implemented yet!");
    }

    @Test
    public void testGetEventItem_InvalidSectionIndex() {
        fail("Not implemented yet!");
    }

    @Test
    public void testGetEventItem_InvalidOffsetInSection() {
        fail("Not implemented yet!");
    }
}
