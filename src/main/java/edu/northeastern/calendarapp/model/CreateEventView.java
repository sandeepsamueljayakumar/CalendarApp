package edu.northeastern.calendarapp.view;

import edu.northeastern.calendarapp.model.Calendar;
import edu.northeastern.calendarapp.model.SimpleEvent;
import edu.northeastern.calendarapp.model.Visibility;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * View for creating new events in a calendar.
 * AI-generated code for event creation UI.
 */
public class CreateEventView extends JFrame {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    private final Calendar calendar;
    private final JTextField subjectField;
    private final JTextField startDateField;
    private final JTextField startTimeField;
    private final JTextField endDateField;
    private final JTextField endTimeField;
    private final JCheckBox allDayCheckBox;
    private final JTextArea descriptionArea;
    private final JTextField locationField;
    private final JComboBox<Visibility> visibilityComboBox;
    private final JButton saveButton;
    private final JButton cancelButton;

    /**
     * Creates a new CreateEventView for the specified calendar.
     *
     * @param calendar the calendar to add events to
     */
    public CreateEventView(Calendar calendar) {
        this.calendar = calendar;

        setTitle("Create Event - " + calendar.getTitle());
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        // Subject
        JLabel subjectLabel = new JLabel("Subject:");
        subjectLabel.setBounds(20, 20, 100, 25);
        panel.add(subjectLabel);

        subjectField = new JTextField();
        subjectField.setBounds(130, 20, 230, 25);
        panel.add(subjectField);

        // Start Date
        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setBounds(20, 60, 100, 25);
        panel.add(startDateLabel);

        startDateField = new JTextField("MM/dd/yyyy");
        startDateField.setBounds(130, 60, 230, 25);
        panel.add(startDateField);

        // Start Time
        JLabel startTimeLabel = new JLabel("Start Time:");
        startTimeLabel.setBounds(20, 100, 100, 25);
        panel.add(startTimeLabel);

        startTimeField = new JTextField("hh:mm AM/PM");
        startTimeField.setBounds(130, 100, 230, 25);
        panel.add(startTimeField);

        // End Date
        JLabel endDateLabel = new JLabel("End Date:");
        endDateLabel.setBounds(20, 140, 100, 25);
        panel.add(endDateLabel);

        endDateField = new JTextField("MM/dd/yyyy");
        endDateField.setBounds(130, 140, 230, 25);
        panel.add(endDateField);

        // End Time
        JLabel endTimeLabel = new JLabel("End Time:");
        endTimeLabel.setBounds(20, 180, 100, 25);
        panel.add(endTimeLabel);

        endTimeField = new JTextField("hh:mm AM/PM");
        endTimeField.setBounds(130, 180, 230, 25);
        panel.add(endTimeField);

        // All Day Event
        allDayCheckBox = new JCheckBox("All Day Event");
        allDayCheckBox.setBounds(130, 220, 150, 25);
        allDayCheckBox.addActionListener(e -> toggleAllDayFields());
        panel.add(allDayCheckBox);

        // Description
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setBounds(20, 260, 100, 25);
        panel.add(descriptionLabel);

        descriptionArea = new JTextArea();
        descriptionArea.setBounds(130, 260, 230, 60);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        panel.add(descriptionArea);

        // Location
        JLabel locationLabel = new JLabel("Location:");
        locationLabel.setBounds(20, 330, 100, 25);
        panel.add(locationLabel);

        locationField = new JTextField();
        locationField.setBounds(130, 330, 230, 25);
        panel.add(locationField);

        // Visibility
        JLabel visibilityLabel = new JLabel("Visibility:");
        visibilityLabel.setBounds(20, 370, 100, 25);
        panel.add(visibilityLabel);

        visibilityComboBox = new JComboBox<>(Visibility.values());
        visibilityComboBox.setBounds(130, 370, 230, 25);
        panel.add(visibilityComboBox);

        // Save Button
        saveButton = new JButton("Create Event");
        saveButton.setBounds(130, 410, 120, 30);
        saveButton.addActionListener(e -> createEvent());
        panel.add(saveButton);

        // Cancel Button
        cancelButton = new JButton("Cancel");
        cancelButton.setBounds(260, 410, 100, 30);
        cancelButton.addActionListener(e -> dispose());
        panel.add(cancelButton);

        add(panel);
    }

    /**
     * Toggles time fields based on all-day checkbox state.
     */
    private void toggleAllDayFields() {
        boolean isAllDay = allDayCheckBox.isSelected();
        startTimeField.setEnabled(!isAllDay);
        endTimeField.setEnabled(!isAllDay);
    }

    /**
     * Creates an event from the form data and adds it to the calendar.
     */
    private void createEvent() {
        try {
            // Parse required fields
            String subject = subjectField.getText().trim();
            if (subject.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Subject is required",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate startDate = LocalDate.parse(startDateField.getText().trim(), DATE_FORMAT);

            // Parse optional fields
            LocalTime startTime = null;
            LocalDate endDate = null;
            LocalTime endTime = null;

            if (!allDayCheckBox.isSelected() && !startTimeField.getText().trim().isEmpty()
                    && !startTimeField.getText().equals("hh:mm AM/PM")) {
                startTime = LocalTime.parse(startTimeField.getText().trim(), TIME_FORMAT);
            }

            if (!endDateField.getText().trim().isEmpty()
                    && !endDateField.getText().equals("MM/dd/yyyy")) {
                endDate = LocalDate.parse(endDateField.getText().trim(), DATE_FORMAT);
            }

            if (!allDayCheckBox.isSelected() && !endTimeField.getText().trim().isEmpty()
                    && !endTimeField.getText().equals("hh:mm AM/PM")) {
                endTime = LocalTime.parse(endTimeField.getText().trim(), TIME_FORMAT);
            }

            String description = descriptionArea.getText().trim();
            String location = locationField.getText().trim();
            Visibility visibility = (Visibility) visibilityComboBox.getSelectedItem();

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

            builder.withVisibility(visibility);

            SimpleEvent event = builder.build();

            // Add to calendar
            boolean added = calendar.addEvent(event, false);

            if (added) {
                JOptionPane.showMessageDialog(this, "Event created successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Event conflicts with existing event. Allow conflicts?",
                        "Conflict", JOptionPane.WARNING_MESSAGE);

                int result = JOptionPane.showConfirmDialog(this,
                        "Add event anyway?", "Confirm", JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    calendar.addEvent(event, true);
                    JOptionPane.showMessageDialog(this, "Event created successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            }

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date/time format. Use MM/dd/yyyy for dates and hh:mm AM/PM for times.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error creating event: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}