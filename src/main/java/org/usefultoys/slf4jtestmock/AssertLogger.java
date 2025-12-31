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
public final class AssertLogger {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AssertLogger() {
        // Utility class
    }


    /**
     * Asserts that the logger has recorded an event at the specified index with the expected message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param messageParts     an array of substrings that should be present in the event's message
     */
    public static void assertEvent(final @NonNull Logger logger, final int eventIndex, final @NonNull String... messageParts) {
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
    public static void assertEvent(final @NonNull Logger logger, final int eventIndex, final @NonNull Marker expectedMarker) {
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
    public static void assertEvent(final @NonNull Logger logger, final int eventIndex, final @NonNull Level expectedLevel, final @NonNull String... messageParts) {
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
    public static void assertEvent(final @NonNull Logger logger, final int eventIndex, final @NonNull Marker expectedMarker, final @NonNull String... messageParts) {
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
    public static void assertEvent(final @NonNull Logger logger, final int eventIndex, final @NonNull Level expectedLevel, final @NonNull Marker expectedMarker, final @NonNull String... messageParts) {
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
    public static void assertEvent(final @NonNull Logger logger, final int eventIndex, final @NonNull Level expectedLevel, final @NonNull Marker expectedMarker) {
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
    public static void assertHasEvent(final @NonNull Logger logger, final @NonNull String... messageParts) {
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
    public static void assertHasEvent(final @NonNull Logger logger, final @NonNull Marker expectedMarker) {
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
    public static void assertHasEvent(final @NonNull Logger logger, final @NonNull Level expectedLevel, final @NonNull String... messageParts) {
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
    public static void assertHasEvent(final @NonNull Logger logger, final @NonNull Marker expectedMarker, final @NonNull String... messageParts) {
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
    public static void assertHasEvent(final @NonNull Logger logger, final @NonNull Level expectedLevel, final Marker expectedMarker, final @NonNull String... messageParts) {
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
    public static void assertHasEvent(final @NonNull Logger logger, final @NonNull Level expectedLevel, final @NonNull Marker expectedMarker) {
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
    public static void assertEventWithThrowable(final @NonNull Logger logger, final int eventIndex, final @NonNull Class<? extends Throwable> expectedThrowableClass) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final Throwable throwable = event.getThrowable();
        AssertHelper.assertThrowableOfInstance(event, throwable, expectedThrowableClass);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with a throwable whose message contains the expected parts.
     *
     * @param logger                the Logger instance to check (must be a MockLogger)
     * @param eventIndex            the index of the event to check
     * @param throwableMessageParts an array of substrings that should be present in the throwable's message
     */
    public static void assertEventWithThrowable(final @NonNull Logger logger, final int eventIndex, final @NonNull String... throwableMessageParts) {
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
     * @param throwableMessageParts an array of substrings that should be present in the throwable's message
     */
    public static void assertEventWithThrowable(final @NonNull Logger logger, final int eventIndex, final @NonNull Class<? extends Throwable> expectedThrowableClass, final @NonNull String... throwableMessageParts) {
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
    public static void assertEventHasThrowable(final @NonNull Logger logger, final int eventIndex) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        Assertions.assertNotNull(event.getThrowable(), String.format("should have a throwable at eventIndex %d", event.getEventIndex()));
    }

    /**
     * Asserts that the logger has recorded at least one event with a throwable of the expected type.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedThrowableClass   the expected throwable class
     */
    public static void assertHasEventWithThrowable(final @NonNull Logger logger, final @NonNull Class<? extends Throwable> expectedThrowableClass) {
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
     * @param throwableMessageParts an array of substrings that should be present in the throwable's message
     */
    public static void assertHasEventWithThrowable(final @NonNull Logger logger, final @NonNull Class<? extends Throwable> expectedThrowableClass, final @NonNull String... throwableMessageParts) {
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
    public static void assertHasEventWithThrowable(final @NonNull Logger logger) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> event.getThrowable() != null);
        Assertions.assertTrue(hasEvent, "should have at least one event with a throwable");
    }

    // Negative assertion methods (no event matching the criteria in any position)

    /**
     * Asserts that the logger has not recorded any event containing the specified message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param messageParts     an array of substrings that should not be present together in any event's message
     */
    public static void assertNoEvent(final @NonNull Logger logger, final @NonNull String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> AssertHelper.hasAllMessageParts(event, messageParts));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events containing message parts; unexpected message parts: %s", String.join(", ", messageParts)));
    }

    /**
     * Asserts that the logger has not recorded any event with the specified marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedMarker   the marker that should not be present in any event
     */
    public static void assertNoEvent(final @NonNull Logger logger, final @NonNull Marker expectedMarker) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> AssertHelper.isMarker(event, expectedMarker));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with marker; unexpected marker: %s", expectedMarker));
    }

    /**
     * Asserts that the logger has not recorded any event with the specified level and message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedLevel    the log level that should not be present in any event
     * @param messageParts     an array of substrings that should not be present together in any event's message
     */
    public static void assertNoEvent(final @NonNull Logger logger, final @NonNull Level expectedLevel, final @NonNull String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> AssertHelper.isLevel(event, expectedLevel) && AssertHelper.hasAllMessageParts(event, messageParts));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with level and message parts; unexpected level: %s, unexpected messages: %s", expectedLevel, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the logger has not recorded any event with the specified marker and message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedMarker   the marker that should not be present in any event
     * @param messageParts     an array of substrings that should not be present together in any event's message
     */
    public static void assertNoEvent(final @NonNull Logger logger, final @NonNull Marker expectedMarker, final @NonNull String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isMarker(event, expectedMarker) && AssertHelper.hasAllMessageParts(event, messageParts));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with marker and message parts; unexpected marker: %s, unexpected messages: %s", expectedMarker, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the logger has not recorded any event with the specified level, marker and message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedLevel    the log level that should not be present in any event
     * @param expectedMarker   the marker that should not be present in any event
     * @param messageParts     an array of substrings that should not be present together in any event's message
     */
    public static void assertNoEvent(final @NonNull Logger logger, final @NonNull Level expectedLevel, final @NonNull Marker expectedMarker, final @NonNull String... messageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isLevel(event, expectedLevel) && AssertHelper.isMarker(event, expectedMarker) && AssertHelper.hasAllMessageParts(event, messageParts));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with level, marker and message parts; unexpected level: %s, unexpected marker: %s, unexpected messages: %s", expectedLevel, expectedMarker, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the logger has not recorded any event with the specified level and marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedLevel    the log level that should not be present in any event
     * @param expectedMarker   the marker that should not be present in any event
     */
    public static void assertNoEvent(final @NonNull Logger logger, final @NonNull Level expectedLevel, final @NonNull Marker expectedMarker) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isLevel(event, expectedLevel) && AssertHelper.isMarker(event, expectedMarker));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with level and marker; unexpected level: %s, unexpected marker: %s", expectedLevel, expectedMarker));
    }

    /**
     * Asserts that the logger has not recorded any event with a throwable.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     */
    public static void assertNoEventWithThrowable(final @NonNull Logger logger) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> event.getThrowable() != null);
        Assertions.assertFalse(hasEvent, "should have no events with a throwable");
    }

    /**
     * Asserts that the logger has not recorded any event with a throwable of the specified type.
     *
     * @param logger                    the Logger instance to check (must be a MockLogger)
     * @param expectedThrowableClass    the throwable class that should not be present in any event
     */
    public static void assertNoEventWithThrowable(final @NonNull Logger logger, final @NonNull Class<? extends Throwable> expectedThrowableClass) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isThrowableOfInstance(event.getThrowable(), expectedThrowableClass));
        Assertions.assertFalse(hasEvent, String.format("should have no events with throwable type; unexpected type: %s", expectedThrowableClass.getName()));
    }

    /**
     * Asserts that the logger has not recorded any event with a throwable of the specified type and message parts.
     *
     * @param logger                    the Logger instance to check (must be a MockLogger)
     * @param expectedThrowableClass    the throwable class that should not be present in any event
     * @param throwableMessageParts     an array of substrings that should not be present in the throwable's message
     */
    public static void assertNoEventWithThrowable(final @NonNull Logger logger, final @NonNull Class<? extends Throwable> expectedThrowableClass, final @NonNull String... throwableMessageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.isThrowableOfInstance(event.getThrowable(), expectedThrowableClass) && AssertHelper.hasAllMessageParts(event.getThrowable(), throwableMessageParts));
        Assertions.assertFalse(hasEvent, String.format("should have no events with throwable type and message parts; unexpected type: %s, unexpected messages: %s", expectedThrowableClass.getName(), String.join(", ", throwableMessageParts)));
    }

    /**
     * Asserts that the logger has not recorded any event with a throwable containing the specified message parts.
     *
     * @param logger                 the Logger instance to check (must be a MockLogger)
     * @param throwableMessageParts  an array of substrings that should not be present in the throwable's message
     */
    public static void assertNoEventWithThrowable(final @NonNull Logger logger, final @NonNull String... throwableMessageParts) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
                .anyMatch(event -> AssertHelper.hasAllMessageParts(event.getThrowable(), throwableMessageParts));
        Assertions.assertFalse(hasEvent, String.format("should have no events with throwable type and message parts; unexpected messages: %s", String.join(", ", throwableMessageParts)));
    }

    // Argument assertions

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected argument at the given argument index.
     * <p>
     * This assertion validates the raw arguments stored in the event (not the formatted message).
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param argumentIndex    the argument index (0-based)
     * @param expectedArgument the expected argument (may be null)
     */
    @AIGenerated("copilot")
    public static void assertEventWithArgument(final @NonNull Logger logger, final int eventIndex, final int argumentIndex, final Object expectedArgument) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        AssertHelper.assertArgumentAtIndex(event, argumentIndex, expectedArgument);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with exactly the expected arguments.
     * <p>
     * This assertion validates argument count and order.
     *
     * @param logger             the Logger instance to check (must be a MockLogger)
     * @param eventIndex         the index of the event to check
     * @param expectedArguments  the expected arguments (may be empty)
     */
    @AIGenerated("copilot")
    public static void assertEventWithArguments(final @NonNull Logger logger, final int eventIndex, final Object... expectedArguments) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        AssertHelper.assertArgumentsExactly(event, expectedArguments);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected argument.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedArgument the expected argument
     */
    public static void assertEventHasArgument(final @NonNull Logger logger, final int eventIndex, final Object expectedArgument) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        AssertHelper.assertHasArgument(event, expectedArgument);
    }

    /**
     * Asserts that the logger has recorded at least one event with the expected argument.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedArgument the expected argument
     */
    public static void assertHasEventWithArgument(final @NonNull Logger logger, final Object expectedArgument) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> AssertHelper.hasArgument(event, expectedArgument));
        Assertions.assertTrue(hasEvent,
            String.format("should have at least one event with expected argument; expected: %s", expectedArgument));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected argument count.
     * <p>
     * This assertion validates only the number of arguments stored in the event (not their values).
     *
     * @param logger                the Logger instance to check (must be a MockLogger)
     * @param eventIndex            the index of the event to check
     * @param expectedArgumentCount the expected argument count
     */
    @AIGenerated("copilot")
    public static void assertEventHasArgumentCount(final @NonNull Logger logger, final int eventIndex, final int expectedArgumentCount) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        AssertHelper.assertArgumentCount(event, expectedArgumentCount);
    }

    /**
     * Asserts that the logger has recorded at least one event with the expected argument count.
     * <p>
     * This assertion validates only the number of arguments stored in the event (not their values).
     *
     * @param logger                the Logger instance to check (must be a MockLogger)
     * @param expectedArgumentCount the expected argument count
     */
    @AIGenerated("copilot")
    public static void assertHasEventWithArgumentCount(final @NonNull Logger logger, final int expectedArgumentCount) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream().anyMatch(event -> AssertHelper.hasArgumentCount(event, expectedArgumentCount));
        Assertions.assertTrue(hasEvent,
            String.format("should have at least one event with expected argument count; expected: %d", expectedArgumentCount));
    }

    /**
     * Asserts that the logger has no recorded events with the expected argument count.
     * <p>
     * This assertion validates only the number of arguments stored in the event (not their values).
     *
     * @param logger                the Logger instance to check (must be a MockLogger)
     * @param expectedArgumentCount the argument count that must not be present in any event
     */
    @AIGenerated("copilot")
    public static void assertNoEventWithArgumentCount(final @NonNull Logger logger, final int expectedArgumentCount) {
        final List<MockLoggerEvent> loggerEvents = AssertHelper.loggerToEvents(logger);
        final boolean hasEvent = loggerEvents.stream().anyMatch(event -> AssertHelper.hasArgumentCount(event, expectedArgumentCount));
        Assertions.assertFalse(hasEvent,
            String.format("should have no events with expected argument count; unexpected count: %d", expectedArgumentCount));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index that does NOT have the unexpected argument.
     * <p>
     * This assertion validates raw arguments stored in the event (not the formatted message).
     *
     * @param logger             the Logger instance to check (must be a MockLogger)
     * @param eventIndex         the index of the event to check
     * @param unexpectedArgument the argument that should NOT be present in any argument position (may be null)
     */
    @AIGenerated("copilot")
    public static void assertEventNotHasArgument(final @NonNull Logger logger, final int eventIndex, final Object unexpectedArgument) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.hasArgument(event, unexpectedArgument);
        Assertions.assertFalse(match,
            String.format("should not have event at index %d with argument; unexpected argument: %s", eventIndex, unexpectedArgument));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index that does NOT have the unexpected argument at the given argument index.
     * <p>
     * If the event does not have an argument at the requested argument index, this assertion passes.
     *
     * @param logger             the Logger instance to check (must be a MockLogger)
     * @param eventIndex         the index of the event to check
     * @param argumentIndex      the argument index (0-based)
     * @param unexpectedArgument the argument that should NOT be present at that argument index (may be null)
     */
    @AIGenerated("copilot")
    public static void assertEventNotWithArgument(final @NonNull Logger logger, final int eventIndex, final int argumentIndex, final Object unexpectedArgument) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final Object[] arguments = AssertHelper.safeArguments(event);
        final boolean match = argumentIndex >= 0 && argumentIndex < arguments.length
            && AssertHelper.argumentEquals(unexpectedArgument, arguments[argumentIndex]);
        Assertions.assertFalse(match,
            String.format("should not have event at index %d with argument at argumentIndex %d; unexpected argument: %s", eventIndex, argumentIndex, unexpectedArgument));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index that does NOT have exactly the unexpected arguments.
     * <p>
     * This assertion validates argument count and order.
     *
     * @param logger              the Logger instance to check (must be a MockLogger)
     * @param eventIndex          the index of the event to check
     * @param unexpectedArguments the arguments that should NOT match exactly (may be empty)
     */
    @AIGenerated("copilot")
    public static void assertEventNotWithArguments(final @NonNull Logger logger, final int eventIndex, final Object... unexpectedArguments) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.hasArgumentsExactly(event, unexpectedArguments);
        Assertions.assertFalse(match,
            String.format("should not have event at index %d with exact arguments; unexpected arguments: %s", eventIndex, java.util.Arrays.deepToString(unexpectedArguments == null ? new Object[0] : unexpectedArguments)));
    }

    // Per-index negative assertions (negation of assertEvent...)

    /**
     * Asserts that the logger has recorded an event at the specified index that does NOT contain the given message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param messageParts     an array of substrings that should NOT be present in the event's message
     */
    public static void assertEventNot(final @NonNull Logger logger, final int eventIndex, final @NonNull String... messageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.hasAllMessageParts(event, messageParts);
        Assertions.assertFalse(match, String.format("should not have event at index %d with message parts; unexpected message parts: %s", eventIndex, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index that does NOT have the given marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param unexpectedMarker the marker that should NOT be present in the event
     */
    public static void assertEventNot(final @NonNull Logger logger, final int eventIndex, final Marker unexpectedMarker) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.isMarker(event, unexpectedMarker);
        Assertions.assertFalse(match, String.format("should not have event at index %d with marker; unexpected marker: %s", eventIndex, unexpectedMarker));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index that does NOT have the given level AND message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param unexpectedLevel  the log level that should NOT be present in the event
     * @param messageParts     an array of substrings that should NOT be present in the event's message
     */
    public static void assertEventNot(final @NonNull Logger logger, final int eventIndex, final Level unexpectedLevel, final @NonNull String... messageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.isLevel(event, unexpectedLevel) && AssertHelper.hasAllMessageParts(event, messageParts);
        Assertions.assertFalse(match, String.format("should not have event at index %d with level and message parts; unexpected level: %s, unexpected message parts: %s", eventIndex, unexpectedLevel, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index that does NOT have the given marker AND message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param unexpectedMarker the marker that should NOT be present in the event
     * @param messageParts     an array of substrings that should NOT be present in the event's message
     */
    public static void assertEventNot(final @NonNull Logger logger, final int eventIndex, final Marker unexpectedMarker, final @NonNull String... messageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.isMarker(event, unexpectedMarker) && AssertHelper.hasAllMessageParts(event, messageParts);
        Assertions.assertFalse(match, String.format("should not have event at index %d with marker and message parts; unexpected marker: %s, unexpected message parts: %s", eventIndex, unexpectedMarker, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index that does NOT have the given level, marker AND message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param unexpectedLevel  the log level that should NOT be present in the event
     * @param unexpectedMarker the marker that should NOT be present in the event
     * @param messageParts     an array of substrings that should NOT be present in the event's message
     */
    public static void assertEventNot(final @NonNull Logger logger, final int eventIndex, final Level unexpectedLevel, final Marker unexpectedMarker, final @NonNull String... messageParts) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.isLevel(event, unexpectedLevel) && AssertHelper.isMarker(event, unexpectedMarker) && AssertHelper.hasAllMessageParts(event, messageParts);
        Assertions.assertFalse(match, String.format("should not have event at index %d with level, marker and message parts; unexpected level: %s, unexpected marker: %s, unexpected message parts: %s", eventIndex, unexpectedLevel, unexpectedMarker, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index that does NOT have the given level AND marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param unexpectedLevel  the log level that should NOT be present in the event
     * @param unexpectedMarker the marker that should NOT be present in the event
     */
    public static void assertEventNot(final @NonNull Logger logger, final int eventIndex, final Level unexpectedLevel, final Marker unexpectedMarker) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.isLevel(event, unexpectedLevel) && AssertHelper.isMarker(event, unexpectedMarker);
        Assertions.assertFalse(match, String.format("should not have event at index %d with level and marker; unexpected level: %s, unexpected marker: %s", eventIndex, unexpectedLevel, unexpectedMarker));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index that does NOT have any throwable.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     */
    public static void assertEventNotWithThrowable(final @NonNull Logger logger, final int eventIndex) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        Assertions.assertNull(event.getThrowable(), String.format("event at index %d should NOT have a throwable; actual: %s", eventIndex, event.getThrowable()));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index that does NOT have a throwable of the given type.
     *
     * @param logger                   the Logger instance to check (must be a MockLogger)
     * @param eventIndex               the index of the event to check
     * @param unexpectedThrowableClass the throwable class that should NOT be present in the event
     */
    public static void assertEventNotWithThrowable(final @NonNull Logger logger, final int eventIndex, final Class<? extends Throwable> unexpectedThrowableClass) {
        final MockLoggerEvent event = AssertHelper.loggerIndexToEvent(logger, eventIndex);
        final boolean match = AssertHelper.isThrowableOfInstance(event.getThrowable(), unexpectedThrowableClass);
        Assertions.assertFalse(match, String.format("should not have event at index %d with throwable of type; unexpected type: %s", eventIndex, unexpectedThrowableClass.getName()));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index that does NOT have a throwable of the given type AND message parts.
     *
     * @param logger                   the Logger instance to check (must be a MockLogger)
     * @param eventIndex               the index of the event to check
     * @param unexpectedThrowableClass the throwable class that should NOT be present in the event
     * @param throwableMessageParts    an array of substrings that should NOT be present in the throwable's message
     */
    public static void assertEventNotWithThrowable(final @NonNull Logger logger, final int eventIndex, final Class<? extends Throwable> unexpectedThrowableClass, final @NonNull String... throwableMessageParts) {
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
    public static void assertEventCount(final @NonNull Logger logger, final int expectedCount) {
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
    public static void assertNoEvents(final @NonNull Logger logger) {
        assertEventCount(logger, 0);
    }

    /**
     * Asserts that the logger has recorded the expected number of events with the specified level.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     * @param level          the log level to count
     * @param expectedCount  the expected number of events with the specified level
     */
    public static void assertEventCountByLevel(final @NonNull Logger logger, final @NonNull Level level, final int expectedCount) {
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
    public static void assertEventCountByMarker(final @NonNull Logger logger, final @NonNull Marker marker, final int expectedCount) {
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
    public static void assertEventCountByMessage(final @NonNull Logger logger, final String messagePart, final int expectedCount) {
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
    public static void assertEventSequence(final @NonNull Logger logger, final @NonNull Level... expectedLevels) {
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
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedMarkers  the expected sequence of markers
     */
    public static void assertEventSequence(final @NonNull Logger logger, final @NonNull Marker... expectedMarkers) {
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
    public static void assertEventSequence(final @NonNull Logger logger, final @NonNull String... expectedMessageParts) {
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
