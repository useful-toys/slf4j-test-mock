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
import org.slf4j.Logger;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;

import java.util.List;

/**
 * Utility class for formatting logged events in a readable format.
 * Used for debugging failed assertions by displaying all captured log events.
 *
 * @author Daniel Felix Ferber
 */
public final class LoggerEventFormatter {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private LoggerEventFormatter() {
        // Utility class
    }

    /**
     * Formats all logged events from a logger into a readable string.
     * Shows event index, level, marker (if any), and message for each event.
     *
     * @param logger the Logger instance (must be a MockLogger)
     * @return a formatted string containing all logged events
     */
    public static String formatLoggedEvents(final Logger logger) {
        if (!(logger instanceof MockLogger)) {
            return "  (logger is not a MockLogger)";
        }

        final MockLogger mockLogger = (MockLogger) logger;
        final List<MockLoggerEvent> events = mockLogger.getLoggerEvents();

        if (events.isEmpty()) {
            return "  (no events logged)";
        }

        final StringBuilder sb = new StringBuilder(256);
        sb.append(String.format("  Total events: %d%n", events.size()));

        for (final MockLoggerEvent event : events) {
            sb.append(String.format("  [%d] %-5s", event.getEventIndex(), event.getLevel()));

            if (event.getMarker() != null) {
                sb.append(String.format(" | marker=%-20s", event.getMarker().getName()));
            } else {
                sb.append(" |");
            }

            sb.append(String.format(" | %s%n", event.getFormattedMessage()));

            if (event.getThrowable() != null) {
                sb.append(String.format("        └─ throwable: %s%n", event.getThrowable().getClass().getSimpleName()));
            }
        }

        return sb.toString();
    }
}

