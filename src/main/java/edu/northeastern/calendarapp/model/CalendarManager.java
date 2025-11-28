package edu.northeastern.calendarapp.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing multiple calendars (save/restore operations).
 * AI-generated code for calendar persistence.
 */
public class CalendarManager {

    private static final String CALENDAR_DELIMITER = "===CALENDAR===";

    /**
     * Saves all calendars to a file.
     *
     * @param calendars the list of calendars to save
     * @param filename the file to save to
     * @throws IOException if file writing fails
     */
    public static void saveAllCalendars(List<Calendar> calendars, String filename)
            throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Calendar calendar : calendars) {
                writer.write(CALENDAR_DELIMITER);
                writer.newLine();
                writer.write(calendar.getTitle());
                writer.newLine();
                writer.write(calendar.exportToCSV());
                writer.newLine();
            }
        }
    }

    /**
     * Restores all calendars from a file.
     *
     * @param filename the file to restore from
     * @return list of restored calendars
     * @throws IOException if file reading fails
     */
    public static List<Calendar> restoreAllCalendars(String filename) throws IOException {
        List<Calendar> calendars = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            String currentTitle = null;
            StringBuilder currentCSV = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.equals(CALENDAR_DELIMITER)) {
                    // Save previous calendar if exists
                    if (currentTitle != null && currentCSV.length() > 0) {
                        Calendar calendar = new SimpleCalendar(currentTitle);
                        calendar.importFromCSV(currentCSV.toString());
                        calendars.add(calendar);
                    }

                    // Start new calendar
                    currentTitle = reader.readLine();
                    currentCSV = new StringBuilder();
                } else {
                    currentCSV.append(line).append("\n");
                }
            }

            // Save last calendar
            if (currentTitle != null && currentCSV.length() > 0) {
                Calendar calendar = new SimpleCalendar(currentTitle);
                calendar.importFromCSV(currentCSV.toString());
                calendars.add(calendar);
            }
        }

        return calendars;
    }
}