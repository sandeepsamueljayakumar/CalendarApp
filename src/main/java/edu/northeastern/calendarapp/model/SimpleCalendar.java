package edu.northeastern.calendarapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A simple implementation of the Calendar interface.
 */
public class SimpleCalendar implements Calendar {
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

  private final String title;
  private final List<Event> events;
  private final boolean defaultAllowConflicts;

  /**
   * Creates a new calendar with the given title.
   *
   * @param title the title of the calendar
   * @throws IllegalArgumentException if title is null or empty
   */
  public SimpleCalendar(String title) {
    this(title, false);
  }

  /**
   * Creates a new calendar with the given title and conflict setting.
   *
   * @param title the title of the calendar
   * @param defaultAllowConflicts the default setting for allowing conflicts
   * @throws IllegalArgumentException if title is null or empty
   */
  public SimpleCalendar(String title, boolean defaultAllowConflicts) {
    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar title cannot be null or empty");
    }
    this.title = title.trim();
    this.events = new ArrayList<>();
    this.defaultAllowConflicts = defaultAllowConflicts;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public boolean addEvent(Event event, boolean allowConflicts) {
    Objects.requireNonNull(event, "Event cannot be null");

    // Check for duplicate (same subject, start date, and start time)
    if (isDuplicateEvent(event)) {
      throw new IllegalArgumentException(
          "An event with the same subject, date, and time already exists");
    }

    // Check for conflicts if not allowed
    if (!allowConflicts && hasConflict(event)) {
      return false;
    }

    events.add(event);
    return true;
  }

  @Override
  public Optional<Event> getEvent(String subject, LocalDate startDate, LocalDateTime startTime) {
    if (subject == null || startDate == null) {
      return Optional.empty();
    }

    LocalTime time = startTime != null ? startTime.toLocalTime() : null;

    return events.stream()
        .filter(e -> e.getSubject().equals(subject))
        .filter(e -> e.getStartDate().equals(startDate))
        .filter(e -> {
          Optional<LocalTime> eventTime = e.getStartTime();
          if (time == null) {
            return eventTime.isEmpty();
          }
          return eventTime.isPresent() && eventTime.get().equals(time);
        })
        .findFirst();
  }

  @Override
  public List<Event> getEventsOnDate(LocalDate date) {
    Objects.requireNonNull(date, "Date cannot be null");

    return events.stream()
        .filter(e -> isEventOnDate(e, date))
        .collect(Collectors.toList());
  }

  @Override
  public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
    Objects.requireNonNull(startDate, "Start date cannot be null");
    Objects.requireNonNull(endDate, "End date cannot be null");

    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date must be after or equal to start date");
    }

    return events.stream()
        .filter(e -> isEventInRange(e, startDate, endDate))
        .collect(Collectors.toList());
  }

  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    Objects.requireNonNull(dateTime, "Date time cannot be null");

    return events.stream()
        .anyMatch(e -> isEventAtTime(e, dateTime));
  }

  @Override
  public List<Event> getAllEvents() {
    return new ArrayList<>(events);
  }

  @Override
  public boolean updateEvent(String originalSubject, LocalDate originalDate,
      LocalDateTime originalTime, Event updatedEvent,
      boolean allowConflicts) {
    Objects.requireNonNull(originalSubject, "Original subject cannot be null");
    Objects.requireNonNull(originalDate, "Original date cannot be null");
    Objects.requireNonNull(updatedEvent, "Updated event cannot be null");

    // Find the original event
    Optional<Event> originalEvent = getEvent(originalSubject, originalDate, originalTime);
    if (originalEvent.isEmpty()) {
      throw new IllegalArgumentException("Original event not found");
    }

    // Remove the original event temporarily
    events.remove(originalEvent.get());

    // Check if the updated event would create a duplicate
    if (isDuplicateEvent(updatedEvent)) {
      // Restore the original event
      events.add(originalEvent.get());
      throw new IllegalArgumentException(
          "Update would create duplicate event");
    }

    // Check for conflicts
    if (!allowConflicts && hasConflict(updatedEvent)) {
      // Restore the original event
      events.add(originalEvent.get());
      return false;
    }

    // Add the updated event
    events.add(updatedEvent);
    return true;
  }

  @Override
  public String exportToCSV() {
    StringBuilder csv = new StringBuilder();
    // Google Calendar CSV header
    csv.append("Subject,Start Date,Start Time,End Date,End Time,")
        .append("All Day Event,Description,Location,Private\n");

    for (Event event : events) {
      csv.append(formatEventAsCSV(event)).append("\n");
    }

    return csv.toString();
  }

  /**
   * Formats a single event as a CSV row.
   * AI-generated code for CSV export formatting.
   */
  private String formatEventAsCSV(Event event) {
    StringBuilder row = new StringBuilder();

    // Subject
    row.append(escapeCSV(event.getSubject())).append(",");

    // Start Date
    row.append(event.getStartDate().format(DATE_FORMAT)).append(",");

    // Start Time
    if (event.getStartTime().isPresent()) {
      row.append(event.getStartTime().get().format(TIME_FORMAT));
    }
    row.append(",");

    // End Date
    if (event.getEndDate().isPresent()) {
      row.append(event.getEndDate().get().format(DATE_FORMAT));
    } else if (event.getStartTime().isPresent()) {
      row.append(event.getStartDate().format(DATE_FORMAT));
    }
    row.append(",");

    // End Time
    if (event.getEndTime().isPresent()) {
      row.append(event.getEndTime().get().format(TIME_FORMAT));
    } else if (event.getStartTime().isPresent()) {
      row.append(event.getStartTime().get().format(TIME_FORMAT));
    }
    row.append(",");

    // All Day Event
    row.append(event.isAllDay() ? "TRUE" : "FALSE").append(",");

    // Description
    row.append(escapeCSV(event.getDescription().orElse(""))).append(",");

    // Location
    row.append(escapeCSV(event.getLocation().orElse(""))).append(",");

    // Private
    row.append(event.getVisibility() == Visibility.PRIVATE ? "TRUE" : "FALSE");

    return row.toString();
  }

  /**
   * Escapes special characters in CSV fields.
   * AI-generated code for CSV escaping.
   */
  private String escapeCSV(String field) {
    if (field == null) {
      return "";
    }
    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
      return "\"" + field.replace("\"", "\"\"") + "\"";
    }
    return field;
  }

  private boolean isDuplicateEvent(Event event) {
    return events.stream()
        .anyMatch(e -> e.getSubject().equals(event.getSubject())
            && e.getStartDate().equals(event.getStartDate())
            && e.getStartTime().equals(event.getStartTime()));
  }

  private boolean hasConflict(Event event) {
    return events.stream()
        .anyMatch(e -> e.conflictsWith(event));
  }

  private boolean isEventOnDate(Event event, LocalDate date) {
    LocalDate eventStart = event.getStartDate();
    LocalDate eventEnd = event.getEndDate().orElse(eventStart);

    return !date.isBefore(eventStart) && !date.isAfter(eventEnd);
  }

  private boolean isEventInRange(Event event, LocalDate rangeStart, LocalDate rangeEnd) {
    LocalDate eventStart = event.getStartDate();
    LocalDate eventEnd = event.getEndDate().orElse(eventStart);

    return !eventEnd.isBefore(rangeStart) && !eventStart.isAfter(rangeEnd);
  }

  private boolean isEventAtTime(Event event, LocalDateTime dateTime) {
    LocalDateTime eventStart = event.getStartDateTime();
    LocalDateTime eventEnd = event.getEndDateTime();

    return !dateTime.isBefore(eventStart) && !dateTime.isAfter(eventEnd);
  }

  @Override
  public String toString() {
    return String.format("Calendar: %s (%d events)", title, events.size());
  }
}