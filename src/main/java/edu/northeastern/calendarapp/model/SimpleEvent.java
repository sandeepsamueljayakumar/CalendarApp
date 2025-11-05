package edu.northeastern.calendarapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a single, non-recurring calendar event.
 */
public class SimpleEvent implements Event {
  private final String subject;
  private final LocalDate startDate;
  private final LocalTime startTime;
  private final LocalDate endDate;
  private final LocalTime endTime;
  private final Visibility visibility;
  private final String description;
  private final String location;

  /**
   * Private constructor for use with the Builder.
   */
  private SimpleEvent(Builder builder) {
    // Validate required fields
    if (builder.subject == null || builder.subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject is required");
    }
    // Check for null startDate FIRST (throws NullPointerException)
    Objects.requireNonNull(builder.startDate, "Start date cannot be null");

    this.subject = builder.subject.trim();
    this.startDate = builder.startDate;
    this.startTime = builder.startTime;

    // Validate time constraints
    if (startTime == null && builder.endTime != null) {
      throw new IllegalArgumentException("Cannot have end time without start time");
    }

    // Set end date - if start time exists but end date doesn't, use start date
    if (startTime != null && builder.endDate == null) {
      this.endDate = startDate;
    } else {
      this.endDate = builder.endDate;
    }

    this.endTime = builder.endTime;

    // Validate that end is after start
    if (!isAllDay() && getEndDateTime().isBefore(getStartDateTime())) {
      throw new IllegalArgumentException("End time must be after start time");
    }

    this.visibility = builder.visibility != null ? builder.visibility : Visibility.PUBLIC;
    this.description = builder.description;
    this.location = builder.location;
  }

  /**
   * Creates a new Builder for constructing SimpleEvent instances.
   *
   * @param subject the required subject of the event
   * @param startDate the required start date of the event
   * @return a new Builder instance
   */
  public static Builder builder(String subject, LocalDate startDate) {
    return new Builder(subject, startDate);
  }

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
    return Optional.ofNullable(endDate);
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
      LocalDate actualEndDate = endDate != null ? endDate : startDate;
      return actualEndDate.atTime(23, 59, 59);
    }
    LocalDate actualEndDate = endDate != null ? endDate : startDate;
    LocalTime actualEndTime = endTime != null ? endTime : startTime;
    return LocalDateTime.of(actualEndDate, actualEndTime);
  }

  @Override
  public boolean conflictsWith(Event other) {
    if (other == null) {
      return false;
    }

    LocalDateTime thisStart = this.getStartDateTime();
    LocalDateTime thisEnd = this.getEndDateTime();
    LocalDateTime otherStart = other.getStartDateTime();
    LocalDateTime otherEnd = other.getEndDateTime();

    // Events conflict if their time ranges overlap
    return thisStart.isBefore(otherEnd) && thisEnd.isAfter(otherStart);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SimpleEvent)) {
      return false;
    }
    SimpleEvent other = (SimpleEvent) obj;
    return Objects.equals(subject, other.subject)
        && Objects.equals(startDate, other.startDate)
        && Objects.equals(startTime, other.startTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDate, startTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("SimpleEvent{subject='").append(subject)
        .append("', date=").append(startDate);
    if (!isAllDay()) {
      sb.append(", time=").append(startTime);
      if (endTime != null && !endTime.equals(startTime)) {
        sb.append("-").append(endTime);
      }
    }
    sb.append("}");
    return sb.toString();
  }

  /**
   * Builder class for creating SimpleEvent instances.
   */
  public static class Builder {
    private final String subject;
    private final LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;
    private Visibility visibility;
    private String description;
    private String location;

    /**
     * Creates a new Builder with required fields.
     *
     * @param subject the subject of the event
     * @param startDate the start date of the event
     */
    private Builder(String subject, LocalDate startDate) {
      this.subject = subject;
      this.startDate = startDate;
    }

    /**
     * Sets the start time of the event.
     *
     * @param startTime the start time
     * @return this Builder instance
     */
    public Builder withStartTime(LocalTime startTime) {
      this.startTime = startTime;
      return this;
    }

    /**
     * Sets the end date of the event.
     *
     * @param endDate the end date
     * @return this Builder instance
     */
    public Builder withEndDate(LocalDate endDate) {
      this.endDate = endDate;
      return this;
    }

    /**
     * Sets the end time of the event.
     *
     * @param endTime the end time
     * @return this Builder instance
     */
    public Builder withEndTime(LocalTime endTime) {
      this.endTime = endTime;
      return this;
    }

    /**
     * Sets the visibility of the event.
     *
     * @param visibility the visibility level
     * @return this Builder instance
     */
    public Builder withVisibility(Visibility visibility) {
      this.visibility = visibility;
      return this;
    }

    /**
     * Sets the description of the event.
     *
     * @param description the description
     * @return this Builder instance
     */
    public Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location of the event.
     *
     * @param location the location
     * @return this Builder instance
     */
    public Builder withLocation(String location) {
      this.location = location;
      return this;
    }

    /**
     * Builds and returns a new SimpleEvent instance.
     *
     * @return the created SimpleEvent
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    public SimpleEvent build() {
      return new SimpleEvent(this);
    }
  }
}