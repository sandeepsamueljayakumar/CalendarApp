package edu.northeastern.calendarapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Represents a calendar event with required and optional properties.
 */
public interface Event {

  /**
   * Gets the subject of the event.
   *
   * @return the event subject
   */
  String getSubject();

  /**
   * Gets the start date of the event.
   *
   * @return the start date
   */
  LocalDate getStartDate();

  /**
   * Gets the start time of the event.
   *
   * @return the start time, or empty if this is an all-day event
   */
  Optional<LocalTime> getStartTime();

  /**
   * Gets the end date of the event.
   *
   * @return the end date, or empty if no end date is specified
   */
  Optional<LocalDate> getEndDate();

  /**
   * Gets the end time of the event.
   *
   * @return the end time, or empty if no end time is specified
   */
  Optional<LocalTime> getEndTime();

  /**
   * Gets the visibility of the event.
   *
   * @return the visibility level
   */
  Visibility getVisibility();

  /**
   * Gets the description of the event.
   *
   * @return the description, or empty if no description
   */
  Optional<String> getDescription();

  /**
   * Gets the location of the event.
   *
   * @return the location, or empty if no location
   */
  Optional<String> getLocation();

  /**
   * Checks if this is an all-day event.
   *
   * @return true if this is an all-day event, false otherwise
   */
  boolean isAllDay();

  /**
   * Checks if this event conflicts with another event.
   *
   * @param other the other event to check
   * @return true if the events conflict, false otherwise
   */
  boolean conflictsWith(Event other);

  /**
   * Gets the start date and time combined.
   *
   * @return the start date-time
   */
  LocalDateTime getStartDateTime();

  /**
   * Gets the end date and time combined.
   *
   * @return the end date-time
   */
  LocalDateTime getEndDateTime();
}