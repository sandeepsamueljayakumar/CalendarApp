package edu.northeastern.calendarapp.model;

import edu.northeastern.calendarapp.view.CreateEventView;
import edu.northeastern.calendarapp.view.EventDetailView;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Main controller for the Calendar application.
 * AI-generated code for application bootstrapping.
 */
public class Main {
    private static final String CALENDARS_FILE = "calendars.txt";

    /**
     * Main entry point for the application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // Step 1: Restore calendars from previous runs
        List<Calendar> calendars = restoreCalendars();

        // Step 2: Select a calendar (or create a default one if none exist)
        Calendar selectedCalendar = selectCalendar(calendars);

        // Step 3: Create and display views
        displayViews(selectedCalendar);

        // Save calendars on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                CalendarManager.saveAllCalendars(calendars, CALENDARS_FILE);
                System.out.println("Calendars saved successfully.");
            } catch (Exception e) {
                System.err.println("Error saving calendars: " + e.getMessage());
            }
        }));
    }

    /**
     * Restores calendars from the save file, or creates default calendars if none exist.
     */
    private static List<Calendar> restoreCalendars() {
        File file = new File(CALENDARS_FILE);

        if (file.exists()) {
            try {
                List<Calendar> calendars = CalendarManager.restoreAllCalendars(CALENDARS_FILE);
                System.out.println("Restored " + calendars.size() + " calendar(s).");
                return calendars;
            } catch (Exception e) {
                System.err.println("Error restoring calendars: " + e.getMessage());
                System.out.println("Creating new default calendars.");
            }
        }

        // Create default calendars if restoration failed or file doesn't exist
        return createDefaultCalendars();
    }

    /**
     * Creates default calendars with sample events.
     */
    private static List<Calendar> createDefaultCalendars() {
        Calendar workCalendar = new SimpleCalendar("Work");
        Calendar personalCalendar = new SimpleCalendar("Personal");

        // Add some sample events using Builder pattern
        try {
            SimpleEvent meeting = SimpleEvent.builder("Team Meeting", LocalDate.now())
                    .withStartTime(LocalTime.of(10, 0))
                    .withEndTime(LocalTime.of(11, 0))
                    .withDescription("Weekly team sync")
                    .withLocation("Conference Room A")
                    .withVisibility(Visibility.PUBLIC)
                    .build();
            workCalendar.addEvent(meeting, false);

            SimpleEvent lunch = SimpleEvent.builder("Lunch with Friends", LocalDate.now())
                    .withStartTime(LocalTime.of(12, 30))
                    .withEndTime(LocalTime.of(13, 30))
                    .withLocation("Downtown Cafe")
                    .withVisibility(Visibility.PRIVATE)
                    .build();
            personalCalendar.addEvent(lunch, false);

        } catch (Exception e) {
            System.err.println("Error creating sample events: " + e.getMessage());
        }

        return List.of(workCalendar, personalCalendar);
    }

    /**
     * Selects a calendar to work with (arbitrarily selects the first one).
     */
    private static Calendar selectCalendar(List<Calendar> calendars) {
        if (calendars.isEmpty()) {
            System.out.println("No calendars available. Creating a new one.");
            Calendar newCalendar = new SimpleCalendar("Default Calendar");
            calendars.add(newCalendar);
            return newCalendar;
        }

        Calendar selected = calendars.get(0);
        System.out.println("Selected calendar: " + selected.getTitle());
        return selected;
    }

    /**
     * Displays the views for creating and viewing events.
     */
    private static void displayViews(Calendar calendar) {
        // Create and display CreateEventView
        CreateEventView createView = new CreateEventView(calendar);
        createView.setLocation(100, 100);
        createView.setVisible(true);

        // Create and display EventDetailView with an arbitrary event
        List<Event> events = calendar.getAllEvents();
        if (!events.isEmpty()) {
            Event sampleEvent = events.get(0);
            EventDetailView detailView = new EventDetailView(calendar, sampleEvent);
            detailView.setLocation(550, 100);
            detailView.setVisible(true);
        } else {
            System.out.println("No events available for EventDetailView.");
        }
    }
}