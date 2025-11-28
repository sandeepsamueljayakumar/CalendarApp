package edu.northeastern.calendarapp.model;

/**
 * Listener interface for receiving notifications about calendar event changes.
 */
public interface CalendarListener {

    /**
     * Called when an event is added to the calendar.
     *
     * @param event the event that was added
     */
    void onEventAdded(Event event);

    /**
     * Called when an event is modified in the calendar.
     *
     * @param event the event that was modified
     */
    void onEventModified(Event event);
}