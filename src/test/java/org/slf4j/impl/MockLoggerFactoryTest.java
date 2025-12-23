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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link MockLoggerFactory}.
 *
 * @author Daniel Felix Ferber
 */
@DisplayName("MockLoggerFactory Tests")
class MockLoggerFactoryTest {

    @Test
    @DisplayName("Should return singleton instance")
    void shouldReturnSingletonInstance() {
        // When
        final ILoggerFactory instance1 = MockLoggerFactory.getInstance();
        final ILoggerFactory instance2 = MockLoggerFactory.getInstance();
        
        // Then
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
        assertInstanceOf(MockLoggerFactory.class, instance1);
    }

    @Test
    @DisplayName("Should create logger with correct name")
    void shouldCreateLoggerWithCorrectName() {
        // Given
        final MockLoggerFactory factory = new MockLoggerFactory();
        final String loggerName = "test.logger.name";
        
        // When
        final Logger logger = factory.getLogger(loggerName);
        
        // Then
        assertNotNull(logger);
        assertInstanceOf(MockLogger.class, logger);
        assertEquals(loggerName, logger.getName());
    }

    @Test
    @DisplayName("Should return same logger instance for same name")
    void shouldReturnSameLoggerInstanceForSameName() {
        // Given
        final MockLoggerFactory factory = new MockLoggerFactory();
        final String loggerName = "same.logger.name";
        
        // When
        final Logger logger1 = factory.getLogger(loggerName);
        final Logger logger2 = factory.getLogger(loggerName);
        
        // Then
        assertNotNull(logger1);
        assertNotNull(logger2);
        assertSame(logger1, logger2);
    }

    @Test
    @DisplayName("Should create different logger instances for different names")
    void shouldCreateDifferentLoggerInstancesForDifferentNames() {
        // Given
        final MockLoggerFactory factory = new MockLoggerFactory();
        final String loggerName1 = "logger.one";
        final String loggerName2 = "logger.two";
        
        // When
        final Logger logger1 = factory.getLogger(loggerName1);
        final Logger logger2 = factory.getLogger(loggerName2);
        
        // Then
        assertNotNull(logger1);
        assertNotNull(logger2);
        assertNotSame(logger1, logger2);
        assertEquals(loggerName1, logger1.getName());
        assertEquals(loggerName2, logger2.getName());
    }

    @Test
    @DisplayName("Should handle null logger name gracefully")
    void shouldHandleNullLoggerNameGracefully() {
        // Given
        final MockLoggerFactory factory = new MockLoggerFactory();
        
        // When
        final Logger logger = factory.getLogger(null);
        
        // Then
        assertNotNull(logger);
        assertInstanceOf(MockLogger.class, logger);
        assertNull(logger.getName());
    }

    @Test
    @DisplayName("Should handle empty logger name")
    void shouldHandleEmptyLoggerName() {
        // Given
        final MockLoggerFactory factory = new MockLoggerFactory();
        final String emptyName = "";
        
        // When
        final Logger logger = factory.getLogger(emptyName);
        
        // Then
        assertNotNull(logger);
        assertInstanceOf(MockLogger.class, logger);
        assertEquals(emptyName, logger.getName());
    }

    @Test
    @DisplayName("Should handle very long logger name")
    void shouldHandleVeryLongLoggerName() {
        // Given
        final MockLoggerFactory factory = new MockLoggerFactory();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        final String longName = sb.toString();
        
        // When
        final Logger logger = factory.getLogger(longName);
        
        // Then
        assertNotNull(logger);
        assertInstanceOf(MockLogger.class, logger);
        assertEquals(longName, logger.getName());
    }

    @Test
    @DisplayName("Should handle logger names with special characters")
    void shouldHandleLoggerNamesWithSpecialCharacters() {
        // Given
        final MockLoggerFactory factory = new MockLoggerFactory();
        final String specialName = "logger.with-special_chars.123!@#$%";
        
        // When
        final Logger logger = factory.getLogger(specialName);
        
        // Then
        assertNotNull(logger);
        assertInstanceOf(MockLogger.class, logger);
        assertEquals(specialName, logger.getName());
    }

    @Test
    @DisplayName("Should maintain logger cache across multiple calls")
    void shouldMaintainLoggerCacheAcrossMultipleCalls() {
        // Given
        final MockLoggerFactory factory = new MockLoggerFactory();
        final String[] loggerNames = {
            "logger.a", "logger.b", "logger.c", "logger.a", "logger.b"
        };
        
        // When
        final Logger[] loggers = new Logger[loggerNames.length];
        for (int i = 0; i < loggerNames.length; i++) {
            loggers[i] = factory.getLogger(loggerNames[i]);
        }
        
        // Then
        assertSame(loggers[0], loggers[3]); // Same instance for "logger.a"
        assertSame(loggers[1], loggers[4]); // Same instance for "logger.b"
        assertNotSame(loggers[0], loggers[1]); // Different instances for different names
        assertNotSame(loggers[1], loggers[2]); // Different instances for different names
    }

    @Test
    @DisplayName("Should work with hierarchical logger names")
    void shouldWorkWithHierarchicalLoggerNames() {
        // Given
        final MockLoggerFactory factory = new MockLoggerFactory();
        final String parentLogger = "com.example";
        final String childLogger = "com.example.service";
        final String grandchildLogger = "com.example.service.impl";
        
        // When
        final Logger parent = factory.getLogger(parentLogger);
        final Logger child = factory.getLogger(childLogger);
        final Logger grandchild = factory.getLogger(grandchildLogger);
        
        // Then
        assertNotNull(parent);
        assertNotNull(child);
        assertNotNull(grandchild);
        assertNotSame(parent, child);
        assertNotSame(child, grandchild);
        assertNotSame(parent, grandchild);
        
        assertEquals(parentLogger, parent.getName());
        assertEquals(childLogger, child.getName());
        assertEquals(grandchildLogger, grandchild.getName());
    }
}