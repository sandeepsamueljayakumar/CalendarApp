package edu.northeastern.calendarapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a recurring calendar event.
 */
public class RecurringEvent implements Event {
  private final String subject;
  private final LocalDate startDate;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final Visibility visibility;
  private final String description;
  private final String location;
  private final RecurrencePattern pattern;
  private final List<LocalDate> occurrences;
  private final Map<LocalDate, Event> modifications;

  /**
   * Private constructor for use with the Builder.
   */
  private RecurringEvent(Builder builder) {
    // Validate required fields
    if (builder.subject == null || builder.subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject is required");
    }
    Objects.requireNonNull(builder.startDate, "Start date cannot be null");
    Objects.requireNonNull(builder.pattern, "Recurrence pattern cannot be null");

    // For recurring events, if start time exists, end time must exist and be on same day
    if (builder.startTime != null && builder.endTime == null) {
      throw new IllegalArgumentException("Recurring events with start time must have end time");
    }
    if (builder.startTime != null && builder.endTime != null
        && builder.endTime.isBefore(builder.startTime)) {
      throw new IllegalArgumentException("End time must be after start time");
    }

    this.subject = builder.subject.trim();
    this.startDate = builder.startDate;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.visibility = builder.visibility != null ? builder.visibility : Visibility.PUBLIC;
    this.description = builder.description;
    this.location = builder.location;
    this.pattern = builder.pattern;
    this.modifications = new HashMap<>();
    this.occurrences = generateOccurrences();
  }

  /**
   * Generates all occurrence dates based on the recurrence pattern.
   */
  private List<LocalDate> generateOccurrences() {
    List<LocalDate> dates = new ArrayList<>();
    LocalDate current = startDate;
    int count = 0;

    LocalDate endDate = pattern.usesEndDate()
        ? pattern.getEndDate()
        : startDate.plusYears(2); // Max 2 years for occurrence-based

    Integer maxOccurrences = pattern.getOccurrences();

    while (!current.isAfter(endDate)) {
      // Check if current date's day of week matches pattern
      java.time.DayOfWeek javaDayOfWeek = current.getDayOfWeek();
      DayOfWeek ourDayOfWeek = convertToOurDayOfWeek(javaDayOfWeek);

      if (pattern.getDaysOfWeek().contains(ourDayOfWeek)) {
        dates.add(current);
        count++;

        if (maxOccurrences != null && count >= maxOccurrences) {
          break;
        }
      }

      current = current.plusDays(1);
    }

    return dates;
  }

  /**
   * Converts Java's DayOfWeek to our DayOfWeek enum.
   */
  private DayOfWeek convertToOurDayOfWeek(java.time.DayOfWeek javaDayOfWeek) {
    return switch (javaDayOfWeek) {
      case MONDAY -> DayOfWeek.MONDAY;
      case TUESDAY -> DayOfWeek.TUESDAY;
      case WEDNESDAY -> DayOfWeek.WEDNESDAY;
      case THURSDAY -> DayOfWeek.THURSDAY;
      case FRIDAY -> DayOfWeek.FRIDAY;
      case SATURDAY -> DayOfWeek.SATURDAY;
      case SUNDAY -> DayOfWeek.SUNDAY;
    };
  }

  /**
   * Creates a new Builder for constructing RecurringEvent instances.
   */
  public static Builder builder(String subject, LocalDate startDate, RecurrencePattern pattern) {
    return new Builder(subject, startDate, pattern);
  }

  /**
   * Gets all individual event occurrences.
   */
  public List<Event> getAllOccurrences() {
    List<Event> events = new ArrayList<>();

    for (LocalDate date : occurrences) {
      // Check if this occurrence has been modified
      if (modifications.containsKey(date)) {
        events.add(modifications.get(date));
      } else {
        // Create a SimpleEvent for this occurrence
        SimpleEvent.Builder builder = SimpleEvent.builder(subject, date);

        if (startTime != null) {
          builder.withStartTime(startTime);
        }
        if (endTime != null) {
          builder.withEndTime(endTime);
        }
        builder.withVisibility(visibility);

        if (description != null) {
          builder.withDescription(description);
        }
        if (location != null) {
          builder.withLocation(location);
        }

        events.add(builder.build());
      }
    }

    return events;
  }

  /**
   * Modifies a single occurrence of the recurring event.
   */
  public void modifySingleOccurrence(LocalDate date, Event newEvent) {
    if (!occurrences.contains(date)) {
      throw new IllegalArgumentException("Date is not an occurrence of this recurring event");
    }
    modifications.put(date, newEvent);
  }

  /**
   * Modifies all occurrences from a specific date onwards.
   */
  public void modifyFromDate(LocalDate fromDate, Event template) {
    for (LocalDate date : occurrences) {
      if (!date.isBefore(fromDate)) {
        // Create a new event for this date based on the template
        SimpleEvent.Builder builder = SimpleEvent.builder(template.getSubject(), date);

        template.getStartTime().ifPresent(builder::withStartTime);
        template.getEndTime().ifPresent(builder::withEndTime);
        builder.withVisibility(template.getVisibility());
        template.getDescription().ifPresent(builder::withDescription);
        template.getLocation().ifPresent(builder::withLocation);

        modifications.put(date, builder.build());
      }
    }
  }

  // Implement Event interface methods (these apply to the first occurrence)
  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public LocalDate getStartDate() {
    return startDate;
  }

  @Override
  public Optional<LocalTime> getStartTime() {
    return Optional.ofNullable(startTime);
  }

  @Override
  public Optional<LocalDate> getEndDate() {
    // For recurring events, the end date is the same as start date (single day events)
    return Optional.of(startDate);
  }

  @Override
  public Optional<LocalTime> getEndTime() {
    return Optional.ofNullable(endTime);
  }

  @Override
  public Visibility getVisibility() {
    return visibility;
  }

  @Override
  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  @Override
  public Optional<String> getLocation() {
    return Optional.ofNullable(location);
  }

  @Override
  public boolean isAllDay() {
    return startTime == null;
  }

  @Override
  public LocalDateTime getStartDateTime() {
    if (isAllDay()) {
      return startDate.atStartOfDay();
    }
    return LocalDateTime.of(startDate, startTime);
  }

  @Override
  public LocalDateTime getEndDateTime() {
    if (isAllDay()) {
      return startDate.atTime(23, 59, 59);
    }
    return LocalDateTime.of(startDate, endTime != null ? endTime : startTime);
  }

  @Override
  public boolean conflictsWith(Event other) {
    // Check if any occurrence conflicts with the other event
    for (Event occurrence : getAllOccurrences()) {
      if (occurrence.conflictsWith(other)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RecurringEvent)) {
      return false;
    }
    RecurringEvent other = (RecurringEvent) obj;
    return Objects.equals(subject, other.subject)
        && Objects.equals(startDate, other.startDate)
        && Objects.equals(startTime, other.startTime)
        && Objects.equals(pattern, other.pattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDate, startTime, pattern);
  }

  @Override
  public String toString() {
    return String.format("RecurringEvent{subject='%s', startDate=%s, pattern=%s}",
        subject, startDate, pattern);
  }

  /**
   * Builder class for creating RecurringEvent instances.
   */
  public static class Builder {
    private final String subject;
    private final LocalDate startDate;
    private final RecurrencePattern pattern;
    private LocalTime startTime;
    private LocalTime endTime;
    private Visibility visibility;
    private String description;
    private String location;

    private Builder(String subject, LocalDate startDate, RecurrencePattern pattern) {
      this.subject = subject;
      this.startDate = startDate;
      this.pattern = pattern;
    }

    public Builder withStartTime(LocalTime startTime) {
      this.startTime = startTime;
      return this;
    }

    public Builder withEndTime(LocalTime endTime) {
      this.endTime = endTime;
      return this;
    }

    public Builder withVisibility(Visibility visibility) {
      this.visibility = visibility;
      return this;
    }

    public Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder withLocation(String location) {
      this.location = location;
      return this;
    }

    public RecurringEvent build() {
      return new RecurringEvent(this);
    }
  }
}