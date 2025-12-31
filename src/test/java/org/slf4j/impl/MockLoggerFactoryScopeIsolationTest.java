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
package org.slf4j.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.usefultoys.slf4jtestmock.AIGenerated;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for verifying that {@link MockLoggerFactory} can isolate logger instances across scopes.
 */
@DisplayName("MockLoggerFactory scope isolation")
@AIGenerated("copilot")
class MockLoggerFactoryScopeIsolationTest {

    @AfterEach
    void cleanupScope() {
        MockLoggerFactory.clearCurrentScopeId();
    }

    @Nested
    @DisplayName("Scope Isolation")
    class ScopeIsolation {

        @Test
        @DisplayName("should return different logger instances for same name across different scopes")
        void shouldReturnDifferentLoggerInstancesForSameNameAcrossDifferentScopes() throws Exception {
        final ILoggerFactory factory = MockLoggerFactory.getInstance();
        assertInstanceOf(MockLoggerFactory.class, factory, "should return MockLoggerFactory singleton");

        final String loggerName = "shared.logger";

        final AtomicReference<MockLogger> logger1 = new AtomicReference<>();
        final AtomicReference<MockLogger> logger2 = new AtomicReference<>();

        final Thread t1 = new Thread(() -> {
            MockLoggerFactory.setCurrentScopeId("scope-1");
            final Logger logger = factory.getLogger(loggerName);
            logger1.set((MockLogger) logger);
            logger.info("from scope 1");
        });

        final Thread t2 = new Thread(() -> {
            MockLoggerFactory.setCurrentScopeId("scope-2");
            final Logger logger = factory.getLogger(loggerName);
            logger2.set((MockLogger) logger);
            logger.info("from scope 2");
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertNotNull(logger1.get(), "should have logger in scope-1");
        assertNotNull(logger2.get(), "should have logger in scope-2");
        assertNotSame(logger1.get(), logger2.get(), "should isolate loggers by scope id");

        MockLoggerFactory.setCurrentScopeId("scope-1");
        assertEquals(1, logger1.get().getEventCount(), "should have exactly one event in scope-1 logger");
        assertEquals(1, logger2.get().getEventCount(), "should have exactly one event in scope-2 logger instance");
        final Map<String, Logger> scope1Loggers = MockLoggerFactory.getLoggers();
        assertTrue(scope1Loggers.containsKey(loggerName), "should expose logger in current scope via getLoggers()");
        assertSame(logger1.get(), scope1Loggers.get(loggerName), "should return scope-1 logger from getLoggers()");

        MockLoggerFactory.setCurrentScopeId("scope-2");
        assertEquals(1, logger2.get().getEventCount(), "should have exactly one event in scope-2 logger");
        final Map<String, Logger> scope2Loggers = MockLoggerFactory.getLoggers();
        assertTrue(scope2Loggers.containsKey(loggerName), "should expose logger in current scope via getLoggers()");
        assertSame(logger2.get(), scope2Loggers.get(loggerName), "should return scope-2 logger from getLoggers()");

        MockLoggerFactory.clearCurrentScopeId();
    }

        @Test
        @DisplayName("should return same instance within same scope")
        void shouldReturnSameInstanceWithinSameScope() {
            final ILoggerFactory factory = MockLoggerFactory.getInstance();
            final String loggerName = "test.logger";

            MockLoggerFactory.setCurrentScopeId("scope-A");
            final Logger logger1 = factory.getLogger(loggerName);
            final Logger logger2 = factory.getLogger(loggerName);

            assertSame(logger1, logger2, "should return same instance for same name in same scope");

            logger1.info("Message 1");
            logger2.info("Message 2");

            final MockLogger mockLogger = (MockLogger) logger1;
            assertEquals(2, mockLogger.getEventCount(), "should accumulate events in same logger instance");
        }

        @Test
        @DisplayName("should maintain independent event lists per scope")
        void shouldMaintainIndependentEventListsPerScope() {
            final ILoggerFactory factory = MockLoggerFactory.getInstance();
            final String loggerName = "shared.logger";

            MockLoggerFactory.setCurrentScopeId("scope-X");
            final Logger loggerX = factory.getLogger(loggerName);
            loggerX.info("X1");
            loggerX.warn("X2");

            MockLoggerFactory.setCurrentScopeId("scope-Y");
            final Logger loggerY = factory.getLogger(loggerName);
            loggerY.error("Y1");

            final MockLogger mockX = (MockLogger) loggerX;
            final MockLogger mockY = (MockLogger) loggerY;

            assertEquals(2, mockX.getEventCount(), "scope-X logger should have 2 events");
            assertEquals(1, mockY.getEventCount(), "scope-Y logger should have 1 event");

            assertEquals("X1", mockX.getEvent(0).getMessage(), "scope-X first event should be X1");
            assertEquals("Y1", mockY.getEvent(0).getMessage(), "scope-Y first event should be Y1");
        }
    }

    @Nested
    @DisplayName("Scope-less Behavior (Backward Compatibility)")
    class ScopelessBehavior {

        @Test
        @DisplayName("should use global cache when no scope is set")
        void shouldUseGlobalCacheWhenNoScopeIsSet() {
            final ILoggerFactory factory = MockLoggerFactory.getInstance();
            final String loggerName = "global.logger";

            assertNull(MockLoggerFactory.getCurrentScopeId(), "scope should be null initially");

            final Logger logger1 = factory.getLogger(loggerName);
            final Logger logger2 = factory.getLogger(loggerName);

            assertSame(logger1, logger2, "should return same instance when no scope is set");
        }

        @Test
        @DisplayName("should isolate null-scoped loggers from scoped loggers")
        void shouldIsolateNullScopedLoggersFromScopedLoggers() {
            final ILoggerFactory factory = MockLoggerFactory.getInstance();
            final String loggerName = "mixed.logger";

            // Get logger without scope
            final Logger globalLogger = factory.getLogger(loggerName);
            globalLogger.info("Global message");

            // Get logger with scope
            MockLoggerFactory.setCurrentScopeId("scope-Z");
            final Logger scopedLogger = factory.getLogger(loggerName);
            scopedLogger.warn("Scoped message");

            assertNotSame(globalLogger, scopedLogger, "should return different instances for null vs non-null scope");

            final MockLogger mockGlobal = (MockLogger) globalLogger;
            final MockLogger mockScoped = (MockLogger) scopedLogger;

            assertEquals(1, mockGlobal.getEventCount(), "global logger should have 1 event");
            assertEquals(1, mockScoped.getEventCount(), "scoped logger should have 1 event");

            assertEquals("Global message", mockGlobal.getEvent(0).getMessage());
            assertEquals("Scoped message", mockScoped.getEvent(0).getMessage());
        }
    }

    @Nested
    @DisplayName("getLoggers() Scope Filtering")
    class GetLoggersFiltering {

        @Test
        @DisplayName("should return only loggers from current scope")
        void shouldReturnOnlyLoggersFromCurrentScope() {
            final ILoggerFactory factory = MockLoggerFactory.getInstance();

            MockLoggerFactory.setCurrentScopeId("scope-1");
            factory.getLogger("logger.A");
            factory.getLogger("logger.B");

            MockLoggerFactory.setCurrentScopeId("scope-2");
            factory.getLogger("logger.C");

            MockLoggerFactory.setCurrentScopeId("scope-1");
            final Map<String, Logger> scope1Loggers = MockLoggerFactory.getLoggers();

            assertEquals(2, scope1Loggers.size(), "should return only 2 loggers for scope-1");
            assertTrue(scope1Loggers.containsKey("logger.A"), "should contain logger.A");
            assertTrue(scope1Loggers.containsKey("logger.B"), "should contain logger.B");
            assertFalse(scope1Loggers.containsKey("logger.C"), "should not contain logger.C from scope-2");

            MockLoggerFactory.setCurrentScopeId("scope-2");
            final Map<String, Logger> scope2Loggers = MockLoggerFactory.getLoggers();

            assertEquals(1, scope2Loggers.size(), "should return only 1 logger for scope-2");
            assertTrue(scope2Loggers.containsKey("logger.C"), "should contain logger.C");
        }

        @Test
        @DisplayName("should return empty map when no loggers exist in current scope")
        void shouldReturnEmptyMapWhenNoLoggersExistInCurrentScope() {
            MockLoggerFactory.setCurrentScopeId("nonexistent-scope");
            final Map<String, Logger> loggers = MockLoggerFactory.getLoggers();

            assertNotNull(loggers, "should return non-null map");
            assertTrue(loggers.isEmpty(), "should return empty map for scope with no loggers");
        }

        @Test
        @DisplayName("should return unmodifiable map")
        void shouldReturnUnmodifiableMap() {
            final ILoggerFactory factory = MockLoggerFactory.getInstance();

            MockLoggerFactory.setCurrentScopeId("test-scope");
            factory.getLogger("test.logger");

            final Map<String, Logger> loggers = MockLoggerFactory.getLoggers();

            assertThrows(UnsupportedOperationException.class, () -> {
                loggers.put("new.logger", factory.getLogger("new.logger"));
            }, "should throw UnsupportedOperationException when trying to modify returned map");
        }
    }

    @Nested
    @DisplayName("Scope Management API")
    class ScopeManagementApi {

        @Test
        @DisplayName("should set and get current scope ID")
        void shouldSetAndGetCurrentScopeId() {
            final String scopeId = "test-scope-123";

            MockLoggerFactory.setCurrentScopeId(scopeId);
            assertEquals(scopeId, MockLoggerFactory.getCurrentScopeId(), "should return the set scope ID");
        }

        @Test
        @DisplayName("should clear scope ID with clearCurrentScopeId()")
        void shouldClearScopeIdWithClearMethod() {
            MockLoggerFactory.setCurrentScopeId("some-scope");
            assertNotNull(MockLoggerFactory.getCurrentScopeId(), "scope should be set");

            MockLoggerFactory.clearCurrentScopeId();
            assertNull(MockLoggerFactory.getCurrentScopeId(), "scope should be null after clear");
        }

        @Test
        @DisplayName("should clear scope ID with setCurrentScopeId(null)")
        void shouldClearScopeIdWithNullSet() {
            MockLoggerFactory.setCurrentScopeId("some-scope");
            assertNotNull(MockLoggerFactory.getCurrentScopeId(), "scope should be set");

            MockLoggerFactory.setCurrentScopeId(null);
            assertNull(MockLoggerFactory.getCurrentScopeId(), "scope should be null after set(null)");
        }

        @Test
        @DisplayName("should return null when no scope is set")
        void shouldReturnNullWhenNoScopeIsSet() {
            MockLoggerFactory.clearCurrentScopeId();
            assertNull(MockLoggerFactory.getCurrentScopeId(), "should return null when no scope is active");
        }
    }

    @Nested
    @DisplayName("InheritableThreadLocal Behavior")
    class InheritableThreadLocalBehavior {

        @Test
        @DisplayName("should inherit scope in child thread")
        void shouldInheritScopeInChildThread() throws Exception {
            final String parentScope = "parent-scope";
            final AtomicReference<String> childScope = new AtomicReference<>();
            final CountDownLatch latch = new CountDownLatch(1);

            MockLoggerFactory.setCurrentScopeId(parentScope);

            final Thread childThread = new Thread(() -> {
                childScope.set(MockLoggerFactory.getCurrentScopeId());
                latch.countDown();
            });

            childThread.start();
            latch.await();
            childThread.join();

            assertEquals(parentScope, childScope.get(), "child thread should inherit parent's scope ID");
        }

        @Test
        @DisplayName("should get same logger instance in parent and child thread with inherited scope")
        void shouldGetSameLoggerInstanceInParentAndChildThreadWithInheritedScope() throws Exception {
            final ILoggerFactory factory = MockLoggerFactory.getInstance();
            final String loggerName = "inherited.logger";
            final AtomicReference<Logger> childLogger = new AtomicReference<>();
            final CountDownLatch latch = new CountDownLatch(1);

            MockLoggerFactory.setCurrentScopeId("shared-scope");
            final Logger parentLogger = factory.getLogger(loggerName);
            parentLogger.info("Parent message");

            final Thread childThread = new Thread(() -> {
                final Logger logger = factory.getLogger(loggerName);
                logger.info("Child message");
                childLogger.set(logger);
                latch.countDown();
            });

            childThread.start();
            latch.await();
            childThread.join();

            assertSame(parentLogger, childLogger.get(), "parent and child should get same logger instance");

            final MockLogger mockLogger = (MockLogger) parentLogger;
            assertEquals(2, mockLogger.getEventCount(), "should have 2 events from both parent and child");
        }

        @Test
        @DisplayName("should not affect parent scope when child changes scope")
        void shouldNotAffectParentScopeWhenChildChangesScope() throws Exception {
            final String parentScope = "parent-scope";
            final String childScope = "child-scope";
            final AtomicReference<String> parentScopeAfter = new AtomicReference<>();
            final CountDownLatch latch = new CountDownLatch(1);

            MockLoggerFactory.setCurrentScopeId(parentScope);

            final Thread childThread = new Thread(() -> {
                // Child changes its scope
                MockLoggerFactory.setCurrentScopeId(childScope);
                latch.countDown();
            });

            childThread.start();
            latch.await();
            childThread.join();

            parentScopeAfter.set(MockLoggerFactory.getCurrentScopeId());

            assertEquals(parentScope, parentScopeAfter.get(), "parent scope should remain unchanged after child modifies its scope");
        }
    }
}
