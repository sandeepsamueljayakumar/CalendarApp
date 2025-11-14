package edu.northeastern.calendarapp.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a recurrence pattern for recurring events.
 */
public class RecurrencePattern {
  private final Set<DayOfWeek> daysOfWeek;
  private final LocalDate endDate;
  private final Integer occurrences;

  /**
   * Creates a recurrence pattern that ends on a specific date.
   *
   * @param daysOfWeek the days of the week when the event recurs
   * @param endDate the end date for the recurrence
   */
  public RecurrencePattern(Set<DayOfWeek> daysOfWeek, LocalDate endDate) {
    if (daysOfWeek == null || daysOfWeek.isEmpty()) {
      throw new IllegalArgumentException("Days of week cannot be null or empty");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date cannot be null");
    }
    this.daysOfWeek = Set.copyOf(daysOfWeek);
    this.endDate = endDate;
    this.occurrences = null;
  }

  /**
   * Creates a recurrence pattern with a specific number of occurrences.
   *
   * @param daysOfWeek the days of the week when the event recurs
   * @param occurrences the number of times the event should occur
   */
  public RecurrencePattern(Set<DayOfWeek> daysOfWeek, int occurrences) {
    if (daysOfWeek == null || daysOfWeek.isEmpty()) {
      throw new IllegalArgumentException("Days of week cannot be null or empty");
    }
    if (occurrences <= 0) {
      throw new IllegalArgumentException("Occurrences must be positive");
    }
    this.daysOfWeek = Set.copyOf(daysOfWeek);
    this.endDate = null;
    this.occurrences = occurrences;
  }

  /**
   * Gets the days of the week for this recurrence pattern.
   *
   * @return the days of the week
   */
  public Set<DayOfWeek> getDaysOfWeek() {
    return daysOfWeek;
  }

  /**
   * Gets the end date for this recurrence pattern.
   *
   * @return the end date, or null if using occurrences
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Gets the number of occurrences for this recurrence pattern.
   *
   * @return the number of occurrences, or null if using occurrences
   */
  public Integer getOccurrences() {
    return occurrences;
  }

  /**
   * Checks if this pattern uses an end date.
   *
   * @return true if using end date, false if using occurrences
   */
  public boolean usesEndDate() {
    return endDate != null;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RecurrencePattern)) {
      return false;
    }
    RecurrencePattern other = (RecurrencePattern) obj;
    return Objects.equals(daysOfWeek, other.daysOfWeek)
        && Objects.equals(endDate, other.endDate)
        && Objects.equals(occurrences, other.occurrences);
  }

  @Override
  public int hashCode() {
    return Objects.hash(daysOfWeek, endDate, occurrences);
  }

  @Override
  public String toString() {
    if (endDate != null) {
      return String.format("RecurrencePattern{days=%s, until=%s}", daysOfWeek, endDate);
    } else {
      return String.format("RecurrencePattern{days=%s, occurrences=%d}", daysOfWeek, occurrences);
    }
  }
}