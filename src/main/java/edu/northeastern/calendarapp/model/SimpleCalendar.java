package edu.northeastern.calendarapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private final List<CalendarListener> listeners;

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
        this.listeners = new ArrayList<>();
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
        announceEventAdded(event);
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
        announceEventModified(updatedEvent);
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

    @Override
    public void importFromCSV(String csvContent) {
        Objects.requireNonNull(csvContent, "CSV content cannot be null");

        String[] lines = csvContent.split("\n");
        if (lines.length < 1) {
            throw new IllegalArgumentException("CSV must contain at least a header");
        }

        // Skip header (line 0) and process event lines
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                Event event = parseCSVLine(line);
                addEvent(event, defaultAllowConflicts);
            } catch (Exception e) {
                throw new IllegalArgumentException("Error parsing CSV line " + (i + 1) + ": "
                        + e.getMessage(), e);
            }
        }
    }

    @Override
    public void addCalendarListener(CalendarListener listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeCalendarListener(CalendarListener listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that an event was added.
     *
     * @param event the event that was added
     */
    protected void announceEventAdded(Event event) {
        for (CalendarListener listener : listeners) {
            listener.onEventAdded(event);
        }
    }

    /**
     * Notifies all registered listeners that an event was modified.
     *
     * @param event the event that was modified
     */
    protected void announceEventModified(Event event) {
        for (CalendarListener listener : listeners) {
            listener.onEventModified(event);
        }
    }

    /**
     * Parses a single CSV line into an Event.
     * AI-generated code for CSV import parsing.
     */
    private Event parseCSVLine(String line) {
        List<String> fields = parseCSVFields(line);

        if (fields.size() < 9) {
            throw new IllegalArgumentException("CSV line has insufficient fields");
        }

        String subject = fields.get(0);
        String startDateStr = fields.get(1);
        String startTimeStr = fields.get(2);
        String endDateStr = fields.get(3);
        String endTimeStr = fields.get(4);
        boolean isAllDay = "TRUE".equalsIgnoreCase(fields.get(5));
        String description = fields.get(6);
        String location = fields.get(7);
        boolean isPrivate = "TRUE".equalsIgnoreCase(fields.get(8));

        // Parse dates and times
        LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMAT);
        LocalTime startTime = null;
        LocalDate endDate = null;
        LocalTime endTime = null;

        if (!isAllDay && !startTimeStr.isEmpty()) {
            startTime = LocalTime.parse(startTimeStr, TIME_FORMAT);
        }

        if (!endDateStr.isEmpty()) {
            endDate = LocalDate.parse(endDateStr, DATE_FORMAT);
        }

        if (!isAllDay && !endTimeStr.isEmpty()) {
            endTime = LocalTime.parse(endTimeStr, TIME_FORMAT);
        }

        // Create the event using Builder pattern
        SimpleEvent.Builder builder = SimpleEvent.builder(subject, startDate);

        if (startTime != null) {
            builder.withStartTime(startTime);
        }

        if (endDate != null) {
            builder.withEndDate(endDate);
        }

        if (endTime != null) {
            builder.withEndTime(endTime);
        }

        if (!description.isEmpty()) {
            builder.withDescription(description);
        }

        if (!location.isEmpty()) {
            builder.withLocation(location);
        }

        builder.withVisibility(isPrivate ? Visibility.PRIVATE : Visibility.PUBLIC);

        return builder.build();
    }

    /**
     * Parses CSV fields, handling quoted fields correctly.
     * AI-generated code for CSV field parsing.
     */
    private List<String> parseCSVFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    currentField.append('"');
                    i++;
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // Field separator
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // Add the last field
        fields.add(currentField.toString());

        return fields;
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