package edu.northeastern.calendarapp.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for SimpleCalendar class.
 */
class SimpleCalendarTest {

  private SimpleCalendar calendar;
  private LocalDate testDate;
  private Event testEvent;

  @BeforeEach
  void setUp() {
    calendar = new SimpleCalendar("Test Calendar");
    testDate = LocalDate.of(2025, 11, 15);
    testEvent = SimpleEvent.builder("Meeting", testDate)
        .withStartTime(LocalTime.of(14, 0))
        .withEndTime(LocalTime.of(15, 0))
        .build();
  }

  @Test
  void testConstructorWithTitle() {
    assertEquals("Test Calendar", calendar.getTitle());
    assertTrue(calendar.getAllEvents().isEmpty());
  }

  @Test
  void testConstructorWithNullTitle() {
    assertThrows(IllegalArgumentException.class,
        () -> new SimpleCalendar(null));
  }

  @Test
  void testConstructorWithEmptyTitle() {
    assertThrows(IllegalArgumentException.class,
        () -> new SimpleCalendar("  "));
  }

  @Test
  void testAddEvent() {
    assertTrue(calendar.addEvent(testEvent, false));
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  void testAddNullEvent() {
    assertThrows(NullPointerException.class,
        () -> calendar.addEvent(null, false));
  }

  @Test
  void testAddDuplicateEvent() {
    calendar.addEvent(testEvent, false);

    Event duplicate = SimpleEvent.builder("Meeting", testDate)
        .withStartTime(LocalTime.of(14, 0))
        .withDescription("Different description")
        .build();

    assertThrows(IllegalArgumentException.class,
        () -> calendar.addEvent(duplicate, false));
  }

  @Test
  void testAddConflictingEventNotAllowed() {
    calendar.addEvent(testEvent, false);

    Event conflicting = SimpleEvent.builder("Another Meeting", testDate)
        .withStartTime(LocalTime.of(14, 30))
        .withEndTime(LocalTime.of(15, 30))
        .build();

    assertFalse(calendar.addEvent(conflicting, false));
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  void testAddConflictingEventAllowed() {
    calendar.addEvent(testEvent, true);

    Event conflicting = SimpleEvent.builder("Another Meeting", testDate)
        .withStartTime(LocalTime.of(14, 30))
        .withEndTime(LocalTime.of(15, 30))
        .build();

    assertTrue(calendar.addEvent(conflicting, true));
    assertEquals(2, calendar.getAllEvents().size());
  }

  @Test
  void testGetEvent() {
    calendar.addEvent(testEvent, false);

    Optional<Event> found = calendar.getEvent("Meeting", testDate,
        LocalDateTime.of(testDate, LocalTime.of(14, 0)));

    assertTrue(found.isPresent());
    assertEquals(testEvent, found.get());
  }

  @Test
  void testGetEventNotFound() {
    Optional<Event> found = calendar.getEvent("Not exist", testDate, null);
    assertTrue(found.isEmpty());
  }

  @Test
  void testGetEventsOnDate() {
    Event allDay = SimpleEvent.builder("Holiday", testDate).build();
    Event morning = SimpleEvent.builder("Morning", testDate)
        .withStartTime(LocalTime.of(9, 0))
        .build();
    Event multiDay = SimpleEvent.builder("Conference", testDate.minusDays(1))
        .withStartTime(LocalTime.of(9, 0))
        .withEndDate(testDate.plusDays(1))
        .withEndTime(LocalTime.of(17, 0))
        .build();

    calendar.addEvent(allDay, true);
    calendar.addEvent(morning, true);
    calendar.addEvent(multiDay, true);

    List<Event> eventsOnDate = calendar.getEventsOnDate(testDate);
    assertEquals(3, eventsOnDate.size());
  }

  @Test
  void testGetEventsInRange() {
    LocalDate start = LocalDate.of(2025, 11, 10);
    LocalDate end = LocalDate.of(2025, 11, 20);

    Event before = SimpleEvent.builder("Before", start.minusDays(5)).build();
    Event during1 = SimpleEvent.builder("During1", start.plusDays(2)).build();
    Event during2 = SimpleEvent.builder("During2", end.minusDays(2)).build();
    Event after = SimpleEvent.builder("After", end.plusDays(5)).build();

    calendar.addEvent(before, true);
    calendar.addEvent(during1, true);
    calendar.addEvent(during2, true);
    calendar.addEvent(after, true);

    List<Event> inRange = calendar.getEventsInRange(start, end);
    assertEquals(2, inRange.size());
    assertTrue(inRange.contains(during1));
    assertTrue(inRange.contains(during2));
  }

  @Test
  void testGetEventsInRangeInvalidDates() {
    assertThrows(IllegalArgumentException.class,
        () -> calendar.getEventsInRange(testDate, testDate.minusDays(1)));
  }

  @Test
  void testIsBusyAt() {
    calendar.addEvent(testEvent, false);

    assertTrue(calendar.isBusyAt(LocalDateTime.of(testDate, LocalTime.of(14, 30))));
    assertFalse(calendar.isBusyAt(LocalDateTime.of(testDate, LocalTime.of(16, 0))));
  }

  @Test
  void testUpdateEvent() {
    calendar.addEvent(testEvent, false);

    Event updated = SimpleEvent.builder("Updated Meeting", testDate)
        .withStartTime(LocalTime.of(15, 0))
        .withEndTime(LocalTime.of(16, 0))
        .build();

    boolean result = calendar.updateEvent("Meeting", testDate,
        LocalDateTime.of(testDate, LocalTime.of(14, 0)), updated, false);

    assertTrue(result);
    assertEquals(1, calendar.getAllEvents().size());
    assertTrue(calendar.getEvent("Updated Meeting", testDate,
        LocalDateTime.of(testDate, LocalTime.of(15, 0))).isPresent());
  }

  @Test
  void testUpdateEventNotFound() {
    assertThrows(IllegalArgumentException.class,
        () -> calendar.updateEvent("Not exist", testDate, null, testEvent, false));
  }

  @Test
  void testUpdateEventCreatesDuplicate() {
    Event event1 = SimpleEvent.builder("Event1", testDate)
        .withStartTime(LocalTime.of(10, 0))
        .build();
    Event event2 = SimpleEvent.builder("Event2", testDate)
        .withStartTime(LocalTime.of(11, 0))
        .build();

    calendar.addEvent(event1, false);
    calendar.addEvent(event2, false);

    Event updated = SimpleEvent.builder("Event2", testDate)
        .withStartTime(LocalTime.of(11, 0))
        .build();

    assertThrows(IllegalArgumentException.class,
        () -> calendar.updateEvent("Event1", testDate,
            LocalDateTime.of(testDate, LocalTime.of(10, 0)), updated, false));
  }

  @Test
  void testUpdateEventWithConflict() {
    Event event1 = SimpleEvent.builder("Event1", testDate)
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(11, 0))
        .build();
    Event event2 = SimpleEvent.builder("Event2", testDate)
        .withStartTime(LocalTime.of(14, 0))
        .withEndTime(LocalTime.of(15, 0))
        .build();

    calendar.addEvent(event1, false);
    calendar.addEvent(event2, false);

    Event updated = SimpleEvent.builder("Event2 Updated", testDate)
        .withStartTime(LocalTime.of(10, 30))
        .withEndTime(LocalTime.of(11, 30))
        .build();

    assertFalse(calendar.updateEvent("Event2", testDate,
        LocalDateTime.of(testDate, LocalTime.of(14, 0)), updated, false));

    // Original event should still be there
    assertTrue(calendar.getEvent("Event2", testDate,
        LocalDateTime.of(testDate, LocalTime.of(14, 0))).isPresent());
  }

  @Test
  void testExportToCSV() {
    Event allDay = SimpleEvent.builder("Holiday", testDate)
        .withDescription("National holiday")
        .withVisibility(Visibility.PUBLIC)
        .build();

    Event meeting = SimpleEvent.builder("Team Meeting", testDate)
        .withStartTime(LocalTime.of(14, 0))
        .withEndTime(LocalTime.of(15, 30))
        .withLocation("Conference Room A")
        .withVisibility(Visibility.PRIVATE)
        .build();

    calendar.addEvent(allDay, false);
    calendar.addEvent(meeting, false);

    String csv = calendar.exportToCSV();

    assertTrue(csv.contains("Subject,Start Date,Start Time"));
    assertTrue(csv.contains("Holiday"));
    assertTrue(csv.contains("Team Meeting"));
    assertTrue(csv.contains("TRUE")); // All day event
    assertTrue(csv.contains("Conference Room A"));
  }

  @Test
  void testExportEmptyCalendar() {
    String csv = calendar.exportToCSV();
    assertTrue(csv.contains("Subject,Start Date"));
    assertEquals(2, csv.split("\n").length); // Header + empty line
  }

  @Test
  void testToString() {
    String str = calendar.toString();
    assertTrue(str.contains("Test Calendar"));
    assertTrue(str.contains("0 events"));

    calendar.addEvent(testEvent, false);
    str = calendar.toString();
    assertTrue(str.contains("1 events"));
  }
}