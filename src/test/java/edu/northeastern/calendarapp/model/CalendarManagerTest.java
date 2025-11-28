package edu.northeastern.calendarapp.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for CalendarManager save/restore functionality.
 * AI-generated code for CalendarManager testing.
 */
class CalendarManagerTest {

    private static final String TEST_FILE = "test_calendars.txt";

    @AfterEach
    void cleanup() {
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testSaveAndRestoreEmptyCalendars() throws IOException {
        List<Calendar> calendars = new ArrayList<>();
        calendars.add(new SimpleCalendar("Calendar 1"));
        calendars.add(new SimpleCalendar("Calendar 2"));

        CalendarManager.saveAllCalendars(calendars, TEST_FILE);

        List<Calendar> restored = CalendarManager.restoreAllCalendars(TEST_FILE);

        assertEquals(2, restored.size());
        assertEquals("Calendar 1", restored.get(0).getTitle());
        assertEquals("Calendar 2", restored.get(1).getTitle());
    }

    @Test
    void testSaveAndRestoreCalendarsWithEvents() throws IOException {
        List<Calendar> calendars = new ArrayList<>();

        SimpleCalendar calendar1 = new SimpleCalendar("Work");
        calendar1.addEvent(SimpleEvent.builder("Meeting", LocalDate.of(2024, 1, 15))
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withDescription("Team meeting")
                .withLocation("Conference Room")
                .withVisibility(Visibility.PUBLIC)
                .build(), false);

        SimpleCalendar calendar2 = new SimpleCalendar("Personal");
        calendar2.addEvent(SimpleEvent.builder("Dentist", LocalDate.of(2024, 1, 16))
                .withStartTime(LocalTime.of(14, 0))
                .withEndTime(LocalTime.of(15, 0))
                .withLocation("Downtown Clinic")
                .withVisibility(Visibility.PRIVATE)
                .build(), false);

        calendars.add(calendar1);
        calendars.add(calendar2);

        CalendarManager.saveAllCalendars(calendars, TEST_FILE);

        List<Calendar> restored = CalendarManager.restoreAllCalendars(TEST_FILE);

        assertEquals(2, restored.size());
        assertEquals(1, restored.get(0).getAllEvents().size());
        assertEquals(1, restored.get(1).getAllEvents().size());
        assertEquals("Meeting", restored.get(0).getAllEvents().get(0).getSubject());
        assertEquals("Dentist", restored.get(1).getAllEvents().get(0).getSubject());
    }

    @Test
    void testSaveEmptyList() throws IOException {
        List<Calendar> calendars = new ArrayList<>();
        CalendarManager.saveAllCalendars(calendars, TEST_FILE);

        List<Calendar> restored = CalendarManager.restoreAllCalendars(TEST_FILE);
        assertEquals(0, restored.size());
    }

    @Test
    void testRestoreNonExistentFile() {
        assertThrows(IOException.class, () ->
                CalendarManager.restoreAllCalendars("nonexistent_file.txt"));
    }
}