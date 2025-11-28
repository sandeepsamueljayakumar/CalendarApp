package edu.northeastern.calendarapp.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the CalendarListener functionality in SimpleCalendar.
 * Tests written without AI assistance as per assignment requirements.
 */
class SimpleCalendarListenerTest {

    private SimpleCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new SimpleCalendar("Test Calendar");
    }

    /**
     * Test helper class to track listener notifications.
     */
    private static class TestListener implements CalendarListener {
        private final List<Event> addedEvents = new ArrayList<>();
        private final List<Event> modifiedEvents = new ArrayList<>();
        private final String name;

        TestListener(String name) {
            this.name = name;
        }

        @Override
        public void onEventAdded(Event event) {
            addedEvents.add(event);
        }

        @Override
        public void onEventModified(Event event) {
            modifiedEvents.add(event);
        }

        public List<Event> getAddedEvents() {
            return addedEvents;
        }

        public List<Event> getModifiedEvents() {
            return modifiedEvents;
        }

        public void reset() {
            addedEvents.clear();
            modifiedEvents.clear();
        }

        public String getName() {
            return name;
        }
    }

    @Test
    void testAddListener() {
        TestListener listener = new TestListener("listener1");
        assertDoesNotThrow(() -> calendar.addCalendarListener(listener));
    }

    @Test
    void testAddListenerNull() {
        assertThrows(NullPointerException.class, () -> calendar.addCalendarListener(null));
    }

    @Test
    void testRemoveListener() {
        TestListener listener = new TestListener("listener1");
        calendar.addCalendarListener(listener);
        assertDoesNotThrow(() -> calendar.removeCalendarListener(listener));
    }

    @Test
    void testRemoveListenerNull() {
        assertThrows(NullPointerException.class, () -> calendar.removeCalendarListener(null));
    }

    @Test
    void testSingleListenerNotifiedOnAdd() {
        TestListener listener = new TestListener("listener1");
        calendar.addCalendarListener(listener);

        Event event = SimpleEvent.builder("Test Event", LocalDate.now())
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withVisibility(Visibility.PUBLIC)
                .build();

        calendar.addEvent(event, false);

        assertEquals(1, listener.getAddedEvents().size());
        assertEquals(event, listener.getAddedEvents().get(0));
        assertEquals(0, listener.getModifiedEvents().size());
    }

    @Test
    void testSingleListenerNotifiedOnModify() {
        TestListener listener = new TestListener("listener1");

        // Add an event first
        Event originalEvent = SimpleEvent.builder("Original Event", LocalDate.now())
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withVisibility(Visibility.PUBLIC)
                .build();
        calendar.addEvent(originalEvent, false);

        // Now add the listener
        calendar.addCalendarListener(listener);

        // Modify the event
        Event updatedEvent = SimpleEvent.builder("Updated Event", LocalDate.now())
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withDescription("New description")
                .withVisibility(Visibility.PUBLIC)
                .build();

        calendar.updateEvent(
                "Original Event",
                LocalDate.now(),
                java.time.LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0)),
                updatedEvent,
                false
        );

        assertEquals(0, listener.getAddedEvents().size());
        assertEquals(1, listener.getModifiedEvents().size());
        assertEquals(updatedEvent, listener.getModifiedEvents().get(0));
    }

    @Test
    void testMultipleListenersAllNotified() {
        TestListener listener1 = new TestListener("listener1");
        TestListener listener2 = new TestListener("listener2");
        TestListener listener3 = new TestListener("listener3");

        calendar.addCalendarListener(listener1);
        calendar.addCalendarListener(listener2);
        calendar.addCalendarListener(listener3);

        Event event = SimpleEvent.builder("Test Event", LocalDate.now())
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withVisibility(Visibility.PUBLIC)
                .build();

        calendar.addEvent(event, false);

        // All three listeners should be notified
        assertEquals(1, listener1.getAddedEvents().size());
        assertEquals(1, listener2.getAddedEvents().size());
        assertEquals(1, listener3.getAddedEvents().size());

        assertEquals(event, listener1.getAddedEvents().get(0));
        assertEquals(event, listener2.getAddedEvents().get(0));
        assertEquals(event, listener3.getAddedEvents().get(0));
    }

    @Test
    void testRemovedListenerNotNotified() {
        TestListener listener1 = new TestListener("listener1");
        TestListener listener2 = new TestListener("listener2");

        calendar.addCalendarListener(listener1);
        calendar.addCalendarListener(listener2);

        // Remove listener1
        calendar.removeCalendarListener(listener1);

        Event event = SimpleEvent.builder("Test Event", LocalDate.now())
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withVisibility(Visibility.PUBLIC)
                .build();

        calendar.addEvent(event, false);

        // listener1 should NOT be notified
        assertEquals(0, listener1.getAddedEvents().size());

        // listener2 should be notified
        assertEquals(1, listener2.getAddedEvents().size());
    }

    @Test
    void testListenerNotifiedMultipleTimes() {
        TestListener listener = new TestListener("listener1");
        calendar.addCalendarListener(listener);

        Event event1 = SimpleEvent.builder("Event 1", LocalDate.now())
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withVisibility(Visibility.PUBLIC)
                .build();

        Event event2 = SimpleEvent.builder("Event 2", LocalDate.now())
                .withStartTime(LocalTime.of(14, 0))
                .withEndTime(LocalTime.of(15, 0))
                .withVisibility(Visibility.PUBLIC)
                .build();

        calendar.addEvent(event1, false);
        calendar.addEvent(event2, false);

        assertEquals(2, listener.getAddedEvents().size());
        assertEquals(event1, listener.getAddedEvents().get(0));
        assertEquals(event2, listener.getAddedEvents().get(1));
    }

    @Test
    void testListenerNotNotifiedWhenAddFails() {
        TestListener listener = new TestListener("listener1");
        calendar.addCalendarListener(listener);

        // Add an event first
        Event event1 = SimpleEvent.builder("Event 1", LocalDate.now())
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withVisibility(Visibility.PUBLIC)
                .build();
        calendar.addEvent(event1, false);

        listener.reset();

        // Try to add a conflicting event (should fail)
        Event event2 = SimpleEvent.builder("Event 2", LocalDate.now())
                .withStartTime(LocalTime.of(10, 30))
                .withEndTime(LocalTime.of(11, 30))
                .withVisibility(Visibility.PUBLIC)
                .build();

        boolean added = calendar.addEvent(event2, false);

        assertFalse(added);
        assertEquals(0, listener.getAddedEvents().size());
    }

    @Test
    void testListenerNotifiedOnAddWithConflictAllowed() {
        TestListener listener = new TestListener("listener1");
        calendar.addCalendarListener(listener);

        // Add an event first
        Event event1 = SimpleEvent.builder("Event 1", LocalDate.now())
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withVisibility(Visibility.PUBLIC)
                .build();
        calendar.addEvent(event1, false);

        listener.reset();

        // Add a conflicting event but allow conflicts
        Event event2 = SimpleEvent.builder("Event 2", LocalDate.now())
                .withStartTime(LocalTime.of(10, 30))
                .withEndTime(LocalTime.of(11, 30))
                .withVisibility(Visibility.PUBLIC)
                .build();

        boolean added = calendar.addEvent(event2, true);

        assertTrue(added);
        assertEquals(1, listener.getAddedEvents().size());
        assertEquals(event2, listener.getAddedEvents().get(0));
    }

    @Test
    void testMultipleListenersIndependentNotifications() {
        TestListener listener1 = new TestListener("listener1");
        TestListener listener2 = new TestListener("listener2");

        calendar.addCalendarListener(listener1);

        // Add first event - only listener1 should be notified
        Event event1 = SimpleEvent.builder("Event 1", LocalDate.now())
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withVisibility(Visibility.PUBLIC)
                .build();
        calendar.addEvent(event1, false);

        // Now add listener2
        calendar.addCalendarListener(listener2);

        // Add second event - both should be notified
        Event event2 = SimpleEvent.builder("Event 2", LocalDate.now())
                .withStartTime(LocalTime.of(14, 0))
                .withEndTime(LocalTime.of(15, 0))
                .withVisibility(Visibility.PUBLIC)
                .build();
        calendar.addEvent(event2, false);

        assertEquals(2, listener1.getAddedEvents().size());
        assertEquals(1, listener2.getAddedEvents().size());
        assertEquals(event2, listener2.getAddedEvents().get(0));
    }

    @Test
    void testListenerNotNotifiedWhenUpdateFails() {
        TestListener listener = new TestListener("listener1");

        // Add first event
        Event event1 = SimpleEvent.builder("Event 1", LocalDate.now())
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withVisibility(Visibility.PUBLIC)
                .build();
        calendar.addEvent(event1, false);

        // Add second event
        Event event2 = SimpleEvent.builder("Event 2", LocalDate.now())
                .withStartTime(LocalTime.of(14, 0))
                .withEndTime(LocalTime.of(15, 0))
                .withVisibility(Visibility.PUBLIC)
                .build();
        calendar.addEvent(event2, false);

        // Now add listener
        calendar.addCalendarListener(listener);

        // Try to update event1 to conflict with event2 (should fail)
        Event updatedEvent = SimpleEvent.builder("Updated Event 1", LocalDate.now())
                .withStartTime(LocalTime.of(14, 30))
                .withEndTime(LocalTime.of(15, 30))
                .withVisibility(Visibility.PUBLIC)
                .build();

        boolean updated = calendar.updateEvent(
                "Event 1",
                LocalDate.now(),
                java.time.LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0)),
                updatedEvent,
                false
        );

        assertFalse(updated);
        assertEquals(0, listener.getModifiedEvents().size());
    }

    @Test
    void testSameListenerNotAddedTwice() {
        TestListener listener = new TestListener("listener1");

        calendar.addCalendarListener(listener);
        calendar.addCalendarListener(listener); // Add same listener again

        Event event = SimpleEvent.builder("Test Event", LocalDate.now())
                .withStartTime(LocalTime.of(10, 0))
                .withEndTime(LocalTime.of(11, 0))
                .withVisibility(Visibility.PUBLIC)
                .build();

        calendar.addEvent(event, false);

        // Should only be notified once
        assertEquals(1, listener.getAddedEvents().size());
    }
}