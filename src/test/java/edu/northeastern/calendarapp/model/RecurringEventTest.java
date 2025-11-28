package edu.northeastern.calendarapp.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for RecurringEvent class.
 */
class RecurringEventTest {

  private LocalDate startDate;
  private RecurrencePattern weekdayPattern;
  private RecurrencePattern limitedPattern;

  @BeforeEach
  void setUp() {
    startDate = LocalDate.of(2025, 11, 17); // Monday

    // Pattern for weekdays until end of month
    weekdayPattern = new RecurrencePattern(
        Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
        LocalDate.of(2025, 11, 30)
    );

    // Pattern for 5 occurrences
    limitedPattern = new RecurrencePattern(
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        5
    );
  }

  @Test
  void testCreateRecurringEventWithEndDate() {
    RecurringEvent event = RecurringEvent.builder("Daily Standup", startDate, weekdayPattern)
        .withStartTime(LocalTime.of(9, 0))
        .withEndTime(LocalTime.of(9, 15))
        .withLocation("Conference Room")
        .build();

    assertNotNull(event);
    assertEquals("Daily Standup", event.getSubject());
    assertEquals(startDate, event.getStartDate());
    assertTrue(event.getStartTime().isPresent());
    assertEquals(LocalTime.of(9, 0), event.getStartTime().get());

    List<Event> occurrences = event.getAllOccurrences();
    assertFalse(occurrences.isEmpty());

    // Should have events for each weekday
    assertTrue(occurrences.size() > 5);

    // All occurrences should have same time
    for (Event occurrence : occurrences) {
      assertEquals(LocalTime.of(9, 0), occurrence.getStartTime().get());
      assertEquals(LocalTime.of(9, 15), occurrence.getEndTime().get());
    }
  }

  @Test
  void testCreateRecurringEventWithOccurrences() {
    RecurringEvent event = RecurringEvent.builder("Team Meeting", startDate, limitedPattern)
        .withStartTime(LocalTime.of(14, 0))
        .withEndTime(LocalTime.of(15, 0))
        .build();

    List<Event> occurrences = event.getAllOccurrences();
    assertEquals(5, occurrences.size());
  }

  @Test
  void testRecurringAllDayEvent() {
    RecurrencePattern pattern = new RecurrencePattern(
        Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
        4
    );

    RecurringEvent event = RecurringEvent.builder("Weekend",
            LocalDate.of(2025, 11, 15), pattern)
        .withDescription("Weekend relaxation")
        .build();

    assertTrue(event.isAllDay());
    List<Event> occurrences = event.getAllOccurrences();
    assertEquals(4, occurrences.size());

    for (Event occurrence : occurrences) {
      assertTrue(occurrence.isAllDay());
    }
  }

  @Test
  void testModifySingleOccurrence() {
    RecurringEvent event = RecurringEvent.builder("Daily Meeting", startDate, weekdayPattern)
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(11, 0))
        .build();

    List<Event> originalOccurrences = event.getAllOccurrences();
    LocalDate modifyDate = originalOccurrences.get(2).getStartDate();

    Event modified = SimpleEvent.builder("Daily Meeting - Cancelled", modifyDate)
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(11, 0))
        .withDescription("Cancelled due to holiday")
        .build();

    event.modifySingleOccurrence(modifyDate, modified);

    List<Event> newOccurrences = event.getAllOccurrences();
    assertEquals(originalOccurrences.size(), newOccurrences.size());

    // Find the modified occurrence
    boolean foundModified = false;
    for (Event occurrence : newOccurrences) {
      if (occurrence.getStartDate().equals(modifyDate)) {
        assertEquals("Daily Meeting - Cancelled", occurrence.getSubject());
        assertTrue(occurrence.getDescription().isPresent());
        foundModified = true;
      }
    }
    assertTrue(foundModified);
  }

  @Test
  void testModifyFromDate() {
    RecurringEvent event = RecurringEvent.builder("Weekly Review", startDate, weekdayPattern)
        .withStartTime(LocalTime.of(16, 0))
        .withEndTime(LocalTime.of(17, 0))
        .build();

    LocalDate fromDate = startDate.plusDays(7);

    Event template = SimpleEvent.builder("Weekly Review - Updated", fromDate)
        .withStartTime(LocalTime.of(15, 0))
        .withEndTime(LocalTime.of(16, 0))
        .withLocation("New Room")
        .build();

    event.modifyFromDate(fromDate, template);

    List<Event> occurrences = event.getAllOccurrences();

    for (Event occurrence : occurrences) {
      if (!occurrence.getStartDate().isBefore(fromDate)) {
        assertEquals("Weekly Review - Updated", occurrence.getSubject());
        assertEquals(LocalTime.of(15, 0), occurrence.getStartTime().get());
        assertEquals("New Room", occurrence.getLocation().get());
      } else {
        assertEquals("Weekly Review", occurrence.getSubject());
        assertEquals(LocalTime.of(16, 0), occurrence.getStartTime().get());
      }
    }
  }

  @Test
  void testNullSubjectThrows() {
    assertThrows(IllegalArgumentException.class, () ->
        RecurringEvent.builder(null, startDate, weekdayPattern).build()
    );
  }

  @Test
  void testNullStartDateThrows() {
    assertThrows(NullPointerException.class, () ->
        RecurringEvent.builder("Event", null, weekdayPattern).build()
    );
  }

  @Test
  void testNullPatternThrows() {
    assertThrows(NullPointerException.class, () ->
        RecurringEvent.builder("Event", startDate, null).build()
    );
  }

  @Test
  void testRecurringEventRequiresEndTimeIfStartTimeProvided() {
    assertThrows(IllegalArgumentException.class, () ->
        RecurringEvent.builder("Event", startDate, weekdayPattern)
            .withStartTime(LocalTime.of(10, 0))
            .build()
    );
  }

  @Test
  void testConflictDetection() {
    RecurringEvent recurring = RecurringEvent.builder("Recurring", startDate, limitedPattern)
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(11, 0))
        .build();

    // Get first occurrence date
    LocalDate firstOccurrenceDate = recurring.getAllOccurrences().get(0).getStartDate();

    Event conflicting = SimpleEvent.builder("Single", firstOccurrenceDate)
        .withStartTime(LocalTime.of(10, 30))
        .withEndTime(LocalTime.of(11, 30))
        .build();

    assertTrue(recurring.conflictsWith(conflicting));
  }

  @Test
  void testEquals() {
    RecurringEvent event1 = RecurringEvent.builder("Meeting", startDate, weekdayPattern)
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(11, 0))
        .build();

    RecurringEvent event2 = RecurringEvent.builder("Meeting", startDate, weekdayPattern)
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(11, 0))
        .build();

    assertEquals(event1, event2);
  }
}