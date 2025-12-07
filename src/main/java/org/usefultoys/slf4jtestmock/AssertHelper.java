package org.usefultoys.slf4jtestmock;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;

import java.util.Arrays;
import java.util.List;

/**
 * A package-private utility class with helper methods for {@link AssertLogger}.
 * <p>
 * This class centralizes common assertion logic and checks performed on {@link MockLogger}
 * and its {@link MockLoggerEvent}s. It is not intended for public use.
 * </p>
 */
@UtilityClass
class AssertHelper {
    /**
     * Extracts the list of recorded {@link MockLoggerEvent}s from a logger.
     *
     * @param logger the logger to extract events from. Must be a {@link MockLogger}.
     * @return the list of logger events.
     */
    List<MockLoggerEvent> loggerToEvents(final Logger logger) {
        return toMockLogger(logger).getLoggerEvents();
    }

    /**
     * Retrieves a specific {@link MockLoggerEvent} from a logger by its index.
     *
     * @param logger     the logger to get the event from.
     * @param eventIndex the index of the event to retrieve.
     * @return the {@link MockLoggerEvent} at the specified index.
     * @throws AssertionError if the index is out of bounds.
     */
    MockLoggerEvent loggerIndexToEvent(final Logger logger, final int eventIndex) {
        final List<MockLoggerEvent> loggerEvents = loggerToEvents(logger);
        Assertions.assertTrue(eventIndex < loggerEvents.size(),
                String.format("should have enough logger events; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        return loggerEvents.get(eventIndex);
    }

    /**
     * Asserts that a log event's formatted message contains all the specified message parts.
     *
     * @param event        the log event to check.
     * @param messageParts the substrings expected to be in the log message.
     * @throws AssertionError if the message does not contain all parts.
     */
    void assertMessageParts(final MockLoggerEvent event, final String[] messageParts) {
        final boolean hasAllParts = hasAllMessageParts(event, messageParts);
        Assertions.assertTrue(hasAllParts,
                String.format("should contain all expected message parts at eventIndex %d; expected parts: %s; actual message: %s",
                        event.getEventIndex(), String.join(", ", messageParts), event.getFormattedMessage()));
    }

    /**
     * Asserts that a log event's formatted message does NOT contain all the specified message parts.
     *
     * @param event        the log event to check.
     * @param messageParts the substrings expected NOT to be in the log message.
     * @throws AssertionError if the message contains all parts.
     */
    void assertMessagePartsNot(final MockLoggerEvent event, final String[] messageParts) {
        final boolean hasAllParts = hasAllMessageParts(event, messageParts);
        Assertions.assertFalse(hasAllParts,
                String.format("should not contain all expected message parts at eventIndex %d; unexpected parts: %s; actual message: %s",
                        event.getEventIndex(), String.join(", ", messageParts), event.getFormattedMessage()));
    }

    /**
     * Asserts that a log event has the expected {@link Marker}.
     *
     * @param event          the log event to check.
     * @param expectedMarker the expected marker.
     * @throws AssertionError if the event's marker does not match the expected one.
     */
    void assertMarker(final MockLoggerEvent event, final Marker expectedMarker) {
        Assertions.assertSame(expectedMarker, event.getMarker(),
                String.format("should have expected marker at eventIndex %d; expected: %s, actual: %s", event.getEventIndex(), expectedMarker, event.getMarker()));
    }

    /**
     * Asserts that a log event does NOT have the unexpected {@link Marker}.
     *
     * @param event          the log event to check.
     * @param unexpectedMarker the marker expected NOT to be present.
     * @throws AssertionError if the event's marker matches the unexpected one.
     */
    void assertMarkerNot(final MockLoggerEvent event, final Marker unexpectedMarker) {
        Assertions.assertNotSame(unexpectedMarker, event.getMarker(),
                String.format("should not have unexpected marker at eventIndex %d; unexpected: %s", event.getEventIndex(), unexpectedMarker));
    }

    /**
     * Asserts that a log event has the expected {@link MockLoggerEvent.Level}.
     *
     * @param event         the log event to check.
     * @param expectedLevel the expected log level.
     * @throws AssertionError if the event's level does not match the expected one.
     */
    void assertLevel(final MockLoggerEvent event, final MockLoggerEvent.Level expectedLevel) {
        Assertions.assertSame(expectedLevel, event.getLevel(),
                String.format("should have expected log level at eventIndex %d; expected: %s, actual: %s", event.getEventIndex(), expectedLevel, event.getLevel()));
    }

    /**
     * Asserts that a log event does NOT have the unexpected {@link MockLoggerEvent.Level}.
     *
     * @param event          the log event to check.
     * @param unexpectedLevel the log level expected NOT to be present.
     * @throws AssertionError if the event's level matches the unexpected one.
     */
    void assertLevelNot(final MockLoggerEvent event, final MockLoggerEvent.Level unexpectedLevel) {
        Assertions.assertNotSame(unexpectedLevel, event.getLevel(),
                String.format("should not have unexpected log level at eventIndex %d; unexpected: %s", event.getEventIndex(), unexpectedLevel));
    }

    /**
     * Asserts that a {@link Throwable} is not null and is an instance of the expected class.
     *
     * @param event                  the log event to check.
     * @param throwable              the throwable to check.
     * @param expectedThrowableClass the expected class of the throwable.
     * @throws AssertionError if the throwable is null or not of the expected type.
     */
    void assertThrowableOfInstance(final MockLoggerEvent event, final Throwable throwable, final Class<? extends Throwable> expectedThrowableClass) {
        Assertions.assertNotNull(throwable, String.format("should have a throwable at eventIndex %d", event.getEventIndex()));
        Assertions.assertTrue(expectedThrowableClass.isInstance(throwable),
                String.format("should have expected throwable type at eventIndex %d; expected: %s, actual: %s",
                        event.getEventIndex(), expectedThrowableClass.getName(), throwable.getClass().getName()));
    }

    /**
     * Asserts that a {@link Throwable} is NOT an instance of the unexpected class.
     * If the throwable is null, this assertion passes.
     *
     * @param event                    the log event to check.
     * @param throwable                the throwable to check.
     * @param unexpectedThrowableClass the class of the throwable expected NOT to be.
     * @throws AssertionError if the throwable is not null and is an instance of the unexpected type.
     */
    void assertThrowableNotOfInstance(final MockLoggerEvent event, final Throwable throwable, final Class<? extends Throwable> unexpectedThrowableClass) {
        if (throwable != null) {
            Assertions.assertFalse(unexpectedThrowableClass.isInstance(throwable),
                    String.format("should not have unexpected throwable type at eventIndex %d; unexpected: %s, actual: %s",
                            event.getEventIndex(), unexpectedThrowableClass.getName(), throwable.getClass().getName()));
        }
    }

    /**
     * Asserts that a {@link Throwable}'s message contains all the specified message parts.
     *
     * @param event                 the log event to check.
     * @param throwable             the throwable to check.
     * @param throwableMessageParts the substrings expected to be in the throwable's message.
     * @throws AssertionError if the throwable's message does not contain all parts.
     */
    void assertThrowableHasMessageParts(final MockLoggerEvent event, final Throwable throwable, final String[] throwableMessageParts) {
        final boolean hasAllParts = hasAllMessageParts(throwable, throwableMessageParts);
        Assertions.assertTrue(hasAllParts,
                String.format("should contain all expected message parts in throwable at eventIndex %d; expected parts: %s; actual message: %s", event.getEventIndex(), String.join(", ", throwableMessageParts), throwable));
    }

    /**
     * Asserts that a {@link Throwable}'s message does NOT contain all the specified message parts.
     * If the throwable is null, this assertion passes.
     *
     * @param event                 the log event to check.
     * @param throwable             the throwable to check.
     * @param throwableMessageParts the substrings expected NOT to be in the throwable's message.
     * @throws AssertionError if the throwable's message contains all parts.
     */
    void assertThrowableHasMessagePartsNot(final MockLoggerEvent event, final Throwable throwable, final String[] throwableMessageParts) {
        if (throwable != null) {
            final boolean hasAllParts = hasAllMessageParts(throwable, throwableMessageParts);
            Assertions.assertFalse(hasAllParts,
                    String.format("should not contain all expected message parts in throwable at eventIndex %d; unexpected parts: %s; actual message: %s", event.getEventIndex(), String.join(", ", throwableMessageParts), throwable));
        }
    }

    /**
     * Checks if a log event's formatted message contains all specified substrings.
     *
     * @param event        the log event.
     * @param messageParts the array of substrings to check for.
     * @return {@code true} if the message contains all parts, {@code false} otherwise.
     */
    boolean hasAllMessageParts(final MockLoggerEvent event, final String[] messageParts) {
        final String formattedMessage = event.getFormattedMessage();
        return Arrays.stream(messageParts).allMatch(formattedMessage::contains);
    }

    /**
     * Checks if a log event's marker is the same instance as the expected marker.
     *
     * @param event          the log event.
     * @param expectedMarker the expected marker.
     * @return {@code true} if the markers are the same, {@code false} otherwise.
     */
    boolean isMarker(final MockLoggerEvent event, final Marker expectedMarker) {
        return expectedMarker == event.getMarker();
    }

    /**
     * Checks if a log event's marker matches the expected one, or if the expected marker is null.
     *
     * @param expectedMarker the expected marker, can be null.
     * @param event          the log event.
     * @return {@code true} if the expected marker is null or matches the event's marker.
     */
    boolean isMarkerOrNull(final Marker expectedMarker, final MockLoggerEvent event) {
        return expectedMarker == null || isMarker(event, expectedMarker);
    }

    /**
     * Checks if a log event's level is the same as the expected level.
     *
     * @param event         the log event.
     * @param expectedLevel the expected level.
     * @return {@code true} if the levels are the same, {@code false} otherwise.
     */
    boolean isLevel(final MockLoggerEvent event, final MockLoggerEvent.Level expectedLevel) {
        return expectedLevel == event.getLevel();
    }

    /**
     * Checks if a throwable is an instance of a given class.
     *
     * @param throwable              the throwable to check.
     * @param expectedThrowableClass the class to check against.
     * @return {@code true} if the throwable is an instance of the class, {@code false} otherwise.
     */
    boolean isThrowableOfInstance(final Throwable throwable, final Class<? extends Throwable> expectedThrowableClass) {
        return expectedThrowableClass != null && expectedThrowableClass.isInstance(throwable);
    }

    /**
     * Checks if a throwable's message contains all specified substrings.
     *
     * @param throwable    the throwable to check.
     * @param messageParts the array of substrings to check for.
     * @return {@code true} if the message is not null and contains all parts, {@code false} otherwise.
     */
    boolean hasAllMessageParts(final Throwable throwable, final String[] messageParts) {
        final String message = throwable.getMessage();
        return message != null && Arrays.stream(messageParts).allMatch(message::contains);
    }

    /**
     * Converts a Logger instance to MockLogger, throwing an assertion error if the conversion is not possible.
     *
     * @param logger the Logger instance to convert
     * @return the MockLogger instance
     * @throws AssertionError if the logger is not an instance of MockLogger
     */
    MockLogger toMockLogger(final Logger logger) {
        Assertions.assertInstanceOf(MockLogger.class, logger,
                String.format("should be MockLogger instance; actual type: %s",
                        logger != null ? logger.getClass().getName() : "null"));
        return (MockLogger) logger;
    }
}
