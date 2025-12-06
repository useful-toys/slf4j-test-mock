/*
 * Copyright (c) 2024, coming soon
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.usefultoys.slf4jtestmock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.impl.MockLoggerEvent;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AssertHelper}.
 */
@AIGenerated("gemini")
@DisplayName("AssertHelper Test Suite")
class AssertHelperTest {

    private static final Marker MARKER_1 = MarkerFactory.getMarker("MARKER_1");
    private static final Marker MARKER_2 = MarkerFactory.getMarker("MARKER_2");

    private MockLoggerEvent createEvent(MockLoggerEvent.Level level, Marker marker, String message, Throwable throwable) {
        return new MockLoggerEvent(0, "test-logger", level, Collections.emptyMap(), marker, throwable, message);
    }

    @Nested
    @DisplayName("assertMessageParts() tests")
    class AssertMessagePartsTest {
        @Test
        @DisplayName("should pass if message contains all parts")
        void shouldPassWhenAllPartsPresent() {
            final MockLoggerEvent event = createEvent(MockLoggerEvent.Level.INFO, null, "This is a test message.", null);
            assertDoesNotThrow(() -> AssertHelper.assertMessageParts(event, new String[]{"This is", "test message"}), "should not throw when parts are present");
        }

        @Test
        @DisplayName("should fail if message does not contain all parts")
        void shouldFailWhenNotAllPartsPresent() {
            final MockLoggerEvent event = createEvent(MockLoggerEvent.Level.INFO, null, "This is a test message.", null);
            assertThrows(AssertionError.class, () -> AssertHelper.assertMessageParts(event, new String[]{"This is", "another message"}), "should throw when parts are missing");
        }
    }

    @Nested
    @DisplayName("assertMarker() tests")
    class AssertMarkerTest {
        @Test
        @DisplayName("should pass if markers are the same")
        void shouldPassForSameMarker() {
            final MockLoggerEvent event = createEvent(MockLoggerEvent.Level.INFO, MARKER_1, "message", null);
            assertDoesNotThrow(() -> AssertHelper.assertMarker(event, MARKER_1), "should not throw for same marker");
        }

        @Test
        @DisplayName("should fail if markers are different")
        void shouldFailForDifferentMarker() {
            final MockLoggerEvent event = createEvent(MockLoggerEvent.Level.INFO, MARKER_1, "message", null);
            assertThrows(AssertionError.class, () -> AssertHelper.assertMarker(event, MARKER_2), "should throw for different marker");
        }
    }

    @Nested
    @DisplayName("assertLevel() tests")
    class AssertLevelTest {
        @Test
        @DisplayName("should pass if levels are the same")
        void shouldPassForSameLevel() {
            final MockLoggerEvent event = createEvent(MockLoggerEvent.Level.INFO, null, "message", null);
            assertDoesNotThrow(() -> AssertHelper.assertLevel(event, MockLoggerEvent.Level.INFO), "should not throw for same level");
        }

        @Test
        @DisplayName("should fail if levels are different")
        void shouldFailForDifferentLevel() {
            final MockLoggerEvent event = createEvent(MockLoggerEvent.Level.INFO, null, "message", null);
            assertThrows(AssertionError.class, () -> AssertHelper.assertLevel(event, MockLoggerEvent.Level.WARN), "should throw for different level");
        }
    }

    @Nested
    @DisplayName("assertThrowableOfInstance() tests")
    class AssertThrowableOfInstanceTest {
        @Test
        @DisplayName("should pass for same class or superclass")
        void shouldPassForSameOrSuperclass() {
            final MockLoggerEvent event = createEvent(MockLoggerEvent.Level.ERROR, null, "message", new IllegalArgumentException("error"));
            final Throwable throwable = event.getThrowable();
            assertAll("should pass for valid class hierarchies",
                    () -> assertDoesNotThrow(() -> AssertHelper.assertThrowableOfInstance(event, throwable, IllegalArgumentException.class), "should pass for same class"),
                    () -> assertDoesNotThrow(() -> AssertHelper.assertThrowableOfInstance(event, throwable, RuntimeException.class), "should pass for superclass")
            );
        }

        @Test
        @DisplayName("should fail for a different class")
        void shouldFailForWrongType() {
            final MockLoggerEvent event = createEvent(MockLoggerEvent.Level.ERROR, null, "message", new IllegalArgumentException("error"));
            final Throwable throwable = event.getThrowable();
            assertThrows(AssertionError.class, () -> AssertHelper.assertThrowableOfInstance(event, throwable, IllegalStateException.class), "should throw for different class");
        }
    }

    @Nested
    @DisplayName("assertThrowableHasMessageParts() tests")
    class AssertThrowableHasMessagePartsTest {
        @Test
        @DisplayName("should pass if throwable message contains all parts")
        void shouldPassWhenAllPartsPresent() {
            final MockLoggerEvent event = createEvent(MockLoggerEvent.Level.ERROR, null, "message", new RuntimeException("This is an error."));
            final Throwable throwable = event.getThrowable();
            assertDoesNotThrow(() -> AssertHelper.assertThrowableHasMessageParts(event, throwable, new String[]{"is an", "error"}), "should not throw when parts are present");
        }

        @Test
        @DisplayName("should fail if throwable message does not contain all parts")
        void shouldFailWhenNotAllPartsPresent() {
            final MockLoggerEvent event = createEvent(MockLoggerEvent.Level.ERROR, null, "message", new RuntimeException("This is an error."));
            final Throwable throwable = event.getThrowable();
            assertThrows(AssertionError.class, () -> AssertHelper.assertThrowableHasMessageParts(event, throwable, new String[]{"is an", "mistake"}), "should throw when parts are missing");
        }
    }

    @Nested
    @DisplayName("hasAllMessageParts() for event tests")
    class HasAllMessagePartsForEventTest {
        @Test
        @DisplayName("should return true if message contains all parts")
        void shouldReturnTrueWhenAllPartsPresent() {
            final MockLoggerEvent event = createEvent(MockLoggerEvent.Level.INFO, null, "This is a test message.", null);
            assertTrue(AssertHelper.hasAllMessageParts(event, new String[]{"is a", "message"}), "should be true when all parts are found");
        }

        @Test
        @DisplayName("should return false if message does not contain all parts")
        void shouldReturnFalseWhenPartsMissing() {
            final MockLoggerEvent event = createEvent(MockLoggerEvent.Level.INFO, null, "This is a test message.", null);
            assertFalse(AssertHelper.hasAllMessageParts(event, new String[]{"is a", "payload"}), "should be false when parts are missing");
        }
    }

    @Nested
    @DisplayName("isMarker() tests")
    class IsMarkerTest {
        @Test
        @DisplayName("should return true for same marker, false otherwise")
        void shouldReturnCorrectBoolean() {
            final MockLoggerEvent eventWithMarker = createEvent(MockLoggerEvent.Level.INFO, MARKER_1, "message", null);
            final MockLoggerEvent eventWithoutMarker = createEvent(MockLoggerEvent.Level.INFO, null, "message", null);

            assertAll("should correctly identify markers",
                    () -> assertTrue(AssertHelper.isMarker(eventWithMarker, MARKER_1), "should be true for same marker"),
                    () -> assertFalse(AssertHelper.isMarker(eventWithMarker, MARKER_2), "should be false for different marker"),
                    () -> assertFalse(AssertHelper.isMarker(eventWithoutMarker, MARKER_1), "should be false for null marker")
            );
        }
    }

    @Nested
    @DisplayName("isMarkerOrNull() tests")
    class IsMarkerOrNullTest {
        @Test
        @DisplayName("should return true for null or matching marker")
        void shouldReturnTrueForNullOrMatchingMarker() {
            final MockLoggerEvent eventWithMarker = createEvent(MockLoggerEvent.Level.INFO, MARKER_1, "message", null);
            final MockLoggerEvent eventWithoutMarker = createEvent(MockLoggerEvent.Level.INFO, null, "message", null);

            assertAll("should handle null and matching markers",
                    () -> assertTrue(AssertHelper.isMarkerOrNull(null, eventWithMarker), "should be true for null expected marker"),
                    () -> assertTrue(AssertHelper.isMarkerOrNull(null, eventWithoutMarker), "should be true for null expected and actual marker"),
                    () -> assertTrue(AssertHelper.isMarkerOrNull(MARKER_1, eventWithMarker), "should be true for matching marker")
            );
        }

        @Test
        @DisplayName("should return false for a non-matching marker")
        void shouldReturnFalseForNonMatchingMarker() {
            final MockLoggerEvent eventWithMarker = createEvent(MockLoggerEvent.Level.INFO, MARKER_1, "message", null);
            assertFalse(AssertHelper.isMarkerOrNull(MARKER_2, eventWithMarker), "should be false for non-matching marker");
        }
    }

    @Nested
    @DisplayName("isLevel() tests")
    class IsLevelTest {
        @Test
        @DisplayName("should return true for same level, false otherwise")
        void shouldReturnCorrectBoolean() {
            final MockLoggerEvent event = createEvent(MockLoggerEvent.Level.DEBUG, null, "message", null);
            assertTrue(AssertHelper.isLevel(event, MockLoggerEvent.Level.DEBUG), "should be true for same level");
            assertFalse(AssertHelper.isLevel(event, MockLoggerEvent.Level.INFO), "should be false for different level");
        }
    }

    @Nested
    @DisplayName("isThrowableOfInstance() tests")
    class IsThrowableOfInstanceTest {
        @Test
        @DisplayName("should return true for same class or superclass")
        void shouldReturnTrueForSameOrSuperclass() {
            final Throwable throwable = new IllegalArgumentException("error");
            assertTrue(AssertHelper.isThrowableOfInstance(throwable, IllegalArgumentException.class), "should be true for same class");
            assertTrue(AssertHelper.isThrowableOfInstance(throwable, RuntimeException.class), "should be true for superclass");
        }

        @Test
        @DisplayName("should return false for different class or null")
        void shouldReturnFalseForDifferentClassOrNull() {
            final Throwable throwable = new IllegalArgumentException("error");
            assertFalse(AssertHelper.isThrowableOfInstance(throwable, IllegalStateException.class), "should be false for different class");
            assertFalse(AssertHelper.isThrowableOfInstance(throwable, null), "should be false for null class");
        }
    }

    @Nested
    @DisplayName("hasAllMessageParts() for throwable tests")
    class HasAllMessagePartsForThrowableTest {
        @Test
        @DisplayName("should return true if message contains all parts")
        void shouldReturnTrueWhenAllPartsPresent() {
            final Throwable throwable = new RuntimeException("This is an error message.");
            assertTrue(AssertHelper.hasAllMessageParts(throwable, new String[]{"error", "message"}), "should be true when all parts are found");
        }

        @Test
        @DisplayName("should return false if message does not contain all parts")
        void shouldReturnFalseWhenPartsMissing() {
            final Throwable throwable = new RuntimeException("This is an error message.");
            assertFalse(AssertHelper.hasAllMessageParts(throwable, new String[]{"error", "payload"}), "should be false when parts are missing");
        }

        @Test
        @DisplayName("should return false if throwable message is null")
        void shouldReturnFalseForNullMessage() {
            final Throwable throwableWithoutMessage = new RuntimeException();
            assertFalse(AssertHelper.hasAllMessageParts(throwableWithoutMessage, new String[]{"error"}), "should be false for null message");
        }
    }
}
