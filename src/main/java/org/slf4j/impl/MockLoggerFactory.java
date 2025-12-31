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

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.usefultoys.slf4jtestmock.AIGenerated;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A mock implementation of {@link ILoggerFactory} intended for use in unit tests.
 *
 * <p>
 * Provides {@link MockLogger} instances that capture log output in-memory, allowing assertions on logged content
 * without requiring access to external files or consoles.
 * <p>
 * This factory is discovered automatically by SLF4J when present on the test classpath, and should not be referenced or
 * instantiated directly in test code.
 * <p>
 * To use this in tests, ensure the service provider configuration is in place:
 * {@code META-INF/services/org.slf4j.ILoggerFactory} should contain:
 * <pre>
 * org.usefultoys.slf4j.report.MockLoggerFactory
 * </pre>
 * <p>
 * No other SLF4J implementation should be present on the classepath.
 * <p>
 * When configured, all SLF4J logger requests in test code will return {@link MockLogger} instances.
 * 
 *
 * @author Daniel Felix Ferber
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MockLoggerFactory implements ILoggerFactory {

    /**
     * Thread-local scope id used to isolate logger instances between parallel test executions.
     * <p>
     * When set, {@link #getLogger(String)} will return distinct {@link MockLogger} instances per
     * {@code (scopeId, loggerName)} pair. When not set, behavior falls back to the historical
     * global cache keyed only by logger name.
     */
    private static final InheritableThreadLocal<String> CURRENT_SCOPE_ID = new InheritableThreadLocal<>();

    /**
     * Default constructor for SLF4J factory instantiation.
     */
    public MockLoggerFactory() {
        // Default constructor
    }

    Map<LoggerKey, Logger> scopedNameToLogger = new ConcurrentHashMap<>();
    static final MockLoggerFactory instance = new MockLoggerFactory();

    /**
     * Sets the current scope id for the calling thread.
     * <p>
     * Intended to be used by the JUnit extension to isolate parallel tests.
     *
     * @param scopeId the scope id to set (null clears scope)
     */
    @AIGenerated("copilot")
    public static void setCurrentScopeId(final String scopeId) {
        if (scopeId == null) {
            CURRENT_SCOPE_ID.remove();
        } else {
            CURRENT_SCOPE_ID.set(scopeId);
        }
    }

    /**
     * Returns the current scope id for the calling thread, or null if none is set.
     *
     * @return the current scope id, or null
     */
    @AIGenerated("copilot")
    public static String getCurrentScopeId() {
        return CURRENT_SCOPE_ID.get();
    }

    /**
     * Clears the current scope id for the calling thread.
     */
    @AIGenerated("copilot")
    public static void clearCurrentScopeId() {
        CURRENT_SCOPE_ID.remove();
    }

    /**
     * Returns the singleton instance of this factory.
     * <p>
     * This method is used by SLF4J's internal binding mechanism and should not be called directly
     * by application code.
     *
     * @return the singleton MockLoggerFactory instance
     */
    public static ILoggerFactory getInstance() {
        return instance;
    }

    @Override
    public Logger getLogger(final String name) {
        final String scopeId = CURRENT_SCOPE_ID.get();
        final LoggerKey key = new LoggerKey(scopeId, name);
        return scopedNameToLogger.computeIfAbsent(key, k -> new MockLogger(name));
    }

    /**
     * Returns an unmodifiable view of the currently created loggers keyed by name.
     * <p>
     * This is a test helper which allows other classes (for example JUnit extensions)
     * to inspect all MockLogger instances created by this factory.
     *
     * @return unmodifiable map of logger name to Logger instance
     */
    public static Map<String, Logger> getLoggers() {
        final String scopeId = CURRENT_SCOPE_ID.get();
        final Map<String, Logger> result = new HashMap<>();
        for (final Map.Entry<LoggerKey, Logger> entry : instance.scopedNameToLogger.entrySet()) {
            if (Objects.equals(scopeId, entry.getKey().scopeId)) {
                result.put(entry.getKey().loggerName, entry.getValue());
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Key for caching loggers.
     */
    @AIGenerated("copilot")
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    static final class LoggerKey {
        String scopeId;
        String loggerName;

        LoggerKey(final String scopeId, final String loggerName) {
            this.scopeId = scopeId;
            this.loggerName = loggerName;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final LoggerKey loggerKey = (LoggerKey) o;
            return Objects.equals(scopeId, loggerKey.scopeId) && Objects.equals(loggerName, loggerKey.loggerName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(scopeId, loggerName);
        }
    }
}
