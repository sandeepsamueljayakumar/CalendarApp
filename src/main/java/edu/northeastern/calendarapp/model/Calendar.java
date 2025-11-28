package edu.northeastern.calendarapp.model;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Represents a calendar that can hold and manage events.
 */
public interface Calendar {

    /**
     * Gets the title of the calendar.
     *
     * @return the calendar title
     */
    String getTitle();

    /**
     * Adds a single event to the calendar.
     *
     * @param event the event to add
     * @param allowConflicts whether to allow conflicting events
     * @return true if the event was added, false if it was rejected due to conflicts
     * @throws IllegalArgumentException if the event is null or violates uniqueness constraints
     */
    boolean addEvent(Event event, boolean allowConflicts);

    /**
     * Retrieves an event by its subject, start date, and start time.
     *
     * @param subject the subject of the event
     * @param startDate the start date of the event
     * @param startTime the start time of the event (null for all-day events)
     * @return the event if found, empty otherwise
     */
    Optional<Event> getEvent(String subject, LocalDate startDate, LocalDateTime startTime);

    /**
     * Retrieves all events on a specific date.
     *
     * @param date the date to query
     * @return list of events on the given date
     */
    List<Event> getEventsOnDate(LocalDate date);

    /**
     * Retrieves all events within a date range (inclusive).
     *
     * @param startDate the start of the date range
     * @param endDate the end of the date range
     * @return list of events within the date range
     */
    List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate);

    /**
     * Checks if the user is busy at a specific date and time.
     *
     * @param dateTime the date and time to check
     * @return true if there is an event at that time, false otherwise
     */
    boolean isBusyAt(LocalDateTime dateTime);

    /**
     * Gets all events in the calendar.
     *
     * @return list of all events
     */
    List<Event> getAllEvents();

    /**
     * Exports the calendar to CSV format.
     *
     * @return the calendar in CSV format as a string
     */
    String exportToCSV();

    /**
     * Imports events from a CSV string into this calendar.
     *
     * @param csvContent the CSV content as a string
     * @throws IllegalArgumentException if the CSV format is invalid
     */
    void importFromCSV(String csvContent);

    /**
     * Updates an existing event with new information.
     *
     * @param originalSubject the original subject of the event
     * @param originalDate the original start date
     * @param originalTime the original start time (null for all-day)
     * @param updatedEvent the updated event
     * @param allowConflicts whether to allow conflicts with the update
     * @return true if the event was updated, false if update was rejected
     * @throws IllegalArgumentException if the original event is not found
     */
    boolean updateEvent(String originalSubject, LocalDate originalDate,
                        LocalDateTime originalTime, Event updatedEvent,
                        boolean allowConflicts);

    /**
     * Adds a calendar listener to receive event notifications.
     *
     * @param listener the listener to add
     * @throws IllegalArgumentException if listener is null
     */
    void addCalendarListener(CalendarListener listener);

    /**
     * Removes a calendar listener.
     *
     * @param listener the listener to remove
     * @throws IllegalArgumentException if listener is null
     */
    void removeCalendarListener(CalendarListener listener);
}