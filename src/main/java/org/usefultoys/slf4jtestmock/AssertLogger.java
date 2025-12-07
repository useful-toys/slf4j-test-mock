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

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.slf4j.impl.MockLoggerEvent.Level;

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
     * Asserts that the logger has recorded an event at the specified index with the expected message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param messageParts     an array of substrings that should be present in the event's message
     */
    public void assertEvent(final @NonNull Logger logger, final int eventIndex, final @NonNull String... messageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        AssertHelper.assertMessageParts(event, messageParts);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedMarker   the expected marker of the event
     */
    public void assertEvent(final @NonNull Logger logger, final int eventIndex, final @NonNull Marker expectedMarker) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        AssertHelper.assertMarker(event, expectedMarker);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level and message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedLevel    the expected log level of the event
     * @param messageParts     an array of substrings that should be present in the event's message
     */
    public void assertEvent(final @NonNull Logger logger, final int eventIndex, final @NonNull Level expectedLevel, final @NonNull String... messageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        AssertHelper.assertLevel(event, expectedLevel);
        AssertHelper.assertMessageParts(event, messageParts);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected marker and message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedMarker   the expected marker of the event
     * @param messageParts     an array of substrings that should be present in the event's message
     */
    public void assertEvent(final @NonNull Logger logger, final int eventIndex, final @NonNull Marker expectedMarker, final @NonNull String... messageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        AssertHelper.assertMarker(event, expectedMarker);
        AssertHelper.assertMessageParts(event, messageParts);
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
    public void assertEvent(final @NonNull Logger logger, final int eventIndex, final @NonNull Level expectedLevel, final @NonNull Marker expectedMarker, final @NonNull String... messageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        AssertHelper.assertLevel(event, expectedLevel);
        AssertHelper.assertMarker(event, expectedMarker);
        AssertHelper.assertMessageParts(event, messageParts);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level and marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedLevel    the expected log level of the event
     * @param expectedMarker   the expected marker of the event
     */
    public void assertEvent(final @NonNull Logger logger, final int eventIndex, final @NonNull Level expectedLevel, final @NonNull Marker expectedMarker) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        AssertHelper.assertLevel(event, expectedLevel);
        AssertHelper.assertMarker(event, expectedMarker);
    }

    // Methods that assert existence of at least one event matching the criteria

    /**
     * Asserts that the logger has recorded at least one event containing the expected message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param messageParts     an array of substrings that should be present in at least one event's message
     */
    public void assertHasEvent(final @NonNull Logger logger, final @NonNull String... messageParts) {
        final boolean hasEvent = AssertHelper.loggerToEvents(logger).stream()
            .anyMatch(event -> AssertHelper.hasAllMessageParts(event, messageParts));
        Assertions.assertTrue(hasEvent,
            String.format("should have at least one event containing expected message parts; expected: %s", String.join(", ", messageParts)));
    }

    /**
     * Asserts that the logger has recorded at least one event with the expected marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedMarker   the expected marker
     */
    public void assertHasEvent(final @NonNull Logger logger, final @NonNull Marker expectedMarker) {
        final boolean hasEvent = AssertHelper.loggerToEvents(logger).stream()
            .anyMatch(event -> AssertHelper.isMarker(event, expectedMarker));
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
    public void assertHasEvent(final @NonNull Logger logger, final @NonNull Level expectedLevel, final @NonNull String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isLevel(event, expectedLevel) && AssertHelper.hasAllMessageParts(event, messageParts));
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
    public void assertHasEvent(final @NonNull Logger logger, final @NonNull Marker expectedMarker, final @NonNull String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isMarker(event, expectedMarker) && AssertHelper.hasAllMessageParts(event, messageParts));
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
    public void assertHasEvent(final @NonNull Logger logger, final @NonNull Level expectedLevel, final Marker expectedMarker, final @NonNull String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isLevel(event, expectedLevel) && AssertHelper.isMarkerOrNull(expectedMarker, event) && AssertHelper.hasAllMessageParts(event, messageParts));
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
    public void assertHasEvent(final @NonNull Logger logger, final @NonNull Level expectedLevel, final @NonNull Marker expectedMarker) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> expectedLevel == event.getLevel() && AssertHelper.isMarker(event, expectedMarker));
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
     * @param expectedThrowableClass   the expected throwable class
     */
    public void assertEventWithThrowable(final @NonNull Logger logger, final int eventIndex, final @NonNull Class<? extends Throwable> expectedThrowableClass) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final Throwable throwable = event.getThrowable();
        AssertHelper.assertThrowableOfInstance(event, throwable, expectedThrowableClass);
    }

    public void assertEventWithThrowable(final @NonNull Logger logger, final int eventIndex, final @NonNull String... throwableMessageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final Throwable throwable = event.getThrowable();
        Assertions.assertNotNull(throwable, String.format("should have a throwable at eventIndex %d", event.getEventIndex()));
        AssertHelper.assertThrowableHasMessageParts(event, throwable, throwableMessageParts);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with a throwable of the expected type and message.
     *
     * @param logger                the Logger instance to check (must be a MockLogger)
     * @param eventIndex            the index of the event to check
     * @param expectedThrowableClass        the expected throwable class
     * @param throwableMessageParts a list of substrings that should be present in the throwable's message
     */
    public void assertEventWithThrowable(final @NonNull Logger logger, final int eventIndex, final @NonNull Class<? extends Throwable> expectedThrowableClass, final @NonNull String... throwableMessageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final Throwable throwable = event.getThrowable();
        AssertHelper.assertThrowableOfInstance(event, throwable, expectedThrowableClass);
        AssertHelper.assertThrowableHasMessageParts(event, throwable, throwableMessageParts);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with any throwable.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     */
    public void assertEventHasThrowable(final @NonNull Logger logger, final int eventIndex) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        Assertions.assertNotNull(event.getThrowable(), String.format("should have a throwable at eventIndex %d", event.getEventIndex()));
    }

    /**
     * Asserts that the logger has recorded at least one event with a throwable of the expected type.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedThrowableClass   the expected throwable class
     */
    public void assertHasEventWithThrowable(final @NonNull Logger logger, final @NonNull Class<? extends Throwable> expectedThrowableClass) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> AssertHelper.isThrowableOfInstance(event.getThrowable(), expectedThrowableClass));
        Assertions.assertTrue(hasEvent,
            String.format("should have at least one event with expected throwable type; expected: %s", expectedThrowableClass.getName()));
    }

    /**
     * Asserts that the logger has recorded at least one event with a throwable of the expected type and message parts.
     *
     * @param logger                the Logger instance to check (must be a MockLogger)
     * @param expectedThrowableClass        the expected throwable class
     * @param throwableMessageParts a list of substrings that should be present in the throwable's message
     */
    public void assertHasEventWithThrowable(final @NonNull Logger logger, final @NonNull Class<? extends Throwable> expectedThrowableClass, final @NonNull String... throwableMessageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isThrowableOfInstance(event.getThrowable(), expectedThrowableClass) && AssertHelper.hasAllMessageParts(event.getThrowable(), throwableMessageParts));
        Assertions.assertTrue(hasEvent,
            String.format("should have at least one event with expected throwable type and message parts; expected type: %s, expected messages: %s",
                expectedThrowableClass.getName(), String.join(", ", throwableMessageParts)));
    }

    /**
     * Asserts that the logger has recorded at least one event with any throwable.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     */
    public void assertHasEventWithThrowable(final @NonNull Logger logger) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> event.getThrowable() != null);
        Assertions.assertTrue(hasEvent, "should have at least one event with a throwable");
    }

    // Negative assertion methods (no event matching the criteria in any position)

    public void assertNoEvent(final @NonNull Logger logger, final @NonNull String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> AssertHelper.hasAllMessageParts(event, messageParts));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events containing message parts; unexpected message parts: %s", String.join(", ", messageParts)));
    }

    public void assertNoEvent(final @NonNull Logger logger, final @NonNull Marker expectedMarker) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> AssertHelper.isMarker(event, expectedMarker));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with marker; unexpected marker: %s", expectedMarker));
    }

    public void assertNoEvent(final @NonNull Logger logger, final @NonNull Level expectedLevel, final @NonNull String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> AssertHelper.isLevel(event, expectedLevel) && AssertHelper.hasAllMessageParts(event, messageParts));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with level and message parts; unexpected level: %s, unexpected messages: %s", expectedLevel, String.join(", ", messageParts)));
    }

    public void assertNoEvent(final @NonNull Logger logger, final @NonNull Marker expectedMarker, final @NonNull String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isMarker(event, expectedMarker) && AssertHelper.hasAllMessageParts(event, messageParts));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with marker and message parts; unexpected marker: %s, unexpected messages: %s", expectedMarker, String.join(", ", messageParts)));
    }

    public void assertNoEvent(final @NonNull Logger logger, final @NonNull Level expectedLevel, final @NonNull Marker expectedMarker, final @NonNull String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isLevel(event, expectedLevel) && AssertHelper.isMarker(event, expectedMarker) && AssertHelper.hasAllMessageParts(event, messageParts));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with level, marker and message parts; unexpected level: %s, unexpected marker: %s, unexpected messages: %s", expectedLevel, expectedMarker, String.join(", ", messageParts)));
    }

    public void assertNoEvent(final @NonNull Logger logger, final @NonNull Level expectedLevel, final @NonNull Marker expectedMarker) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isLevel(event, expectedLevel) && AssertHelper.isMarker(event, expectedMarker));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with level and marker; unexpected level: %s, unexpected marker: %s", expectedLevel, expectedMarker));
    }

    public void assertNoEventWithThrowable(final @NonNull Logger logger) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> event.getThrowable() != null);
        Assertions.assertFalse(hasEvent, "should have no events with a throwable");
    }

    public void assertNoEventWithThrowable(final @NonNull Logger logger, final @NonNull Class<? extends Throwable> expectedThrowableClass) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isThrowableOfInstance(event.getThrowable(), expectedThrowableClass));
        Assertions.assertFalse(hasEvent, String.format("should have no events with throwable type; unexpected type: %s", expectedThrowableClass.getName()));
    }

    public void assertNoEventWithThrowable(final @NonNull Logger logger, final @NonNull Class<? extends Throwable> expectedThrowableClass, final @NonNull String... throwableMessageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isThrowableOfInstance(event.getThrowable(), expectedThrowableClass) && AssertHelper.hasAllMessageParts(event.getThrowable(), throwableMessageParts));
        Assertions.assertFalse(hasEvent, String.format("should have no events with throwable type and message parts; unexpected type: %s, unexpected messages: %s", expectedThrowableClass.getName(), String.join(", ", throwableMessageParts)));
    }

    public void assertNoEventWithThrowable(final @NonNull Logger logger, final @NonNull String... throwableMessageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> AssertHelper.hasAllMessageParts(event.getThrowable(), throwableMessageParts));
        Assertions.assertFalse(hasEvent, String.format("should have no events with throwable type and message parts; unexpected messages: %s", String.join(", ", throwableMessageParts)));
    }

    // Per-index negative assertions (negation of assertEvent...)

    public void assertEventNot(final @NonNull Logger logger, final int eventIndex, final @NonNull String... messageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.hasAllMessageParts(event, messageParts);
        Assertions.assertFalse(match, String.format("should not have event at index %d with message parts; unexpected message parts: %s", eventIndex, String.join(", ", messageParts)));
    }

    public void assertEventNot(final @NonNull Logger logger, final int eventIndex, final Marker unexpectedMarker) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.isMarker(event, unexpectedMarker);
        Assertions.assertFalse(match, String.format("should not have event at index %d with marker; unexpected marker: %s", eventIndex, unexpectedMarker));
    }

    public void assertEventNot(final @NonNull Logger logger, final int eventIndex, final Level unexpectedLevel, final @NonNull String... messageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.isLevel(event, unexpectedLevel) && AssertHelper.hasAllMessageParts(event, messageParts);
        Assertions.assertFalse(match, String.format("should not have event at index %d with level and message parts; unexpected level: %s, unexpected message parts: %s", eventIndex, unexpectedLevel, String.join(", ", messageParts)));
    }

    public void assertEventNot(final @NonNull Logger logger, final int eventIndex, final Marker unexpectedMarker, final @NonNull String... messageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.isMarker(event, unexpectedMarker) && AssertHelper.hasAllMessageParts(event, messageParts);
        Assertions.assertFalse(match, String.format("should not have event at index %d with marker and message parts; unexpected marker: %s, unexpected message parts: %s", eventIndex, unexpectedMarker, String.join(", ", messageParts)));
    }

    public void assertEventNot(final @NonNull Logger logger, final int eventIndex, final Level unexpectedLevel, final Marker unexpectedMarker, final @NonNull String... messageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.isLevel(event, unexpectedLevel) && AssertHelper.isMarker(event, unexpectedMarker) && AssertHelper.hasAllMessageParts(event, messageParts);
        Assertions.assertFalse(match, String.format("should not have event at index %d with level, marker and message parts; unexpected level: %s, unexpected marker: %s, unexpected message parts: %s", eventIndex, unexpectedLevel, unexpectedMarker, String.join(", ", messageParts)));
    }

    public void assertEventNot(final @NonNull Logger logger, final int eventIndex, final Level unexpectedLevel, final Marker unexpectedMarker) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.isLevel(event, unexpectedLevel) && AssertHelper.isMarker(event, unexpectedMarker);
        Assertions.assertFalse(match, String.format("should not have event at index %d with level and marker; unexpected level: %s, unexpected marker: %s", eventIndex, unexpectedLevel, unexpectedMarker));
    }

    public void assertEventNotWithThrowable(final @NonNull Logger logger, final int eventIndex) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        Assertions.assertNull(event.getThrowable(), String.format("event at index %d should NOT have a throwable; actual: %s", eventIndex, event.getThrowable()));
    }

    public void assertEventNotWithThrowable(final @NonNull Logger logger, final int eventIndex, final Class<? extends Throwable> unexpectedThrowableClass) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.isThrowableOfInstance(event.getThrowable(), unexpectedThrowableClass);
        Assertions.assertFalse(match, String.format("should not have event at index %d with throwable of type; unexpected type: %s", eventIndex, unexpectedThrowableClass.getName()));
    }

    public void assertEventNotWithThrowable(final @NonNull Logger logger, final int eventIndex, final Class<? extends Throwable> unexpectedThrowableClass, final @NonNull String... throwableMessageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.isThrowableOfInstance(event.getThrowable(), unexpectedThrowableClass) && AssertHelper.hasAllMessageParts(event.getThrowable(), throwableMessageParts);
        Assertions.assertFalse(match, String.format("should not have event at index %d with throwable of type and message parts; unexpected type: %s, unexpected message parts: %s", eventIndex, unexpectedThrowableClass.getName(), String.join(", ", throwableMessageParts)));
    }

    // Methods for asserting event counts

    /**
     * Asserts that the logger has recorded the expected number of events.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     * @param expectedCount  the expected number of events
     */
    public void assertEventCount(final @NonNull Logger logger, final int expectedCount) {
        final MockLogger mockLogger = AssertHelper.toMockLogger(logger);
        final int actualCount = mockLogger.getEventCount();
        Assertions.assertEquals(expectedCount, actualCount,
            String.format("should have expected number of events; expected: %d, actual: %d", expectedCount, actualCount));
    }

    /**
     * Asserts that the logger has recorded no events.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     */
    public void assertNoEvents(final @NonNull Logger logger) {
        assertEventCount(logger, 0);
    }

    /**
     * Asserts that the logger has recorded the expected number of events with the specified level.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     * @param level          the log level to count
     * @param expectedCount  the expected number of events with the specified level
     */
    public void assertEventCountByLevel(final @NonNull Logger logger, final @NonNull Level level, final int expectedCount) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final long actualCount = loggerEvents.stream()
            .filter(event -> AssertHelper.isLevel(event, level))
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
    public void assertEventCountByMarker(final @NonNull Logger logger, final @NonNull Marker marker, final int expectedCount) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final long actualCount = loggerEvents.stream()
            .filter(event -> AssertHelper.isMarker(event, marker))
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
    public void assertEventCountByMessage(final @NonNull Logger logger, final String messagePart, final int expectedCount) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
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
    public void assertEventSequence(final @NonNull Logger logger, final @NonNull Level... expectedLevels) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);

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
-     * @param logger           the Logger instance to check (must be a MockLogger)
-     * @param expectedMarkers  the expected sequence of markers
-     */
    public void assertEventSequence(final @NonNull Logger logger, final @NonNull Marker... expectedMarkers) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);

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
    public void assertEventSequence(final @NonNull Logger logger, final @NonNull String... expectedMessageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);

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
