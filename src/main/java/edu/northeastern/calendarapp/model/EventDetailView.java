package edu.northeastern.calendarapp.view;

import edu.northeastern.calendarapp.model.Calendar;
import edu.northeastern.calendarapp.model.Event;
import edu.northeastern.calendarapp.model.SimpleEvent;
import edu.northeastern.calendarapp.model.Visibility;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * View for displaying and modifying event details.
 * AI-generated code for event detail UI.
 */
public class EventDetailView extends JFrame {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    private final Calendar calendar;
    private final Event originalEvent;
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
     * Creates a new EventDetailView for the specified event.
     *
     * @param calendar the calendar containing the event
     * @param event the event to display and modify
     */
    public EventDetailView(Calendar calendar, Event event) {
        this.calendar = calendar;
        this.originalEvent = event;

        setTitle("Event Details - " + event.getSubject());
        setSize(400, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        // Subject
        JLabel subjectLabel = new JLabel("Subject:");
        subjectLabel.setBounds(20, 20, 100, 25);
        panel.add(subjectLabel);

        subjectField = new JTextField(event.getSubject());
        subjectField.setBounds(130, 20, 230, 25);
        panel.add(subjectField);

        // Start Date
        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setBounds(20, 60, 100, 25);
        panel.add(startDateLabel);

        startDateField = new JTextField(event.getStartDate().format(DATE_FORMAT));
        startDateField.setBounds(130, 60, 230, 25);
        panel.add(startDateField);

        // Start Time
        JLabel startTimeLabel = new JLabel("Start Time:");
        startTimeLabel.setBounds(20, 100, 100, 25);
        panel.add(startTimeLabel);

        String startTimeText = event.getStartTime()
                .map(t -> t.format(TIME_FORMAT))
                .orElse("");
        startTimeField = new JTextField(startTimeText);
        startTimeField.setBounds(130, 100, 230, 25);
        panel.add(startTimeField);

        // End Date
        JLabel endDateLabel = new JLabel("End Date:");
        endDateLabel.setBounds(20, 140, 100, 25);
        panel.add(endDateLabel);

        String endDateText = event.getEndDate()
                .map(d -> d.format(DATE_FORMAT))
                .orElse("");
        endDateField = new JTextField(endDateText);
        endDateField.setBounds(130, 140, 230, 25);
        panel.add(endDateField);

        // End Time
        JLabel endTimeLabel = new JLabel("End Time:");
        endTimeLabel.setBounds(20, 180, 100, 25);
        panel.add(endTimeLabel);

        String endTimeText = event.getEndTime()
                .map(t -> t.format(TIME_FORMAT))
                .orElse("");
        endTimeField = new JTextField(endTimeText);
        endTimeField.setBounds(130, 180, 230, 25);
        panel.add(endTimeField);

        // All Day Event
        allDayCheckBox = new JCheckBox("All Day Event");
        allDayCheckBox.setBounds(130, 220, 150, 25);
        allDayCheckBox.setSelected(event.isAllDay());
        allDayCheckBox.addActionListener(e -> toggleAllDayFields());
        panel.add(allDayCheckBox);

        // Description
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setBounds(20, 260, 100, 25);
        panel.add(descriptionLabel);

        descriptionArea = new JTextArea(event.getDescription().orElse(""));
        descriptionArea.setBounds(130, 260, 230, 60);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        panel.add(descriptionArea);

        // Location
        JLabel locationLabel = new JLabel("Location:");
        locationLabel.setBounds(20, 330, 100, 25);
        panel.add(locationLabel);

        locationField = new JTextField(event.getLocation().orElse(""));
        locationField.setBounds(130, 330, 230, 25);
        panel.add(locationField);

        // Visibility
        JLabel visibilityLabel = new JLabel("Visibility:");
        visibilityLabel.setBounds(20, 370, 100, 25);
        panel.add(visibilityLabel);

        visibilityComboBox = new JComboBox<>(Visibility.values());
        visibilityComboBox.setSelectedItem(event.getVisibility());
        visibilityComboBox.setBounds(130, 370, 230, 25);
        panel.add(visibilityComboBox);

        // Save Button
        saveButton = new JButton("Save Changes");
        saveButton.setBounds(130, 420, 120, 30);
        saveButton.addActionListener(e -> saveChanges());
        panel.add(saveButton);

        // Cancel Button
        cancelButton = new JButton("Cancel");
        cancelButton.setBounds(260, 420, 100, 30);
        cancelButton.addActionListener(e -> dispose());
        panel.add(cancelButton);

        add(panel);

        // Set initial state of time fields
        toggleAllDayFields();
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
     * Saves changes to the event.
     */
    private void saveChanges() {
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

            if (!allDayCheckBox.isSelected() && !startTimeField.getText().trim().isEmpty()) {
                startTime = LocalTime.parse(startTimeField.getText().trim(), TIME_FORMAT);
            }

            if (!endDateField.getText().trim().isEmpty()) {
                endDate = LocalDate.parse(endDateField.getText().trim(), DATE_FORMAT);
            }

            if (!allDayCheckBox.isSelected() && !endTimeField.getText().trim().isEmpty()) {
                endTime = LocalTime.parse(endTimeField.getText().trim(), TIME_FORMAT);
            }

            String description = descriptionArea.getText().trim();
            String location = locationField.getText().trim();
            Visibility visibility = (Visibility) visibilityComboBox.getSelectedItem();

            // Create the updated event using Builder pattern
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

            SimpleEvent updatedEvent = builder.build();

            // Get original event identifiers
            String originalSubject = originalEvent.getSubject();
            LocalDate originalDate = originalEvent.getStartDate();
            LocalDateTime originalTime = originalEvent.getStartTime()
                    .map(t -> LocalDateTime.of(originalDate, t))
                    .orElse(null);

            // Update in calendar
            boolean updated = calendar.updateEvent(
                    originalSubject,
                    originalDate,
                    originalTime,
                    updatedEvent,
                    false
            );

            if (updated) {
                JOptionPane.showMessageDialog(this, "Event updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Event update conflicts with existing event. Allow conflicts?",
                        "Conflict", JOptionPane.WARNING_MESSAGE);

                int result = JOptionPane.showConfirmDialog(this,
                        "Update event anyway?", "Confirm", JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    calendar.updateEvent(
                            originalSubject,
                            originalDate,
                            originalTime,
                            updatedEvent,
                            true
                    );
                    JOptionPane.showMessageDialog(this, "Event updated successfully!",
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
                    "Error updating event: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}