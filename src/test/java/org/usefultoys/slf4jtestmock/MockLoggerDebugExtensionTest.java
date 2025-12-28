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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Example tests demonstrating the MockLoggerDebugExtension via @WithMockLoggerDebug.
 * <p>
 * When assertions fail, the extension automatically prints all logged events,
 * making it easier to debug test failures.
 *
 * @author Daniel Felix Ferber
 */
@DisplayName("MockLoggerDebugExtension Examples")
class MockLoggerDebugExtensionTest {

    /**
     * Example of using the extension with a single logger.
     * When the assertion fails, all logged events will be printed automatically.
     */
    @Nested
    @DisplayName("Single Logger Example")
    @WithMockLoggerDebug
    class SingleLoggerExample {

        @Test
        @DisplayName("example: logs multiple events and assertion fails - shows all events")
        void exampleMultipleEventsWithFailure() {
            final Logger logger = LoggerFactory.getLogger("example.single");

            // Log some events
            logger.debug("Debug message");
            logger.info("Info message");
            logger.warn("Warning message");

            // This assertion will fail and trigger the extension to print all events
            // Output will show all 3 events logged above
            assertEquals(3, ((MockLogger) logger).getEventCount(),
                "Expected 3 events but got " + ((MockLogger) logger).getEventCount());
        }

        @Test
        @DisplayName("example: logs events with different levels")
        void exampleDifferentLogLevels() {
            final Logger logger = LoggerFactory.getLogger("example.levels");

            logger.trace("Trace level");
            logger.debug("Debug level");
            logger.info("Info level");
            logger.warn("Warn level");
            logger.error("Error level");

            // This will show all 5 events with their levels
            assertEquals(5, ((MockLogger) logger).getEventCount());
        }
    }

    /**
     * Example of using the extension with multiple loggers.
     * The extension will print events from all MockLogger instances found in parameters.
     */
    @Nested
    @DisplayName("Multiple Loggers Example")
    @WithMockLoggerDebug
    class MultipleLoggersExample {

        @Test
        @DisplayName("example: logs to multiple loggers and assertion fails")
        void exampleMultipleLoggers() {
            final Logger logger1 = LoggerFactory.getLogger("example.logger1");
            final Logger logger2 = LoggerFactory.getLogger("example.logger2");

            // Log to first logger
            logger1.info("Message to logger 1");
            logger1.warn("Warning to logger 1");

            // Log to second logger
            logger2.info("Message to logger 2");
            logger2.error("Error to logger 2");

            // This will fail and show events from both loggers
            assertEquals(2, ((MockLogger) logger1).getEventCount());
        }
    }

    /**
     * Example showing that the extension only prints when assertions fail.
     * If this test passes, no events are printed.
     */
    @Nested
    @DisplayName("Passing Test Example")
    @WithMockLoggerDebug
    class PassingTestExample {

        @Test
        @DisplayName("example: correct assertion - no debug output")
        void examplePassingTest() {
            final Logger logger = LoggerFactory.getLogger("example.passing");

            logger.info("Test message");

            // This assertion passes, so no debug output is printed
            assertEquals(1, ((MockLogger) logger).getEventCount());
        }
    }

    /**
     * Example with markers to show how they are displayed in debug output.
     */
    @Nested
    @DisplayName("Markers Example")
    @WithMockLoggerDebug
    class MarkersExample {

        @Test
        @DisplayName("example: logs with markers - shown in output when fails")
        void exampleWithMarkers() {
            final Logger logger = LoggerFactory.getLogger("example.markers");
            final org.slf4j.Marker markerA = org.slf4j.MarkerFactory.getMarker("SECURITY");
            final org.slf4j.Marker markerB = org.slf4j.MarkerFactory.getMarker("PERFORMANCE");

            logger.info(markerA, "Security event");
            logger.warn(markerB, "Performance issue");
            logger.error("Regular error");

            // Markers will be shown in the debug output
            assertEquals(3, ((MockLogger) logger).getEventCount());
        }
    }

    /**
     * Example with exceptions to show how they are displayed.
     */
    @Nested
    @DisplayName("Exceptions Example")
    @WithMockLoggerDebug
    class ExceptionsExample {

        @Test
        @DisplayName("example: logs with exceptions - shown in output when fails")
        void exampleWithExceptions() {
            final Logger logger = LoggerFactory.getLogger("example.exceptions");

            try {
                throw new RuntimeException("Test exception");
            } catch (final RuntimeException e) {
                logger.error("An error occurred", e);
            }

            logger.info("Processing continues");

            // The exception will be shown in the debug output
            assertEquals(2, ((MockLogger) logger).getEventCount());
        }
    }
}
