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

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AssertLogger} enhanced throwable assertions.
 * <p>
 * Tests validate that AssertLogger correctly validates throwable causes, suppressed exceptions,
 * and cause chains as specified in issue #31.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Throwable Cause Validation:</b> Tests for assertEventThrowableCauseIs, assertHasEventThrowableCauseIs, assertNoEventThrowableCauseIs</li>
 *   <li><b>Suppressed Exception Validation:</b> Tests for assertEventThrowableHasSuppressed, assertHasEventThrowableHasSuppressed, assertNoEventThrowableHasSuppressed</li>
 *   <li><b>Cause Chain Validation:</b> Tests for assertEventThrowableChainContains, assertHasEventThrowableChainContains, assertNoEventThrowableChainContains</li>
 *   <li><b>Edge Cases:</b> Null throwables, missing causes, empty suppressed arrays, circular references prevention</li>
 * </ul>
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
 */
@DisplayName("AssertLogger - Enhanced Throwable Assertions")
public class AssertLoggerThrowableEnhancedTest {

    @Nested
    @DisplayName("assertEventThrowableCauseIs")
    class AssertEventThrowableCauseIs {

        @Test
        @DisplayName("should pass when throwable has cause of expected type")
        void shouldPassWhenThrowableHasCauseOfExpectedType() {
            final Logger logger = new MockLogger("test");
            final IOException cause = new IOException("Connection error");
            final RuntimeException exception = new RuntimeException("Wrapper", cause);
            logger.error("Error occurred", exception);

            AssertLogger.assertEventThrowableCauseIs(logger, 0, IOException.class);
        }

        @Test
        @DisplayName("should pass when throwable has cause of subtype")
        void shouldPassWhenThrowableHasCauseOfSubtype() {
            final Logger logger = new MockLogger("test");
            final SQLException cause = new SQLException("DB error");
            final RuntimeException exception = new RuntimeException("Wrapper", cause);
            logger.error("Error occurred", exception);

            // SQLException extends Exception
            AssertLogger.assertEventThrowableCauseIs(logger, 0, SQLException.class);
            AssertLogger.assertEventThrowableCauseIs(logger, 0, Exception.class);
        }

        @Test
        @DisplayName("should throw when throwable has no cause")
        void shouldThrowWhenThrowableHasNoCause() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("No cause");
            logger.error("Error occurred", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableCauseIs(logger, 0, IOException.class));
            assertTrue(error.getMessage().contains("should have a cause"), "should indicate missing cause");
        }

        @Test
        @DisplayName("should throw when cause type does not match")
        void shouldThrowWhenCauseTypeDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            final IOException cause = new IOException("IO error");
            final RuntimeException exception = new RuntimeException("Wrapper", cause);
            logger.error("Error occurred", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableCauseIs(logger, 0, SQLException.class));
            assertTrue(error.getMessage().contains("should have expected cause type"), "should indicate type mismatch");
            assertTrue(error.getMessage().contains("IOException"), "should show actual type");
        }

        @Test
        @DisplayName("should throw when event has no throwable")
        void shouldThrowWhenEventHasNoThrowable() {
            final Logger logger = new MockLogger("test");
            logger.info("No exception");

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableCauseIs(logger, 0, IOException.class));
            assertTrue(error.getMessage().contains("should have a throwable"), "should indicate missing throwable");
        }

        @Test
        @DisplayName("should throw when event index is out of bounds")
        void shouldThrowWhenEventIndexIsOutOfBounds() {
            final Logger logger = new MockLogger("test");

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableCauseIs(logger, 0, IOException.class));
            assertTrue(error.getMessage().contains("should have enough logger events"), "should indicate missing event");
        }
    }

    @Nested
    @DisplayName("assertHasEventThrowableCauseIs")
    class AssertHasEventThrowableCauseIs {

        @Test
        @DisplayName("should pass when at least one event has throwable with expected cause type")
        void shouldPassWhenAtLeastOneEventHasThrowableWithExpectedCauseType() {
            final Logger logger = new MockLogger("test");
            logger.info("Normal log");
            final IOException cause = new IOException("Connection error");
            final RuntimeException exception = new RuntimeException("Wrapper", cause);
            logger.error("Error occurred", exception);
            logger.warn("Warning");

            AssertLogger.assertHasEventThrowableCauseIs(logger, IOException.class);
        }

        @Test
        @DisplayName("should pass when multiple events have throwable with expected cause type")
        void shouldPassWhenMultipleEventsHaveThrowableWithExpectedCauseType() {
            final Logger logger = new MockLogger("test");
            final IOException cause1 = new IOException("Error 1");
            logger.error("Error 1", new RuntimeException("Wrapper 1", cause1));
            final IOException cause2 = new IOException("Error 2");
            logger.error("Error 2", new RuntimeException("Wrapper 2", cause2));

            AssertLogger.assertHasEventThrowableCauseIs(logger, IOException.class);
        }

        @Test
        @DisplayName("should throw when no event has throwable with expected cause type")
        void shouldThrowWhenNoEventHasThrowableWithExpectedCauseType() {
            final Logger logger = new MockLogger("test");
            logger.info("Info message");
            final IOException cause = new IOException("IO error");
            logger.error("Error", new RuntimeException("Wrapper", cause));

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertHasEventThrowableCauseIs(logger, SQLException.class));
            assertTrue(error.getMessage().contains("should have at least one event"), "should indicate no matching event");
            assertTrue(error.getMessage().contains("SQLException"), "should show expected type");
        }

        @Test
        @DisplayName("should throw when all events have no throwable")
        void shouldThrowWhenAllEventsHaveNoThrowable() {
            final Logger logger = new MockLogger("test");
            logger.info("Message 1");
            logger.warn("Message 2");

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertHasEventThrowableCauseIs(logger, IOException.class));
            assertTrue(error.getMessage().contains("should have at least one event"), "should indicate no matching event");
        }
    }

    @Nested
    @DisplayName("assertNoEventThrowableCauseIs")
    class AssertNoEventThrowableCauseIs {

        @Test
        @DisplayName("should pass when no event has throwable with unexpected cause type")
        void shouldPassWhenNoEventHasThrowableWithUnexpectedCauseType() {
            final Logger logger = new MockLogger("test");
            logger.info("Info message");
            final IOException cause = new IOException("IO error");
            logger.error("Error", new RuntimeException("Wrapper", cause));

            AssertLogger.assertNoEventThrowableCauseIs(logger, SQLException.class);
        }

        @Test
        @DisplayName("should pass when all events have no throwable")
        void shouldPassWhenAllEventsHaveNoThrowable() {
            final Logger logger = new MockLogger("test");
            logger.info("Message 1");
            logger.warn("Message 2");

            AssertLogger.assertNoEventThrowableCauseIs(logger, IOException.class);
        }

        @Test
        @DisplayName("should throw when at least one event has throwable with unexpected cause type")
        void shouldThrowWhenAtLeastOneEventHasThrowableWithUnexpectedCauseType() {
            final Logger logger = new MockLogger("test");
            logger.info("Info message");
            final IOException cause = new IOException("IO error");
            logger.error("Error", new RuntimeException("Wrapper", cause));

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertNoEventThrowableCauseIs(logger, IOException.class));
            assertTrue(error.getMessage().contains("should have no events"), "should indicate unexpected event");
            assertTrue(error.getMessage().contains("IOException"), "should show unexpected type");
        }
    }

    @Nested
    @DisplayName("assertEventThrowableHasSuppressed")
    class AssertEventThrowableHasSuppressed {

        @Test
        @DisplayName("should pass when throwable has suppressed exception of expected type")
        void shouldPassWhenThrowableHasSuppressedExceptionOfExpectedType() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("Main error");
            final IOException suppressed = new IOException("Cleanup failed");
            exception.addSuppressed(suppressed);
            logger.error("Error occurred", exception);

            AssertLogger.assertEventThrowableHasSuppressed(logger, 0, IOException.class);
        }

        @Test
        @DisplayName("should pass when throwable has multiple suppressed exceptions with expected type")
        void shouldPassWhenThrowableHasMultipleSuppressedExceptionsWithExpectedType() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("Main error");
            exception.addSuppressed(new SQLException("DB cleanup failed"));
            exception.addSuppressed(new IOException("IO cleanup failed"));
            logger.error("Error occurred", exception);

            AssertLogger.assertEventThrowableHasSuppressed(logger, 0, IOException.class);
            AssertLogger.assertEventThrowableHasSuppressed(logger, 0, SQLException.class);
        }

        @Test
        @DisplayName("should pass when suppressed exception is subtype")
        void shouldPassWhenSuppressedExceptionIsSubtype() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("Main error");
            exception.addSuppressed(new SQLException("DB error"));
            logger.error("Error occurred", exception);

            // SQLException extends Exception
            AssertLogger.assertEventThrowableHasSuppressed(logger, 0, SQLException.class);
            AssertLogger.assertEventThrowableHasSuppressed(logger, 0, Exception.class);
        }

        @Test
        @DisplayName("should throw when throwable has no suppressed exceptions")
        void shouldThrowWhenThrowableHasNoSuppressedExceptions() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("No suppressed");
            logger.error("Error occurred", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableHasSuppressed(logger, 0, IOException.class));
            assertTrue(error.getMessage().contains("should have at least one suppressed exception"), "should indicate missing suppressed");
        }

        @Test
        @DisplayName("should throw when suppressed exception type does not match")
        void shouldThrowWhenSuppressedExceptionTypeDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("Main error");
            exception.addSuppressed(new IOException("IO error"));
            logger.error("Error occurred", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableHasSuppressed(logger, 0, SQLException.class));
            assertTrue(error.getMessage().contains("should have suppressed exception of expected type"), "should indicate type mismatch");
            assertTrue(error.getMessage().contains("SQLException"), "should show expected type");
        }

        @Test
        @DisplayName("should throw when event has no throwable")
        void shouldThrowWhenEventHasNoThrowable() {
            final Logger logger = new MockLogger("test");
            logger.info("No exception");

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableHasSuppressed(logger, 0, IOException.class));
            assertTrue(error.getMessage().contains("should have a throwable"), "should indicate missing throwable");
        }
    }

    @Nested
    @DisplayName("assertHasEventThrowableHasSuppressed")
    class AssertHasEventThrowableHasSuppressed {

        @Test
        @DisplayName("should pass when at least one event has throwable with expected suppressed type")
        void shouldPassWhenAtLeastOneEventHasThrowableWithExpectedSuppressedType() {
            final Logger logger = new MockLogger("test");
            logger.info("Normal log");
            final RuntimeException exception = new RuntimeException("Error");
            exception.addSuppressed(new IOException("Cleanup failed"));
            logger.error("Error occurred", exception);

            AssertLogger.assertHasEventThrowableHasSuppressed(logger, IOException.class);
        }

        @Test
        @DisplayName("should throw when no event has throwable with expected suppressed type")
        void shouldThrowWhenNoEventHasThrowableWithExpectedSuppressedType() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("Error");
            exception.addSuppressed(new IOException("IO error"));
            logger.error("Error", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertHasEventThrowableHasSuppressed(logger, SQLException.class));
            assertTrue(error.getMessage().contains("should have at least one event"), "should indicate no matching event");
        }
    }

    @Nested
    @DisplayName("assertNoEventThrowableHasSuppressed")
    class AssertNoEventThrowableHasSuppressed {

        @Test
        @DisplayName("should pass when no event has throwable with unexpected suppressed type")
        void shouldPassWhenNoEventHasThrowableWithUnexpectedSuppressedType() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("Error");
            exception.addSuppressed(new IOException("IO error"));
            logger.error("Error", exception);

            AssertLogger.assertNoEventThrowableHasSuppressed(logger, SQLException.class);
        }

        @Test
        @DisplayName("should throw when at least one event has throwable with unexpected suppressed type")
        void shouldThrowWhenAtLeastOneEventHasThrowableWithUnexpectedSuppressedType() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("Error");
            exception.addSuppressed(new IOException("IO error"));
            logger.error("Error", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertNoEventThrowableHasSuppressed(logger, IOException.class));
            assertTrue(error.getMessage().contains("should have no events"), "should indicate unexpected event");
        }
    }

    @Nested
    @DisplayName("assertEventThrowableChainContains")
    class AssertEventThrowableChainContains {

        @Test
        @DisplayName("should pass when throwable itself is of expected type")
        void shouldPassWhenThrowableItselfIsOfExpectedType() {
            final Logger logger = new MockLogger("test");
            final IOException exception = new IOException("IO error");
            logger.error("Error occurred", exception);

            AssertLogger.assertEventThrowableChainContains(logger, 0, IOException.class);
        }

        @Test
        @DisplayName("should pass when direct cause is of expected type")
        void shouldPassWhenDirectCauseIsOfExpectedType() {
            final Logger logger = new MockLogger("test");
            final IOException cause = new IOException("Root cause");
            final RuntimeException exception = new RuntimeException("Wrapper", cause);
            logger.error("Error occurred", exception);

            AssertLogger.assertEventThrowableChainContains(logger, 0, IOException.class);
        }

        @Test
        @DisplayName("should pass when deep in chain is of expected type")
        void shouldPassWhenDeepInChainIsOfExpectedType() {
            final Logger logger = new MockLogger("test");
            final SQLException rootCause = new SQLException("DB error");
            final IOException middleCause = new IOException("IO error", rootCause);
            final RuntimeException topException = new RuntimeException("Wrapper", middleCause);
            logger.error("Error occurred", topException);

            AssertLogger.assertEventThrowableChainContains(logger, 0, SQLException.class);
            AssertLogger.assertEventThrowableChainContains(logger, 0, IOException.class);
            AssertLogger.assertEventThrowableChainContains(logger, 0, RuntimeException.class);
        }

        @Test
        @DisplayName("should pass when chain contains subtype")
        void shouldPassWhenChainContainsSubtype() {
            final Logger logger = new MockLogger("test");
            final SQLException cause = new SQLException("DB error");
            final RuntimeException exception = new RuntimeException("Wrapper", cause);
            logger.error("Error occurred", exception);

            // SQLException extends Exception
            AssertLogger.assertEventThrowableChainContains(logger, 0, SQLException.class);
            AssertLogger.assertEventThrowableChainContains(logger, 0, Exception.class);
        }

        @Test
        @DisplayName("should throw when chain does not contain expected type")
        void shouldThrowWhenChainDoesNotContainExpectedType() {
            final Logger logger = new MockLogger("test");
            final IOException cause = new IOException("IO error");
            final RuntimeException exception = new RuntimeException("Wrapper", cause);
            logger.error("Error occurred", exception);

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableChainContains(logger, 0, SQLException.class));
            assertTrue(error.getMessage().contains("should have exception of expected type in cause chain"), "should indicate chain mismatch");
            assertTrue(error.getMessage().contains("SQLException"), "should show expected type");
            assertTrue(error.getMessage().contains("RuntimeException"), "should show actual chain");
        }

        @Test
        @DisplayName("should throw when event has no throwable")
        void shouldThrowWhenEventHasNoThrowable() {
            final Logger logger = new MockLogger("test");
            logger.info("No exception");

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertEventThrowableChainContains(logger, 0, IOException.class));
            assertTrue(error.getMessage().contains("should have a throwable"), "should indicate missing throwable");
        }

        @Test
        @DisplayName("should handle long chains without stack overflow")
        void shouldHandleLongChainsWithoutStackOverflow() {
            final Logger logger = new MockLogger("test");
            Throwable current = new IOException("Root");
            for (int i = 0; i < 20; i++) {
                current = new RuntimeException("Level " + i, current);
            }
            logger.error("Deep chain", current);

            AssertLogger.assertEventThrowableChainContains(logger, 0, IOException.class);
        }
    }

    @Nested
    @DisplayName("assertHasEventThrowableChainContains")
    class AssertHasEventThrowableChainContains {

        @Test
        @DisplayName("should pass when at least one event has throwable chain containing expected type")
        void shouldPassWhenAtLeastOneEventHasThrowableChainContainingExpectedType() {
            final Logger logger = new MockLogger("test");
            logger.info("Normal log");
            final SQLException cause = new SQLException("DB error");
            final RuntimeException exception = new RuntimeException("Wrapper", cause);
            logger.error("Error occurred", exception);

            AssertLogger.assertHasEventThrowableChainContains(logger, SQLException.class);
        }

        @Test
        @DisplayName("should throw when no event has throwable chain containing expected type")
        void shouldThrowWhenNoEventHasThrowableChainContainingExpectedType() {
            final Logger logger = new MockLogger("test");
            final IOException cause = new IOException("IO error");
            logger.error("Error", new RuntimeException("Wrapper", cause));

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertHasEventThrowableChainContains(logger, SQLException.class));
            assertTrue(error.getMessage().contains("should have at least one event"), "should indicate no matching event");
        }
    }

    @Nested
    @DisplayName("assertNoEventThrowableChainContains")
    class AssertNoEventThrowableChainContains {

        @Test
        @DisplayName("should pass when no event has throwable chain containing unexpected type")
        void shouldPassWhenNoEventHasThrowableChainContainingUnexpectedType() {
            final Logger logger = new MockLogger("test");
            final IOException cause = new IOException("IO error");
            logger.error("Error", new RuntimeException("Wrapper", cause));

            AssertLogger.assertNoEventThrowableChainContains(logger, SQLException.class);
        }

        @Test
        @DisplayName("should throw when at least one event has throwable chain containing unexpected type")
        void shouldThrowWhenAtLeastOneEventHasThrowableChainContainingUnexpectedType() {
            final Logger logger = new MockLogger("test");
            final SQLException cause = new SQLException("DB error");
            logger.error("Error", new RuntimeException("Wrapper", cause));

            final AssertionError error = assertThrows(AssertionError.class,
                    () -> AssertLogger.assertNoEventThrowableChainContains(logger, SQLException.class));
            assertTrue(error.getMessage().contains("should have no events"), "should indicate unexpected event");
        }
    }
}
