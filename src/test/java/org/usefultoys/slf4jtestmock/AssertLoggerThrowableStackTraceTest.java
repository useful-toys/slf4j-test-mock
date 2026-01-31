/*
 * Copyright 2026 Daniel Felix Ferber
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
import org.slf4j.impl.MockLogger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AssertLogger} stack trace assertions.
 * <p>
 * Tests validate that AssertLogger correctly validates throwable stack traces
 * as specified in issue #52.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Stack Trace Validation:</b> Tests for assertEventThrowableStackTraceContains, assertHasEventThrowableStackTraceContains, assertNoEventThrowableStackTraceContains</li>
 *   <li><b>Fragment Matching:</b> Single and multiple fragment matching in stack traces</li>
 *   <li><b>Edge Cases:</b> Empty stack traces, null throwables, deep stacks, missing fragments</li>
 * </ul>
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
 */
@DisplayName("AssertLogger - Stack Trace Assertions")
public final class AssertLoggerThrowableStackTraceTest {

    @Nested
    @DisplayName("assertEventThrowableStackTraceContains")
    class AssertEventThrowableStackTraceContains {

        @Test
        @DisplayName("should pass when stack trace contains expected fragment")
        void shouldPassWhenStackTraceContainsExpectedFragment() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = createExceptionWithKnownStackTrace();
            logger.error("Error occurred", exception);

            AssertLogger.assertEventThrowableStackTraceContains(logger, 0,
                    RuntimeException.class,
                    "AssertLoggerThrowableStackTraceTest");
        }

        @Test
        @DisplayName("should pass when stack trace contains multiple expected fragments")
        void shouldPassWhenStackTraceContainsMultipleExpectedFragments() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = createExceptionWithKnownStackTrace();
            logger.error("Error occurred", exception);

            AssertLogger.assertEventThrowableStackTraceContains(logger, 0,
                    RuntimeException.class,
                    "createExceptionWithKnownStackTrace",
                    "AssertLoggerThrowableStackTraceTest");
        }

        @Test
        @DisplayName("should throw when stack trace does not contain expected fragment")
        void shouldThrowWhenStackTraceDoesNotContainExpectedFragment() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = createExceptionWithKnownStackTrace();
            logger.error("Error occurred", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableStackTraceContains(logger, 0,
                            RuntimeException.class,
                            "NonExistentMethod"));
            assertTrue(error.getMessage().contains("should have all expected fragments"), "should indicate missing fragment");
            assertTrue(error.getMessage().contains("NonExistentMethod"), "should show expected fragment");
        }

        @Test
        @DisplayName("should throw when one of multiple fragments is missing")
        void shouldThrowWhenOneOfMultipleFragmentsIsMissing() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = createExceptionWithKnownStackTrace();
            logger.error("Error occurred", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableStackTraceContains(logger, 0,
                            RuntimeException.class,
                            "AssertLoggerThrowableStackTraceTest",
                            "NonExistentMethod"));
            assertTrue(error.getMessage().contains("should have all expected fragments"), "should indicate missing fragment");
        }

        @Test
        @DisplayName("should throw when event has no throwable")
        void shouldThrowWhenEventHasNoThrowable() {
            final Logger logger = new MockLogger("test");
            logger.info("No exception");

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableStackTraceContains(logger, 0,
                            RuntimeException.class,
                            "SomeMethod"));
            assertTrue(error.getMessage().contains("should have a throwable"), "should indicate missing throwable");
        }

        @Test
        @DisplayName("should throw when throwable type does not match")
        void shouldThrowWhenThrowableTypeDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = createExceptionWithKnownStackTrace();
            logger.error("Error occurred", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableStackTraceContains(logger, 0,
                            IllegalArgumentException.class,
                            "AssertLoggerThrowableStackTraceTest"));
            assertTrue(error.getMessage().contains("should have expected throwable type"), "should indicate type mismatch");
        }

        @Test
        @DisplayName("should throw when event index is out of bounds")
        void shouldThrowWhenEventIndexIsOutOfBounds() {
            final Logger logger = new MockLogger("test");

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableStackTraceContains(logger, 0,
                            RuntimeException.class,
                            "SomeMethod"));
            assertTrue(error.getMessage().contains("should have enough logger events"), "should indicate missing event");
        }

        @Test
        @DisplayName("should handle deep stack traces")
        void shouldHandleDeepStackTraces() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = createDeepStackTrace(30);
            logger.error("Deep error", exception);

            AssertLogger.assertEventThrowableStackTraceContains(logger, 0,
                    RuntimeException.class,
                    "deepMethod");
        }
    }

    @Nested
    @DisplayName("assertHasEventThrowableStackTraceContains")
    class AssertHasEventThrowableStackTraceContains {

        @Test
        @DisplayName("should pass when at least one event has throwable with expected stack trace fragments")
        void shouldPassWhenAtLeastOneEventHasThrowableWithExpectedStackTraceFragments() {
            final Logger logger = new MockLogger("test");
            logger.info("Normal log");
            final RuntimeException exception = createExceptionWithKnownStackTrace();
            logger.error("Error occurred", exception);
            logger.warn("Warning");

            AssertLogger.assertHasEventThrowableStackTraceContains(logger,
                    RuntimeException.class,
                    "AssertLoggerThrowableStackTraceTest");
        }

        @Test
        @DisplayName("should pass when multiple events have throwable with expected stack trace fragments")
        void shouldPassWhenMultipleEventsHaveThrowableWithExpectedStackTraceFragments() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception1 = createExceptionWithKnownStackTrace();
            logger.error("Error 1", exception1);
            final RuntimeException exception2 = createExceptionWithKnownStackTrace();
            logger.error("Error 2", exception2);

            AssertLogger.assertHasEventThrowableStackTraceContains(logger,
                    RuntimeException.class,
                    "createExceptionWithKnownStackTrace");
        }

        @Test
        @DisplayName("should throw when no event has throwable with expected stack trace fragments")
        void shouldThrowWhenNoEventHasThrowableWithExpectedStackTraceFragments() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = createExceptionWithKnownStackTrace();
            logger.error("Error", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertHasEventThrowableStackTraceContains(logger,
                            RuntimeException.class,
                            "NonExistentMethod"));
            assertTrue(error.getMessage().contains("should have at least one event"), "should indicate no matching event");
            assertTrue(error.getMessage().contains("NonExistentMethod"), "should show expected fragment");
        }

        @Test
        @DisplayName("should throw when all events have no throwable")
        void shouldThrowWhenAllEventsHaveNoThrowable() {
            final Logger logger = new MockLogger("test");
            logger.info("Message 1");
            logger.warn("Message 2");

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertHasEventThrowableStackTraceContains(logger,
                            RuntimeException.class,
                            "SomeMethod"));
            assertTrue(error.getMessage().contains("should have at least one event"), "should indicate no matching event");
        }
    }

    @Nested
    @DisplayName("assertNoEventThrowableStackTraceContains")
    class AssertNoEventThrowableStackTraceContains {

        @Test
        @DisplayName("should pass when no event has throwable with unexpected stack trace fragments")
        void shouldPassWhenNoEventHasThrowableWithUnexpectedStackTraceFragments() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = createExceptionWithKnownStackTrace();
            logger.error("Error", exception);

            AssertLogger.assertNoEventThrowableStackTraceContains(logger,
                    RuntimeException.class,
                    "NonExistentMethod");
        }

        @Test
        @DisplayName("should pass when all events have no throwable")
        void shouldPassWhenAllEventsHaveNoThrowable() {
            final Logger logger = new MockLogger("test");
            logger.info("Message 1");
            logger.warn("Message 2");

            AssertLogger.assertNoEventThrowableStackTraceContains(logger,
                    RuntimeException.class,
                    "SomeMethod");
        }

        @Test
        @DisplayName("should throw when at least one event has throwable with unexpected stack trace fragments")
        void shouldThrowWhenAtLeastOneEventHasThrowableWithUnexpectedStackTraceFragments() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = createExceptionWithKnownStackTrace();
            logger.error("Error", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertNoEventThrowableStackTraceContains(logger,
                            RuntimeException.class,
                            "AssertLoggerThrowableStackTraceTest"));
            assertTrue(error.getMessage().contains("should have no events"), "should indicate unexpected event");
            assertTrue(error.getMessage().contains("AssertLoggerThrowableStackTraceTest"), "should show unexpected fragment");
        }

        @Test
        @DisplayName("should pass when throwable type does not match")
        void shouldPassWhenThrowableTypeDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = createExceptionWithKnownStackTrace();
            logger.error("Error", exception);

            // Different throwable type, so assertion should pass even with matching fragments
            AssertLogger.assertNoEventThrowableStackTraceContains(logger,
                    IllegalArgumentException.class,
                    "AssertLoggerThrowableStackTraceTest");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle exception with empty stack trace")
        void shouldHandleExceptionWithEmptyStackTrace() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("Empty stack");
            exception.setStackTrace(new StackTraceElement[0]);
            logger.error("Error", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableStackTraceContains(logger, 0,
                            RuntimeException.class,
                            "SomeMethod"));
            assertTrue(error.getMessage().contains("(empty stack trace)"), "should indicate empty stack trace");
        }

        @Test
        @DisplayName("should match fragment in any stack frame")
        void shouldMatchFragmentInAnyStackFrame() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = createExceptionFromNestedCall();
            logger.error("Error", exception);

            // Fragment should be found in any frame, not just the top
            AssertLogger.assertEventThrowableStackTraceContains(logger, 0,
                    RuntimeException.class,
                    "nestedMethodLevel");
        }

        @Test
        @DisplayName("should truncate long stack traces in error message")
        void shouldTruncateLongStackTracesInErrorMessage() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = createDeepStackTrace(20);
            logger.error("Deep error", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableStackTraceContains(logger, 0,
                            RuntimeException.class,
                            "NonExistentMethod"));
            assertTrue(error.getMessage().contains(" more"), "should truncate long stack traces");
        }
    }

    // Helper methods to create exceptions with known stack traces

    private RuntimeException createExceptionWithKnownStackTrace() {
        return new RuntimeException("Test exception from known location");
    }

    private RuntimeException createExceptionFromNestedCall() {
        return nestedMethodLevel1();
    }

    private RuntimeException nestedMethodLevel1() {
        return nestedMethodLevel2();
    }

    private RuntimeException nestedMethodLevel2() {
        return new RuntimeException("Nested exception");
    }

    private RuntimeException createDeepStackTrace(final int depth) {
        if (depth <= 0) {
            return new RuntimeException("Deep exception");
        }
        try {
            return deepMethod(depth);
        } catch (final RuntimeException e) {
            return e;
        }
    }

    private RuntimeException deepMethod(final int depth) {
        if (depth == 1) {
            throw new RuntimeException("Deep exception at level " + depth);
        }
        return deepMethod(depth - 1);
    }
}
