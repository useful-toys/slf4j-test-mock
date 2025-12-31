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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerFactory;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AIGenerated("copilot")
@DisplayName("LoggerEventFormatter")
class LoggerEventFormatterTest {

    @Nested
    @DisplayName("formatLoggedEvents")
    class FormatLoggedEvents {

        @Test
        @DisplayName("should return non-MockLogger message when logger is not a MockLogger")
        void shouldReturnNonMockLoggerMessageWhenLoggerIsNotAMockLogger() {
            final Logger logger = newNonMockLogger("unit.not-mock");

            final String formatted = LoggerEventFormatter.formatLoggedEvents(logger);

            assertEquals("  (logger is not a MockLogger)", formatted,
                "should return the non-MockLogger message for non-MockLogger implementations");
        }

        @Test
        @DisplayName("should return no-events message when MockLogger has no events")
        void shouldReturnNoEventsMessageWhenMockLoggerHasNoEvents() {
            final Logger logger = newMockLogger("unit.empty");
            assertTrue(logger instanceof MockLogger, "should create a MockLogger in tests");

            final String formatted = LoggerEventFormatter.formatLoggedEvents(logger);

            assertEquals("  (no events logged)", formatted,
                "should return the no-events message when no events were logged");
        }

        @Test
        @DisplayName("should format marker, message, and throwable details")
        void shouldFormatMarkerMessageAndThrowableDetails() {
            final Logger logger = newMockLogger("unit.events");
            assertTrue(logger instanceof MockLogger, "should create a MockLogger in tests");

            final Marker marker = MarkerFactory.getMarker("SECURITY");
            logger.info(marker, "marker event");
            logger.error("error with throwable", new IllegalStateException("boom"));
            logger.info("plain event");

            final String formatted = LoggerEventFormatter.formatLoggedEvents(logger);

            assertTrue(formatted.contains("Total events: 3"), "should include total event count");
            assertTrue(formatted.contains("marker=SECURITY"), "should include marker name when present");
            assertTrue(formatted.contains("marker event"), "should include formatted message for marker event");
            assertTrue(formatted.contains("throwable: IllegalStateException"), "should include throwable type when present");
            assertTrue(formatted.contains("plain event"), "should include formatted message for non-marker event");
        }
    }

    private static Logger newMockLogger(final String name) {
        final ILoggerFactory factory = MockLoggerFactory.getInstance();
        return factory.getLogger(name + "." + System.nanoTime());
    }

    private static Logger newNonMockLogger(final String name) {
        return (Logger) Proxy.newProxyInstance(
            LoggerEventFormatterTest.class.getClassLoader(),
            new Class<?>[]{Logger.class},
            (proxy, method, args) -> {
                if ("getName".equals(method.getName())) {
                    return name;
                }
                if (method.getReturnType() == void.class) {
                    return null;
                }
                if (method.getReturnType() == boolean.class) {
                    return false;
                }
                if (method.getReturnType() == int.class) {
                    return 0;
                }
                if (method.getReturnType() == long.class) {
                    return 0L;
                }
                return null;
            }
        );
    }
}
