/*
 * Copyright 2025 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4jtestmock;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.slf4j.impl.MockLoggerEvent.Level;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class providing assertion methods for testing {@link MockLogger} instances.
 * <p>
 * This class contains methods to verify that logged events match expected criteria such as log level,
 * message content, and markers. All assertion methods are and take a {@link Logger} instance as their
 * first parameter, automatically converting it to {@link MockLogger} for testing purposes.
 * <p>
 * Example usage:
 * <pre>{@code
 * Logger logger = LoggerFactory.getLogger("test");
 * logger.info("Test message");
 * 
 * AssertLogger.assertEvent(logger, 0, Level.INFO, "Test message");
 * }</pre>
 *
 * @author Daniel Felix Ferber
 */
@UtilityClass
public class AssertLogger {
    /**
     * Converts a Logger instance to MockLogger, throwing an assertion error if the conversion is not possible.
     *
     * @param logger the Logger instance to convert
     * @return the MockLogger instance
     * @throws AssertionError if the logger is not an instance of MockLogger
     */
    private MockLogger toMockLogger(final Logger logger) {
        Assertions.assertInstanceOf(MockLogger.class, logger, 
            String.format("should be MockLogger instance; actual type: %s", 
                logger != null ? logger.getClass().getName() : "null"));
        return (MockLogger) logger;
    }

    private static List<MockLoggerEvent> loggerToEvents(final Logger logger) {
        return toMockLogger(logger).getLoggerEvents();
    }

    private static MockLoggerEvent loggerIndexToEvent(Logger logger, int eventIndex) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        Assertions.assertTrue(eventIndex < loggerEvents.size(),
                String.format("should have enough logger events; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        return loggerEvents.get(eventIndex);
    }

    private static void assertMessageParts(MockLoggerEvent event, String[] messageParts) {
        final String formattedMessage = event.getFormattedMessage();
        boolean hasAllParts = Arrays.stream(messageParts).allMatch(formattedMessage::contains);
        Assertions.assertTrue(hasAllParts,
                String.format("should contain all expected message parts; expected parts: %s; actual message: %s", String.join(", ", messageParts), event.getFormattedMessage()));
    }

    private static void assertMarker(MockLoggerEvent event, Marker expectedMarker) {
        Assertions.assertSame(expectedMarker, event.getMarker(),
                String.format("should have expected marker; expected: %s, actual: %s", expectedMarker, event.getMarker()));
    }

    private static void assertLevel(MockLoggerEvent event, Level expectedLevel) {
        Assertions.assertSame(expectedLevel, event.getLevel(),
                String.format("should have expected log level; expected: %s, actual: %s", expectedLevel, event.getLevel()));
    }

    private static void assertThrowableOfInstance(Throwable throwable, Class<? extends Throwable> throwableClass) {
        Assertions.assertNotNull(throwable, "should have a throwable");
        Assertions.assertTrue(throwableClass.isInstance(throwable),
                String.format("should have expected throwable type; expected: %s, actual: %s",
                        throwableClass.getName(), throwable.getClass().getName()));
    }

    private static void assertThrowableHasMessageParts(Throwable throwable, String[] throwableMessageParts) {
        final String actualMessage = throwable.getMessage();
        Assertions.assertNotNull(actualMessage, "should have throwable message");
        for (final String messagePart : throwableMessageParts) {
            Assertions.assertTrue(actualMessage.contains(messagePart),
                    String.format("should contain expected throwable message part; expected: %s; actual message: %s", messagePart, actualMessage));
        }
    }

    private static boolean hasMessagePart(MockLoggerEvent event, String[] messageParts) {
        final String formattedMessage = event.getFormattedMessage();
        for (final String messagePart : messageParts) {
            if (!formattedMessage.contains(messagePart)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMarker(MockLoggerEvent event, Marker expectedMarker) {
        return expectedMarker == event.getMarker();
    }

    private static boolean isMarkerOrNull(Marker expectedMarker, MockLoggerEvent event) {
        return expectedMarker == null || isMarker(event, expectedMarker);
    }

    private static boolean isLevel(MockLoggerEvent event, Level expectedLevel) {
        return expectedLevel == event.getLevel();
    }

    private static boolean isThrowableOfInstance(Throwable throwable, Class<? extends Throwable> throwableClass) {
        return throwableClass != null && throwableClass.isInstance(throwable);
    }

    private static boolean hasMessagePart(Throwable throwable, String[] messageParts) {
        final String message = throwable.getMessage();
        if (message == null) {
            // Exceptions with no message
            return false;
        }
        for (final String messagePart : messageParts) {
            if (!message.contains(messagePart)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Asserts that the logger has recorded an event at the specified index with the expected message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param messageParts     an array of substrings that should be present in the event's message
     */
    public void assertEvent(final Logger logger, final int eventIndex, final String... messageParts) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        assertMessageParts(event, messageParts);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedMarker   the expected marker of the event
     */
    public void assertEvent(final Logger logger, final int eventIndex, final Marker expectedMarker) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        assertMarker(event, expectedMarker);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level and message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedLevel    the expected log level of the event
     * @param messageParts     an array of substrings that should be present in the event's message
     */
    public void assertEvent(final Logger logger, final int eventIndex, final Level expectedLevel, final String... messageParts) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        assertLevel(event, expectedLevel);
        assertMessageParts(event, messageParts);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected marker and message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedMarker   the expected marker of the event
     * @param messageParts     an array of substrings that should be present in the event's message
     */
    public void assertEvent(final Logger logger, final int eventIndex, final Marker expectedMarker, final String... messageParts) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        assertMarker(event, expectedMarker);
        assertMessageParts(event, messageParts);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level, marker, and message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedLevel    the expected log level of the event
     * @param expectedMarker   the expected marker of the event
     * @param messageParts     an array of substrings that should be present in the event's message
     */
    public void assertEvent(final Logger logger, final int eventIndex, final Level expectedLevel, final Marker expectedMarker, final String... messageParts) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        assertLevel(event, expectedLevel);
        assertMarker(event, expectedMarker);
        assertMessageParts(event, messageParts);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level and marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedLevel    the expected log level of the event
     * @param expectedMarker   the expected marker of the event
     */
    public void assertEvent(final Logger logger, final int eventIndex, final Level expectedLevel, final Marker expectedMarker) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        assertLevel(event, expectedLevel);
        assertMarker(event, expectedMarker);
    }

    // Methods that assert existence of at least one event matching the criteria

    /**
     * Asserts that the logger has recorded at least one event containing the expected message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param messageParts     an array of substrings that should be present in at least one event's message
     */
    public void assertHasEvent(final Logger logger, final String... messageParts) {
        final boolean hasEvent = loggerToEvents(logger).stream()
            .anyMatch(event -> hasMessagePart(event, messageParts));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event containing expected message parts; expected: %s", String.join(", ", messageParts)));
    }

    /**
     * Asserts that the logger has recorded at least one event with the expected marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedMarker   the expected marker
     */
    public void assertHasEvent(final Logger logger, final Marker expectedMarker) {
        final boolean hasEvent = loggerToEvents(logger).stream()
            .anyMatch(event -> isMarker(event, expectedMarker));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected marker; expected: %s", expectedMarker));
    }

    /**
     * Asserts that the logger has recorded at least one event with the expected level and message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedLevel    the expected log level
     * @param messageParts     an array of substrings that should be present in the event's message
     */
    public void assertHasEvent(final Logger logger, final Level expectedLevel, final String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> isLevel(event, expectedLevel) && hasMessagePart(event, messageParts));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected level and message parts; expected level: %s, expected messages: %s", 
                expectedLevel, String.join(", ", messageParts)));
    }



    /**
     * Asserts that the logger has recorded at least one event with the expected marker and message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedMarker   the expected marker
     * @param messageParts     an array of substrings that should be present in the event's message
     */
    public void assertHasEvent(final Logger logger, final Marker expectedMarker, final String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> isMarker(event, expectedMarker) && hasMessagePart(event, messageParts));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected marker and message parts; expected marker: %s, expected messages: %s", 
                expectedMarker, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the logger has recorded at least one event with the expected level, marker, and all message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedLevel    the expected log level
     * @param expectedMarker   the expected marker
     * @param messageParts     an array of substrings that should all be present in the event's message
     */
    public void assertHasEvent(final Logger logger, final Level expectedLevel, final Marker expectedMarker, final String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> isLevel(event, expectedLevel) && isMarkerOrNull(expectedMarker, event) && hasMessagePart(event, messageParts));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected level, marker and all message parts; expected level: %s, expected marker: %s, expected messages: %s", 
                expectedLevel, expectedMarker, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the logger has recorded at least one event with the expected level and marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedLevel    the expected log level
     * @param expectedMarker   the expected marker
     */
    public void assertHasEvent(final Logger logger, final Level expectedLevel, final Marker expectedMarker) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> expectedLevel == event.getLevel() && isMarker(event, expectedMarker));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected level and marker; expected level: %s, expected marker: %s", 
                expectedLevel, expectedMarker));
    }

    // Methods for asserting throwable-related properties

    /**
     * Asserts that the logger has recorded an event at the specified index with a throwable of the expected type.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param throwableClass   the expected throwable class
     */
    public void assertEventWithThrowable(final Logger logger, final int eventIndex, final Class<? extends Throwable> throwableClass) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        final Throwable throwable = event.getThrowable();
        assertThrowableOfInstance(throwable, throwableClass);
    }

    public void assertEventWithThrowable(final Logger logger, final int eventIndex, final String... throwableMessageParts) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        final Throwable throwable = event.getThrowable();
        assertThrowableHasMessageParts(throwable, throwableMessageParts);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with a throwable of the expected type and message.
     *
     * @param logger                the Logger instance to check (must be a MockLogger)
     * @param eventIndex            the index of the event to check
     * @param throwableClass        the expected throwable class
     * @param throwableMessageParts a list of substrings that should be present in the throwable's message
     */
    public void assertEventWithThrowable(final Logger logger, final int eventIndex, final Class<? extends Throwable> throwableClass, final String... throwableMessageParts) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        final Throwable throwable = event.getThrowable();
        assertThrowableOfInstance(throwable, throwableClass);
        assertThrowableHasMessageParts(throwable, throwableMessageParts);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with any throwable.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     */
    public void assertEventHasThrowable(final Logger logger, final int eventIndex) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        Assertions.assertNotNull(event.getThrowable(), "should have a throwable");
    }

    /**
     * Asserts that the logger has recorded at least one event with a throwable of the expected type.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param throwableClass   the expected throwable class
     */
    public void assertHasEventWithThrowable(final Logger logger, final Class<? extends Throwable> throwableClass) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> isThrowableOfInstance(event.getThrowable(), throwableClass));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected throwable type; expected: %s", throwableClass.getName()));
    }

    /**
     * Asserts that the logger has recorded at least one event with a throwable of the expected type and message parts.
     *
     * @param logger                the Logger instance to check (must be a MockLogger)
     * @param throwableClass        the expected throwable class
     * @param throwableMessageParts a list of substrings that should be present in the throwable's message
     */
    public void assertHasEventWithThrowable(final Logger logger, final Class<? extends Throwable> throwableClass, final String... throwableMessageParts) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> isThrowableOfInstance(event.getThrowable(), throwableClass) && hasMessagePart(event.getThrowable(), throwableMessageParts));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected throwable type and message parts; expected type: %s, expected messages: %s", 
                throwableClass.getName(), String.join(", ", throwableMessageParts)));
    }

    /**
     * Asserts that the logger has recorded at least one event with any throwable.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     */
    public void assertHasEventWithThrowable(final Logger logger) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> event.getThrowable() != null);
        Assertions.assertTrue(hasEvent, "should have at least one event with a throwable");
    }

    // Negative assertion methods (no event matching the criteria in any position)

    public void assertNoEvent(final Logger logger, final String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> hasMessagePart(event, messageParts));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events containing message parts; unexpected message parts: %s", String.join(", ", messageParts)));
    }

    public void assertNoEvent(final Logger logger, final Marker expectedMarker) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> isMarker(event, expectedMarker));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with marker; unexpected marker: %s", expectedMarker));
    }

    public void assertNoEvent(final Logger logger, final Level expectedLevel, final String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> isLevel(event, expectedLevel) && hasMessagePart(event, messageParts));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with level and message parts; unexpected level: %s, unexpected messages: %s", expectedLevel, String.join(", ", messageParts)));
    }

    public void assertNoEvent(final Logger logger, final Marker expectedMarker, final String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> isMarker(event, expectedMarker) && hasMessagePart(event, messageParts));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with marker and message parts; unexpected marker: %s, unexpected messages: %s", expectedMarker, String.join(", ", messageParts)));
    }

    public void assertNoEvent(final Logger logger, final Level expectedLevel, final Marker expectedMarker, final String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> isLevel(event, expectedLevel) && isMarker(event, expectedMarker) && hasMessagePart(event, messageParts));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with level, marker and message parts; unexpected level: %s, unexpected marker: %s, unexpected messages: %s", expectedLevel, expectedMarker, String.join(", ", messageParts)));
    }

    public void assertNoEvent(final Logger logger, final Level expectedLevel, final Marker expectedMarker) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> isLevel(event, expectedLevel) && isMarker(event, expectedMarker));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with level and marker; unexpected level: %s, unexpected marker: %s", expectedLevel, expectedMarker));
    }

    public void assertNoEventWithThrowable(final Logger logger) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> event.getThrowable() != null);
        Assertions.assertFalse(hasEvent, "should have no events with a throwable");
    }

    public void assertNoEventWithThrowable(final Logger logger, final Class<? extends Throwable> throwableClass) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> isThrowableOfInstance(event.getThrowable(), throwableClass));
        Assertions.assertFalse(hasEvent, String.format("should have no events with throwable type; unexpected type: %s", throwableClass.getName()));
    }

    public void assertNoEventWithThrowable(final Logger logger, final Class<? extends Throwable> throwableClass, final String... throwableMessageParts) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> isThrowableOfInstance(event.getThrowable(), throwableClass) && hasMessagePart(event.getThrowable(), throwableMessageParts));
        Assertions.assertFalse(hasEvent, String.format("should have no events with throwable type and message parts; unexpected type: %s, unexpected messages: %s", throwableClass.getName(), String.join(", ", throwableMessageParts)));
    }

    public void assertNoEventWithThrowable(final Logger logger, final String... throwableMessageParts) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> hasMessagePart(event.getThrowable(), throwableMessageParts));
        Assertions.assertFalse(hasEvent, String.format("should have no events with throwable type and message parts; unexpected messages: %s", String.join(", ", throwableMessageParts)));
    }

    // Per-index negative assertions (negation of assertEvent...)

    public void assertEventNot(final Logger logger, final int eventIndex, final String... messageParts) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        // assert individually that the message does not contain all provided parts
        Assertions.assertFalse(java.util.Arrays.stream(messageParts).allMatch(part -> event.getFormattedMessage().contains(part)),
            String.format("event at index %d should NOT contain all expected message parts; unexpected parts: %s; actual message: %s", eventIndex, String.join(", ", messageParts), event.getFormattedMessage()));
    }

    public void assertEventNot(final Logger logger, final int eventIndex, final Marker expectedMarker) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        // assert that the event marker is not the forbidden marker
        Assertions.assertFalse(expectedMarker == event.getMarker(),
            String.format("event at index %d should NOT have the expected marker; forbidden: %s; actual: %s", eventIndex, expectedMarker, event.getMarker()));
    }

    public void assertEventNot(final Logger logger, final int eventIndex, final Level expectedLevel, final String... messageParts) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        // assert separately that level is not the forbidden level
        Assertions.assertFalse(isLevel(event, expectedLevel),
            String.format("event at index %d should NOT have the forbidden level; forbidden level: %s; actual level: %s", eventIndex, expectedLevel, event.getLevel()));
        // assert that the event does not contain all forbidden message parts
        Assertions.assertFalse(java.util.Arrays.stream(messageParts).allMatch(part -> event.getFormattedMessage().contains(part)),
            String.format("event at index %d should NOT contain all expected message parts; unexpected messages: %s; actual message: %s", eventIndex, String.join(", ", messageParts), event.getFormattedMessage()));
    }

    public void assertEventNot(final Logger logger, final int eventIndex, final Marker expectedMarker, final String... messageParts) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        // assert separately that the marker is not the forbidden marker
        Assertions.assertFalse(isMarker(event, expectedMarker),
            String.format("event at index %d should NOT have the forbidden marker; forbidden marker: %s; actual marker: %s", eventIndex, expectedMarker, event.getMarker()));
        // assert that the event does not contain all forbidden message parts
        Assertions.assertFalse(java.util.Arrays.stream(messageParts).allMatch(part -> event.getFormattedMessage().contains(part)),
            String.format("event at index %d should NOT contain all expected message parts; unexpected messages: %s; actual message: %s", eventIndex, String.join(", ", messageParts), event.getFormattedMessage()));
    }

    public void assertEventNot(final Logger logger, final int eventIndex, final Level expectedLevel, final Marker expectedMarker, final String... messageParts) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        // assert separately for each provided condition
        Assertions.assertFalse(isLevel(event, expectedLevel),
            String.format("event at index %d should NOT have the forbidden level; forbidden level: %s; actual level: %s", eventIndex, expectedLevel, event.getLevel()));
        if (expectedMarker != null) {
            Assertions.assertFalse(isMarker(event, expectedMarker),
                String.format("event at index %d should NOT have the forbidden marker; forbidden marker: %s; actual marker: %s", eventIndex, expectedMarker, event.getMarker()));
        }
        Assertions.assertFalse(java.util.Arrays.stream(messageParts).allMatch(part -> event.getFormattedMessage().contains(part)),
            String.format("event at index %d should NOT contain all expected message parts; unexpected messages: %s; actual message: %s", eventIndex, String.join(", ", messageParts), event.getFormattedMessage()));
    }

    public void assertEventNot(final Logger logger, final int eventIndex, final Level expectedLevel, final Marker expectedMarker) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        // assert separately that level and marker are not the forbidden ones
        Assertions.assertFalse(isLevel(event, expectedLevel),
            String.format("event at index %d should NOT have the forbidden level; forbidden level: %s; actual level: %s", eventIndex, expectedLevel, event.getLevel()));
        Assertions.assertFalse(isMarker(event, expectedMarker),
            String.format("event at index %d should NOT have the forbidden marker; forbidden marker: %s; actual marker: %s", eventIndex, expectedMarker, event.getMarker()));
    }

    public void assertEventNotWithThrowable(final Logger logger, final int eventIndex) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        Assertions.assertNull(event.getThrowable(), String.format("event at index %d should NOT have a throwable; actual: %s", eventIndex, event.getThrowable()));
    }

    public void assertEventNotWithThrowable(final Logger logger, final int eventIndex, final Class<? extends Throwable> throwableClass) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        final Throwable throwable = event.getThrowable();
        Assertions.assertFalse(throwable != null && throwableClass.isInstance(throwable),
            String.format("event at index %d should NOT have throwable of type %s; actual: %s", eventIndex, throwableClass.getName(), throwable == null ? "null" : throwable.getClass().getName()));
    }

    public void assertEventNotWithThrowable(final Logger logger, final int eventIndex, final Class<? extends Throwable> throwableClass, final String... throwableMessageParts) {
        final MockLoggerEvent event = loggerIndexToEvent(logger, eventIndex);
        final Throwable throwable = event.getThrowable();
        if (throwable == null) {
            return; // null throwable already satisfies 'not' condition
        }
        if (!throwableClass.isInstance(throwable)) {
            return; // different type, also satisfies 'not' condition
        }
        final String actualMessage = throwable.getMessage();
        final boolean matchesMessage = actualMessage != null && java.util.Arrays.stream(throwableMessageParts).allMatch(part -> actualMessage.contains(part));
        Assertions.assertFalse(matchesMessage, String.format("event at index %d should NOT have throwable of type %s with message parts %s; actual throwable message: %s", eventIndex, throwableClass.getName(), String.join(", ", throwableMessageParts), actualMessage));
    }

    // Methods for asserting event counts

    /**
     * Asserts that the logger has recorded the expected number of events.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     * @param expectedCount  the expected number of events
     */
    public void assertEventCount(final Logger logger, final int expectedCount) {
        final MockLogger mockLogger = toMockLogger(logger);
        final int actualCount = mockLogger.getEventCount();
        Assertions.assertEquals(expectedCount, actualCount, 
            String.format("should have expected number of events; expected: %d, actual: %d", expectedCount, actualCount));
    }

    /**
     * Asserts that the logger has recorded no events.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     */
    public void assertNoEvents(final Logger logger) {
        assertEventCount(logger, 0);
    }

    /**
     * Asserts that the logger has recorded the expected number of events with the specified level.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     * @param level          the log level to count
     * @param expectedCount  the expected number of events with the specified level
     */
    public void assertEventCountByLevel(final Logger logger, final Level level, final int expectedCount) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final long actualCount = loggerEvents.stream()
            .filter(event -> isLevel(event, level))
            .count();
        Assertions.assertEquals(expectedCount, actualCount, 
            String.format("should have expected number of events with level %s; expected: %d, actual: %d", 
                level, expectedCount, actualCount));
    }

    /**
     * Asserts that the logger has recorded the expected number of events with the specified marker.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     * @param marker         the marker to count
     * @param expectedCount  the expected number of events with the specified marker
     */
    public void assertEventCountByMarker(final Logger logger, final Marker marker, final int expectedCount) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final long actualCount = loggerEvents.stream()
            .filter(event -> isMarker(event, marker))
            .count();
        Assertions.assertEquals(expectedCount, actualCount, 
            String.format("should have expected number of events with marker %s; expected: %d, actual: %d", 
                marker, expectedCount, actualCount));
    }

    /**
     * Asserts that the logger has recorded the expected number of events containing the specified message part.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     * @param messagePart    a substring that should be present in the event's message
     * @param expectedCount  the expected number of events containing the message part
     */
    public void assertEventCountByMessage(final Logger logger, final String messagePart, final int expectedCount) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        final long actualCount = loggerEvents.stream()
            .filter(event -> event.getFormattedMessage().contains(messagePart))
            .count();
        Assertions.assertEquals(expectedCount, actualCount, 
            String.format("should have expected number of events containing message part '%s'; expected: %d, actual: %d", 
                messagePart, expectedCount, actualCount));
    }

    // Methods for asserting event sequences

    /**
     * Asserts that the logger has recorded events in the exact sequence of log levels specified.
     *
     * @param logger          the Logger instance to check (must be a MockLogger)
     * @param expectedLevels  the expected sequence of log levels
     */
    public void assertEventSequence(final Logger logger, final Level... expectedLevels) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);

        Assertions.assertEquals(expectedLevels.length, loggerEvents.size(), 
            String.format("should have expected number of events for sequence; expected: %d, actual: %d", 
                expectedLevels.length, loggerEvents.size()));
        
        for (int i = 0; i < expectedLevels.length; i++) {
            final Level actualLevel = loggerEvents.get(i).getLevel();
            Assertions.assertSame(expectedLevels[i], actualLevel, 
                String.format("should have expected level at position %d; expected: %s, actual: %s", 
                    i, expectedLevels[i], actualLevel));
        }
    }

    /**
     * Asserts that the logger has recorded events in the exact sequence of markers specified.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedMarkers  the expected sequence of markers
     */
    public void assertEventSequence(final Logger logger, final Marker... expectedMarkers) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);

        Assertions.assertEquals(expectedMarkers.length, loggerEvents.size(), 
            String.format("should have expected number of events for sequence; expected: %d, actual: %d", 
                expectedMarkers.length, loggerEvents.size()));
        
        for (int i = 0; i < expectedMarkers.length; i++) {
            final Marker actualMarker = loggerEvents.get(i).getMarker();
            Assertions.assertEquals(expectedMarkers[i], actualMarker, 
                String.format("should have expected marker at position %d; expected: %s, actual: %s", 
                    i, expectedMarkers[i], actualMarker));
        }
    }

    /**
     * Asserts that the logger has recorded events containing message parts in the exact sequence specified.
     *
     * @param logger              the Logger instance to check (must be a MockLogger)
     * @param expectedMessageParts the expected sequence of message parts
     */
    public void assertEventSequence(final Logger logger, final String... expectedMessageParts) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);

        Assertions.assertEquals(expectedMessageParts.length, loggerEvents.size(), 
            String.format("should have expected number of events for sequence; expected: %d, actual: %d", 
                expectedMessageParts.length, loggerEvents.size()));
        
        for (int i = 0; i < expectedMessageParts.length; i++) {
            final String actualMessage = loggerEvents.get(i).getFormattedMessage();
            Assertions.assertTrue(actualMessage.contains(expectedMessageParts[i]), 
                String.format("should contain expected message part at position %d; expected: %s, actual message: %s", 
                    i, expectedMessageParts[i], actualMessage));
        }
    }
}
