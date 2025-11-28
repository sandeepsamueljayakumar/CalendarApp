package edu.northeastern.calendarapp.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for SimpleEvent class.
 */
class SimpleEventTest {

  private LocalDate testDate;
  private LocalTime testTime;

  @BeforeEach
  void setUp() {
    testDate = LocalDate.of(2025, 11, 15);
    testTime = LocalTime.of(14, 30);
  }

  @Test
  void testBuilderWithRequiredFields() {
    Event event = SimpleEvent.builder("Meeting", testDate).build();

    assertEquals("Meeting", event.getSubject());
    assertEquals(testDate, event.getStartDate());
    assertTrue(event.isAllDay());
    assertTrue(event.getStartTime().isEmpty());
    assertEquals(Visibility.PUBLIC, event.getVisibility());
  }

  @Test
  void testBuilderWithAllFields() {
    Event event = SimpleEvent.builder("Conference", testDate)
        .withStartTime(testTime)
        .withEndDate(testDate.plusDays(1))
        .withEndTime(LocalTime.of(16, 30))
        .withVisibility(Visibility.PRIVATE)
        .withDescription("Annual conference")
        .withLocation("Room 101")
        .build();

    assertEquals("Conference", event.getSubject());
    assertEquals(testDate, event.getStartDate());
    assertEquals(testTime, event.getStartTime().orElse(null));
    assertEquals(testDate.plusDays(1), event.getEndDate().orElse(null));
    assertEquals(LocalTime.of(16, 30), event.getEndTime().orElse(null));
    assertEquals(Visibility.PRIVATE, event.getVisibility());
    assertEquals("Annual conference", event.getDescription().orElse(null));
    assertEquals("Room 101", event.getLocation().orElse(null));
    assertFalse(event.isAllDay());
  }

  @Test
  void testNullSubjectThrows() {
    assertThrows(IllegalArgumentException.class, () ->
        SimpleEvent.builder(null, testDate).build()
    );
  }

  @Test
  void testEmptySubjectThrows() {
    assertThrows(IllegalArgumentException.class, () ->
        SimpleEvent.builder("  ", testDate).build()
    );
  }

  @Test
  void testNullStartDateThrows() {
    assertThrows(NullPointerException.class, () ->
        SimpleEvent.builder("Meeting", null).build()
    );
  }

  @Test
  void testEndTimeWithoutStartTimeThrows() {
    assertThrows(IllegalArgumentException.class, () ->
        SimpleEvent.builder("Meeting", testDate)
            .withEndTime(LocalTime.of(15, 0))
            .build()
    );
  }

  @Test
  void testEndBeforeStartThrows() {
    assertThrows(IllegalArgumentException.class, () ->
        SimpleEvent.builder("Meeting", testDate)
            .withStartTime(LocalTime.of(15, 0))
            .withEndTime(LocalTime.of(14, 0))
            .build()
    );
  }

  @Test
  void testAllDayEventTimes() {
    Event event = SimpleEvent.builder("Holiday", testDate).build();

    assertTrue(event.isAllDay());
    assertEquals(testDate.atStartOfDay(), event.getStartDateTime());
    assertEquals(testDate.atTime(23, 59, 59), event.getEndDateTime());
  }

  @Test
  void testTimedEventWithoutEndTime() {
    Event event = SimpleEvent.builder("Meeting", testDate)
        .withStartTime(testTime)
        .build();

    assertFalse(event.isAllDay());
    assertEquals(LocalDateTime.of(testDate, testTime), event.getStartDateTime());
    assertEquals(LocalDateTime.of(testDate, testTime), event.getEndDateTime());
  }

  @Test
  void testMultiDayEvent() {
    LocalDate endDate = testDate.plusDays(2);
    Event event = SimpleEvent.builder("Conference", testDate)
        .withStartTime(LocalTime.of(9, 0))
        .withEndDate(endDate)
        .withEndTime(LocalTime.of(17, 0))
        .build();

    assertEquals(LocalDateTime.of(testDate, LocalTime.of(9, 0)),
        event.getStartDateTime());
    assertEquals(LocalDateTime.of(endDate, LocalTime.of(17, 0)),
        event.getEndDateTime());
  }

  @ParameterizedTest
  @CsvSource({
      "true, true, true",    // Both all-day, same day
      "true, false, true",   // One all-day, overlapping
      "false, false, true",  // Both timed, overlapping
      "false, false, false"  // Both timed, not overlapping
  })
  void testConflictDetection(boolean firstAllDay, boolean secondAllDay, boolean shouldConflict) {
    Event event1;
    Event event2;

    if (firstAllDay) {
      event1 = SimpleEvent.builder("Event1", testDate).build();
    } else {
      event1 = SimpleEvent.builder("Event1", testDate)
          .withStartTime(LocalTime.of(14, 0))
          .withEndTime(LocalTime.of(15, 0))
          .build();
    }

    if (secondAllDay) {
      event2 = SimpleEvent.builder("Event2", testDate).build();
    } else if (shouldConflict) {
      event2 = SimpleEvent.builder("Event2", testDate)
          .withStartTime(LocalTime.of(14, 30))
          .withEndTime(LocalTime.of(15, 30))
          .build();
    } else {
      event2 = SimpleEvent.builder("Event2", testDate)
          .withStartTime(LocalTime.of(16, 0))
          .withEndTime(LocalTime.of(17, 0))
          .build();
    }

    assertEquals(shouldConflict, event1.conflictsWith(event2));
    assertEquals(shouldConflict, event2.conflictsWith(event1));
  }

  @Test
  void testConflictsWithNull() {
    Event event = SimpleEvent.builder("Meeting", testDate).build();
    assertFalse(event.conflictsWith(null));
  }

  @Test
  void testEquals() {
    Event event1 = SimpleEvent.builder("Meeting", testDate)
        .withStartTime(testTime)
        .build();
    Event event2 = SimpleEvent.builder("Meeting", testDate)
        .withStartTime(testTime)
        .build();
    Event event3 = SimpleEvent.builder("Meeting", testDate)
        .withStartTime(testTime)
        .withDescription("Different description")
        .build();
    Event event4 = SimpleEvent.builder("Different", testDate)
        .withStartTime(testTime)
        .build();

    assertEquals(event1, event2);
    assertEquals(event1, event3); // Description doesn't affect equality
    assertNotEquals(event1, event4);
    assertNotEquals(event1, null);
    assertNotEquals(event1, "Not an event");
  }

  @Test
  void testHashCode() {
    Event event1 = SimpleEvent.builder("Meeting", testDate)
        .withStartTime(testTime)
        .build();
    Event event2 = SimpleEvent.builder("Meeting", testDate)
        .withStartTime(testTime)
        .withDescription("Different")
        .build();

    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  void testToString() {
    Event allDayEvent = SimpleEvent.builder("Holiday", testDate).build();
    Event timedEvent = SimpleEvent.builder("Meeting", testDate)
        .withStartTime(testTime)
        .withEndTime(LocalTime.of(16, 0))
        .build();

    assertTrue(allDayEvent.toString().contains("Holiday"));
    assertTrue(allDayEvent.toString().contains(testDate.toString()));

    assertTrue(timedEvent.toString().contains("Meeting"));
    assertTrue(timedEvent.toString().contains(testTime.toString()));
  }

  @Test
  void testSubjectTrimming() {
    Event event = SimpleEvent.builder("  Meeting  ", testDate).build();
    assertEquals("Meeting", event.getSubject());
  }

  @Test
  void testDefaultEndDateWhenStartTimeProvided() {
    Event event = SimpleEvent.builder("Meeting", testDate)
        .withStartTime(testTime)
        .build();

    assertEquals(testDate, event.getEndDate().orElse(null));
  }
}