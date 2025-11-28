package edu.northeastern.calendarapp.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for CSV import/export functionality.
 * AI-generated code for import/export testing.
 */
class SimpleCalendarImportExportTest {

    private SimpleCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new SimpleCalendar("Test Calendar");
    }

    @Test
    void testExportEmptyCalendar() {
        String csv = calendar.exportToCSV();
        assertNotNull(csv);
        assertTrue(csv.startsWith("Subject,Start Date,Start Time"));
    }

    @Test
    void testExportSingleEvent() {
        Event event = SimpleEvent.builder("Test Event", LocalDate.of(2024, 1, 15))
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withDescription("Test description")
                .withLocation("Test location")
                .withVisibility(Visibility.PUBLIC)
                .build();

        calendar.addEvent(event, false);
        String csv = calendar.exportToCSV();

        assertTrue(csv.contains("Test Event"));
        assertTrue(csv.contains("01/15/2024"));
        assertTrue(csv.contains("10:00 AM"));
    }

    @Test
    void testImportSingleEvent() {
        String csv = "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n"
                + "Test Event,01/15/2024,10:00 AM,01/15/2024,11:00 AM,FALSE,Test description,Test location,FALSE\n";

        calendar.importFromCSV(csv);

        assertEquals(1, calendar.getAllEvents().size());
        Event imported = calendar.getAllEvents().get(0);
        assertEquals("Test Event", imported.getSubject());
        assertEquals(LocalDate.of(2024, 1, 15), imported.getStartDate());
    }

    @Test
    void testImportExportRoundTrip() {
        Event event1 = SimpleEvent.builder("Event 1", LocalDate.of(2024, 1, 15))
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withDescription("Description 1")
                .withLocation("Location 1")
                .withVisibility(Visibility.PUBLIC)
                .build();

        Event event2 = SimpleEvent.builder("Event 2", LocalDate.of(2024, 1, 16))
                .withStartTime(LocalTime.of(14, 0))
                .withEndTime(LocalTime.of(15, 0))
                .withDescription("Description 2")
                .withLocation("Location 2")
                .withVisibility(Visibility.PRIVATE)
                .build();

        calendar.addEvent(event1, false);
        calendar.addEvent(event2, false);

        String csv = calendar.exportToCSV();

        SimpleCalendar newCalendar = new SimpleCalendar("Imported Calendar");
        newCalendar.importFromCSV(csv);

        assertEquals(2, newCalendar.getAllEvents().size());
    }

    @Test
    void testImportAllDayEvent() {
        String csv = "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n"
                + "All Day Event,01/15/2024,,,,,TRUE,Test description,Test location,FALSE\n";

        calendar.importFromCSV(csv);

        assertEquals(1, calendar.getAllEvents().size());
        Event imported = calendar.getAllEvents().get(0);
        assertTrue(imported.isAllDay());
    }

    @Test
    void testImportWithQuotedFields() {
        String csv = "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n"
                + "\"Event, with comma\",01/15/2024,10:00 AM,01/15/2024,11:00 AM,FALSE,\"Description \"\"quoted\"\"\",Test location,FALSE\n";

        calendar.importFromCSV(csv);

        assertEquals(1, calendar.getAllEvents().size());
        Event imported = calendar.getAllEvents().get(0);
        assertEquals("Event, with comma", imported.getSubject());
    }

    @Test
    void testImportNullCSV() {
        assertThrows(NullPointerException.class, () -> calendar.importFromCSV(null));
    }

    @Test
    void testImportInvalidCSV() {
        String csv = "Not a valid header\nInvalid data line";
        assertThrows(IllegalArgumentException.class, () -> calendar.importFromCSV(csv));
    }
}