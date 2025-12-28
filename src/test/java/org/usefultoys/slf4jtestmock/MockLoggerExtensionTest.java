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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MockLoggerExtension}.
 *
 * @author Daniel Felix Ferber
 */
@DisplayName("MockLoggerExtension Tests")
class MockLoggerExtensionTest {

    /**
     * Tests for field injection with default configuration.
     */
    @Nested
    @DisplayName("Field Injection with Default Configuration")
    @ExtendWith(MockLoggerExtension.class)
    class FieldInjectionDefaultTest {

        @Slf4jMock
        private Logger logger;

        @Test
        @DisplayName("Should inject logger into field")
        void shouldInjectLoggerIntoField() {
            assertNotNull(logger);
            assertInstanceOf(MockLogger.class, logger);
        }

        @Test
        @DisplayName("Should have all log levels enabled by default")
        void shouldHaveAllLogLevelsEnabledByDefault() {
            assertTrue(logger.isTraceEnabled());
            assertTrue(logger.isDebugEnabled());
            assertTrue(logger.isInfoEnabled());
            assertTrue(logger.isWarnEnabled());
            assertTrue(logger.isErrorEnabled());
        }

        @Test
        @DisplayName("Should record log events")
        void shouldRecordLogEvents() {
            logger.info("Test message");

            MockLogger mockLogger = (MockLogger) logger;
            assertEquals(1, mockLogger.getEventCount());
            assertEquals("Test message", mockLogger.getLoggerEvents().get(0).getMessage());
        }

        @Test
        @DisplayName("Should use test class name as default logger name")
        void shouldUseTestClassNameAsDefaultLoggerName() {
            MockLogger mockLogger = (MockLogger) logger;
            assertEquals(FieldInjectionDefaultTest.class.getName(), mockLogger.getName());
        }

        @Test
        @DisplayName("Should clear events before each test")
        void shouldClearEventsBeforeEachTest() {
            MockLogger mockLogger = (MockLogger) logger;
            assertEquals(0, mockLogger.getEventCount());

            logger.info("Test");
            assertEquals(1, mockLogger.getEventCount());
        }
    }

    /**
     * Tests for field injection with custom logger name.
     */
    @Nested
    @DisplayName("Field Injection with Custom Logger Name")
    @ExtendWith(MockLoggerExtension.class)
    class FieldInjectionCustomNameTest {

        @Slf4jMock(value = "custom.logger.name")
        private Logger customLogger;

        @Test
        @DisplayName("Should use custom logger name from value attribute")
        void shouldUseCustomLoggerNameFromValue() {
            MockLogger mockLogger = (MockLogger) customLogger;
            assertEquals("custom.logger.name", mockLogger.getName());
        }

        @Test
        @DisplayName("Should inject and configure logger with custom name")
        void shouldInjectAndConfigureLoggerWithCustomName() {
            assertNotNull(customLogger);
            assertInstanceOf(MockLogger.class, customLogger);

            customLogger.info("Test message");
            MockLogger mockLogger = (MockLogger) customLogger;
            assertEquals(1, mockLogger.getEventCount());
        }
    }

    /**
     * Tests for field injection with type-based logger name.
     */
    @Nested
    @DisplayName("Field Injection with Type-Based Logger Name")
    @ExtendWith(MockLoggerExtension.class)
    class FieldInjectionTypeBasedTest {

        @Slf4jMock(type = String.class)
        private Logger typeBasedLogger;

        @Test
        @DisplayName("Should use type name as logger name")
        void shouldUseTypeNameAsLoggerName() {
            MockLogger mockLogger = (MockLogger) typeBasedLogger;
            assertEquals(String.class.getName(), mockLogger.getName());
        }
    }

    /**
     * Tests for field injection with custom logger name taking priority over type.
     */
    @Nested
    @DisplayName("Field Injection with Value Priority Over Type")
    @ExtendWith(MockLoggerExtension.class)
    class FieldInjectionValuePriorityTest {

        @Slf4jMock(value = "explicit.name", type = Integer.class)
        private Logger priorityLogger;

        @Test
        @DisplayName("Should use value attribute over type attribute")
        void shouldUseValueAttributeOverTypeAttribute() {
            MockLogger mockLogger = (MockLogger) priorityLogger;
            assertEquals("explicit.name", mockLogger.getName());
            assertNotEquals(Integer.class.getName(), mockLogger.getName());
        }
    }

    /**
     * Tests for field injection with disabled logger.
     */
    @Nested
    @DisplayName("Field Injection with Disabled Logger")
    @ExtendWith(MockLoggerExtension.class)
    class FieldInjectionDisabledTest {

        @Slf4jMock(enabled = false)
        private Logger disabledLogger;

        @Test
        @DisplayName("Should inject disabled logger")
        void shouldInjectDisabledLogger() {
            assertNotNull(disabledLogger);
            assertInstanceOf(MockLogger.class, disabledLogger);
        }

        @Test
        @DisplayName("Should not record events when logger is disabled")
        void shouldNotRecordEventsWhenLoggerIsDisabled() {
            disabledLogger.info("This should not be recorded");
            disabledLogger.error("This should also not be recorded");

            MockLogger mockLogger = (MockLogger) disabledLogger;
            assertEquals(0, mockLogger.getEventCount());
        }
    }

    /**
     * Tests for field injection with selective log level configuration.
     */
    @Nested
    @DisplayName("Field Injection with Selective Log Levels")
    @ExtendWith(MockLoggerExtension.class)
    class FieldInjectionSelectiveLevelsTest {

        @Slf4jMock(traceEnabled = false, debugEnabled = false, infoEnabled = true, warnEnabled = true, errorEnabled = true)
        private Logger selectiveLogger;

        @Test
        @DisplayName("Should disable trace level")
        void shouldDisableTraceLevel() {
            assertFalse(selectiveLogger.isTraceEnabled());
        }

        @Test
        @DisplayName("Should disable debug level")
        void shouldDisableDebugLevel() {
            assertFalse(selectiveLogger.isDebugEnabled());
        }

        @Test
        @DisplayName("Should enable info level")
        void shouldEnableInfoLevel() {
            assertTrue(selectiveLogger.isInfoEnabled());
        }

        @Test
        @DisplayName("Should enable warn level")
        void shouldEnableWarnLevel() {
            assertTrue(selectiveLogger.isWarnEnabled());
        }

        @Test
        @DisplayName("Should enable error level")
        void shouldEnableErrorLevel() {
            assertTrue(selectiveLogger.isErrorEnabled());
        }

        @Test
        @DisplayName("Should not record trace and debug events")
        void shouldNotRecordTraceAndDebugEvents() {
            selectiveLogger.trace("Trace message");
            selectiveLogger.debug("Debug message");

            MockLogger mockLogger = (MockLogger) selectiveLogger;
            assertEquals(0, mockLogger.getEventCount());
        }

        @Test
        @DisplayName("Should record info, warn and error events")
        void shouldRecordInfoWarnAndErrorEvents() {
            selectiveLogger.info("Info message");
            selectiveLogger.warn("Warn message");
            selectiveLogger.error("Error message");

            MockLogger mockLogger = (MockLogger) selectiveLogger;
            assertEquals(3, mockLogger.getEventCount());
            assertEquals(MockLoggerEvent.Level.INFO, mockLogger.getLoggerEvents().get(0).getLevel());
            assertEquals(MockLoggerEvent.Level.WARN, mockLogger.getLoggerEvents().get(1).getLevel());
            assertEquals(MockLoggerEvent.Level.ERROR, mockLogger.getLoggerEvents().get(2).getLevel());
        }
    }

    /**
     * Tests for parameter injection.
     */
    @Nested
    @DisplayName("Parameter Injection")
    @ExtendWith(MockLoggerExtension.class)
    class ParameterInjectionTest {

        @Test
        @DisplayName("Should inject logger into test method parameter")
        void shouldInjectLoggerIntoTestMethodParameter(@Slf4jMock Logger paramLogger) {
            assertNotNull(paramLogger);
            assertInstanceOf(MockLogger.class, paramLogger);
        }

        @Test
        @DisplayName("Should inject MockLogger into test method parameter")
        void shouldInjectMockLoggerIntoTestMethodParameter(@Slf4jMock MockLogger paramLogger) {
            assertNotNull(paramLogger);
            assertInstanceOf(MockLogger.class, paramLogger);
        }

        @Test
        @DisplayName("Should inject logger with custom name into parameter")
        void shouldInjectLoggerWithCustomNameIntoParameter(@Slf4jMock(value = "param.logger") Logger paramLogger) {
            MockLogger mockLogger = (MockLogger) paramLogger;
            assertEquals("param.logger", mockLogger.getName());
        }

        @Test
        @DisplayName("Should inject logger with type-based name into parameter")
        void shouldInjectLoggerWithTypeBasedNameIntoParameter(@Slf4jMock(type = Double.class) Logger paramLogger) {
            MockLogger mockLogger = (MockLogger) paramLogger;
            assertEquals(Double.class.getName(), mockLogger.getName());
        }

        @Test
        @DisplayName("Should inject disabled logger into parameter")
        void shouldInjectDisabledLoggerIntoParameter(@Slf4jMock(enabled = false) Logger paramLogger) {
            paramLogger.info("Should not be recorded");

            MockLogger mockLogger = (MockLogger) paramLogger;
            assertEquals(0, mockLogger.getEventCount());
        }

        @Test
        @DisplayName("Should inject logger with selective levels into parameter")
        void shouldInjectLoggerWithSelectiveLevelsIntoParameter(
                @Slf4jMock(infoEnabled = false, warnEnabled = false) Logger paramLogger) {

            assertFalse(paramLogger.isInfoEnabled());
            assertFalse(paramLogger.isWarnEnabled());
            assertTrue(paramLogger.isTraceEnabled());
            assertTrue(paramLogger.isDebugEnabled());
            assertTrue(paramLogger.isErrorEnabled());
        }

        @Test
        @DisplayName("Should inject multiple loggers into parameters")
        void shouldInjectMultipleLoggersIntoParameters(
                @Slf4jMock(value = "logger1") Logger logger1,
                @Slf4jMock(value = "logger2") Logger logger2) {

            assertNotNull(logger1);
            assertNotNull(logger2);
            assertNotSame(logger1, logger2);

            MockLogger mock1 = (MockLogger) logger1;
            MockLogger mock2 = (MockLogger) logger2;

            assertEquals("logger1", mock1.getName());
            assertEquals("logger2", mock2.getName());
        }
    }

    /**
     * Tests for multiple fields injection.
     */
    @Nested
    @DisplayName("Multiple Fields Injection")
    @ExtendWith(MockLoggerExtension.class)
    class MultipleFieldsInjectionTest {

        @Slf4jMock(value = "logger.one")
        private Logger logger1;

        @Slf4jMock(value = "logger.two")
        private Logger logger2;

        @Slf4jMock(value = "logger.three")
        private MockLogger logger3;

        @Test
        @DisplayName("Should inject multiple logger fields")
        void shouldInjectMultipleLoggerFields() {
            assertNotNull(logger1);
            assertNotNull(logger2);
            assertNotNull(logger3);

            assertNotSame(logger1, logger2);
            assertNotSame(logger2, logger3);
            assertNotSame(logger1, logger3);
        }

        @Test
        @DisplayName("Should configure each logger independently")
        void shouldConfigureEachLoggerIndependently() {
            MockLogger mock1 = (MockLogger) logger1;
            MockLogger mock2 = (MockLogger) logger2;

            assertEquals("logger.one", mock1.getName());
            assertEquals("logger.two", mock2.getName());
            assertEquals("logger.three", logger3.getName());
        }

        @Test
        @DisplayName("Should isolate events between loggers")
        void shouldIsolateEvents betweenLoggers() {
            logger1.info("Message to logger1");
            logger2.warn("Message to logger2");
            logger3.error("Message to logger3");

            MockLogger mock1 = (MockLogger) logger1;
            MockLogger mock2 = (MockLogger) logger2;

            assertEquals(1, mock1.getEventCount());
            assertEquals(1, mock2.getEventCount());
            assertEquals(1, logger3.getEventCount());

            assertEquals("Message to logger1", mock1.getLoggerEvents().get(0).getMessage());
            assertEquals("Message to logger2", mock2.getLoggerEvents().get(0).getMessage());
            assertEquals("Message to logger3", logger3.getLoggerEvents().get(0).getMessage());
        }
    }

    /**
     * Tests for event clearing between test methods.
     */
    @Nested
    @DisplayName("Event Clearing Between Tests")
    @ExtendWith(MockLoggerExtension.class)
    class EventClearingTest {

        @Slf4jMock
        private Logger logger;

        @Test
        @DisplayName("Should start with no events in first test")
        void shouldStartWithNoEventsInFirstTest() {
            MockLogger mockLogger = (MockLogger) logger;
            assertEquals(0, mockLogger.getEventCount());

            logger.info("First test message");
            assertEquals(1, mockLogger.getEventCount());
        }

        @Test
        @DisplayName("Should start with no events in second test")
        void shouldStartWithNoEventsInSecondTest() {
            MockLogger mockLogger = (MockLogger) logger;
            assertEquals(0, mockLogger.getEventCount());

            logger.info("Second test message");
            assertEquals(1, mockLogger.getEventCount());
        }

        @Test
        @DisplayName("Should start with no events in third test")
        void shouldStartWithNoEventsInThirdTest() {
            MockLogger mockLogger = (MockLogger) logger;
            assertEquals(0, mockLogger.getEventCount());

            logger.info("Third test message");
            assertEquals(1, mockLogger.getEventCount());
        }
    }

    /**
     * Tests for mixed field and parameter injection.
     */
    @Nested
    @DisplayName("Mixed Field and Parameter Injection")
    @ExtendWith(MockLoggerExtension.class)
    class MixedInjectionTest {

        @Slf4jMock(value = "field.logger")
        private Logger fieldLogger;

        @Test
        @DisplayName("Should inject both field and parameter loggers")
        void shouldInject bothFieldAndParameterLoggers(@Slf4jMock(value = "param.logger") Logger paramLogger) {
            assertNotNull(fieldLogger);
            assertNotNull(paramLogger);
            assertNotSame(fieldLogger, paramLogger);

            MockLogger fieldMock = (MockLogger) fieldLogger;
            MockLogger paramMock = (MockLogger) paramLogger;

            assertEquals("field.logger", fieldMock.getName());
            assertEquals("param.logger", paramMock.getName());
        }

        @Test
        @DisplayName("Should isolate events between field and parameter loggers")
        void shouldIsolateEventsBetweenFieldAndParameterLoggers(
                @Slf4jMock(value = "param.logger") Logger paramLogger) {

            fieldLogger.info("Field message");
            paramLogger.warn("Parameter message");

            MockLogger fieldMock = (MockLogger) fieldLogger;
            MockLogger paramMock = (MockLogger) paramLogger;

            assertEquals(1, fieldMock.getEventCount());
            assertEquals(1, paramMock.getEventCount());

            assertEquals("Field message", fieldMock.getLoggerEvents().get(0).getMessage());
            assertEquals("Parameter message", paramMock.getLoggerEvents().get(0).getMessage());
        }
    }

    /**
     * Tests for logger with all levels disabled individually.
     */
    @Nested
    @DisplayName("Logger with All Levels Disabled")
    @ExtendWith(MockLoggerExtension.class)
    class AllLevelsDisabledTest {

        @Slf4jMock(
            traceEnabled = false,
            debugEnabled = false,
            infoEnabled = false,
            warnEnabled = false,
            errorEnabled = false
        )
        private Logger allDisabledLogger;

        @Test
        @DisplayName("Should have all levels disabled")
        void shouldHaveAllLevelsDisabled() {
            assertFalse(allDisabledLogger.isTraceEnabled());
            assertFalse(allDisabledLogger.isDebugEnabled());
            assertFalse(allDisabledLogger.isInfoEnabled());
            assertFalse(allDisabledLogger.isWarnEnabled());
            assertFalse(allDisabledLogger.isErrorEnabled());
        }

        @Test
        @DisplayName("Should not record any events when all levels disabled")
        void shouldNotRecordAnyEventsWhenAllLevelsDisabled() {
            allDisabledLogger.trace("Trace");
            allDisabledLogger.debug("Debug");
            allDisabledLogger.info("Info");
            allDisabledLogger.warn("Warn");
            allDisabledLogger.error("Error");

            MockLogger mockLogger = (MockLogger) allDisabledLogger;
            assertEquals(0, mockLogger.getEventCount());
        }
    }

    /**
     * Tests for logger re-initialization between tests.
     */
    @Nested
    @DisplayName("Logger Re-initialization")
    @ExtendWith(MockLoggerExtension.class)
    class LoggerReInitializationTest {

        @Slf4jMock(value = "consistent.logger", infoEnabled = false)
        private Logger consistentLogger;

        @Test
        @DisplayName("Should maintain configuration across re-initialization - test 1")
        void shouldMaintainConfigurationAcrossReInitializationTest1() {
            assertFalse(consistentLogger.isInfoEnabled());
            assertTrue(consistentLogger.isDebugEnabled());

            consistentLogger.debug("Debug message");
            MockLogger mockLogger = (MockLogger) consistentLogger;
            assertEquals(1, mockLogger.getEventCount());
        }

        @Test
        @DisplayName("Should maintain configuration across re-initialization - test 2")
        void shouldMaintainConfigurationAcrossReInitializationTest2() {
            // Configuration should be re-applied
            assertFalse(consistentLogger.isInfoEnabled());
            assertTrue(consistentLogger.isDebugEnabled());

            // Events should be cleared
            MockLogger mockLogger = (MockLogger) consistentLogger;
            assertEquals(0, mockLogger.getEventCount());
        }
    }

    /**
     * Tests for MockLogger-specific field type.
     */
    @Nested
    @DisplayName("MockLogger Field Type")
    @ExtendWith(MockLoggerExtension.class)
    class MockLoggerFieldTypeTest {

        @Slf4jMock
        private MockLogger mockLoggerField;

        @Test
        @DisplayName("Should inject MockLogger directly into MockLogger-typed field")
        void shouldInjectMockLoggerDirectlyIntoMockLoggerTypedField() {
            assertNotNull(mockLoggerField);
            assertInstanceOf(MockLogger.class, mockLoggerField);
        }

        @Test
        @DisplayName("Should access MockLogger-specific methods directly")
        void shouldAccessMockLoggerSpecificMethodsDirectly() {
            assertEquals(0, mockLoggerField.getEventCount());

            mockLoggerField.info("Test");
            assertEquals(1, mockLoggerField.getEventCount());

            mockLoggerField.clearEvents();
            assertEquals(0, mockLoggerField.getEventCount());
        }
    }

    /**
     * Tests for fields without @Slf4jMock annotation (should be ignored).
     */
    @Nested
    @DisplayName("Fields Without Annotation")
    @ExtendWith(MockLoggerExtension.class)
    class FieldsWithoutAnnotationTest {

        @Slf4jMock
        private Logger annotatedLogger;

        // This field should NOT be injected because it lacks @Slf4jMock annotation
        private Logger unannotatedLogger;

        // This field should be ignored (not a Logger type)
        @Slf4jMock
        private String notALogger;

        @Test
        @DisplayName("Should inject annotated logger field")
        void shouldInjectAnnotatedLoggerField() {
            assertNotNull(annotatedLogger, "should inject field with @Slf4jMock annotation");
            assertInstanceOf(MockLogger.class, annotatedLogger);
        }

        @Test
        @DisplayName("Should inject unannotated logger field with default configuration")
        void shouldInjectUnannotatedLoggerFieldWithDefaultConfiguration() {
            // The extension injects ALL Logger/MockLogger fields, regardless of annotation
            // The @Slf4jMock annotation is only used for configuration
            assertNotNull(unannotatedLogger, "should inject Logger field even without annotation");
            assertInstanceOf(MockLogger.class, unannotatedLogger);

            // Verify it has default configuration (all levels enabled)
            assertTrue(unannotatedLogger.isInfoEnabled(), "should have default config with info enabled");
        }

        @Test
        @DisplayName("Should ignore non-logger fields even with annotation")
        void shouldIgnoreNonLoggerFieldsEvenWithAnnotation() {
            assertNull(notALogger, "should ignore non-Logger fields even if annotated");
        }
    }

    /**
     * Tests for disabled logger with specific levels configured (levels should be ignored).
     */
    @Nested
    @DisplayName("Disabled Logger with Level Configuration")
    @ExtendWith(MockLoggerExtension.class)
    class DisabledLoggerWithLevelsTest {

        @Slf4jMock(enabled = false, infoEnabled = true, errorEnabled = true)
        private Logger disabledWithLevels;

        @Test
        @DisplayName("Should ignore level configuration when logger is disabled")
        void shouldIgnoreLevelConfigurationWhenLoggerIsDisabled() {
            // When enabled = false, level configurations should be ignored
            // The logger should not record any events
            disabledWithLevels.info("Info message");
            disabledWithLevels.error("Error message");

            final MockLogger mockLogger = (MockLogger) disabledWithLevels;
            assertEquals(0, mockLogger.getEventCount(), "should not record events when disabled");
        }
    }

    /**
     * Tests for empty string values in @Slf4jMock annotation.
     */
    @Nested
    @DisplayName("Empty String Configuration")
    @ExtendWith(MockLoggerExtension.class)
    class EmptyStringConfigurationTest {

        @Slf4jMock(value = "")
        private Logger emptyValueLogger;

        @Test
        @DisplayName("Should use test class name when value is empty string")
        void shouldUseTestClassNameWhenValueIsEmptyString() {
            final MockLogger mockLogger = (MockLogger) emptyValueLogger;
            assertEquals(EmptyStringConfigurationTest.class.getName(), mockLogger.getName(),
                "should fall back to test class name when value is empty");
        }
    }

    /**
     * Tests for Void.class type configuration (should fall back to test class name).
     */
    @Nested
    @DisplayName("Void Type Configuration")
    @ExtendWith(MockLoggerExtension.class)
    class VoidTypeConfigurationTest {

        @Slf4jMock(type = Void.class)
        private Logger voidTypeLogger;

        @Test
        @DisplayName("Should use test class name when type is Void.class")
        void shouldUseTestClassNameWhenTypeIsVoidClass() {
            final MockLogger mockLogger = (MockLogger) voidTypeLogger;
            assertEquals(VoidTypeConfigurationTest.class.getName(), mockLogger.getName(),
                "should fall back to test class name when type is Void.class");
        }
    }

    /**
     * Tests for parameter resolver support checking.
     */
    @Nested
    @DisplayName("Parameter Resolver Support")
    @ExtendWith(MockLoggerExtension.class)
    class ParameterResolverSupportTest {

        @Test
        @DisplayName("Should support Logger parameter type")
        void shouldSupportLoggerParameterType(@Slf4jMock final Logger logger) {
            assertNotNull(logger, "should resolve Logger parameter");
        }

        @Test
        @DisplayName("Should support MockLogger parameter type")
        void shouldSupportMockLoggerParameterType(@Slf4jMock final MockLogger mockLogger) {
            assertNotNull(mockLogger, "should resolve MockLogger parameter");
        }

        @Test
        @DisplayName("Should support parameter without annotation using default config")
        void shouldSupportParameterWithoutAnnotation(final Logger logger) {
            // Without @Slf4jMock, the extension should still create a logger with defaults
            assertNotNull(logger, "should resolve Logger parameter even without annotation");
            assertInstanceOf(MockLogger.class, logger);

            final MockLogger mockLogger = (MockLogger) logger;
            assertTrue(mockLogger.isInfoEnabled(), "should have default config with all levels enabled");
        }
    }

    /**
     * Tests for multiple parameters with different configurations.
     */
    @Nested
    @DisplayName("Multiple Parameters with Different Configs")
    @ExtendWith(MockLoggerExtension.class)
    class MultipleParametersDifferentConfigsTest {

        @Test
        @DisplayName("Should inject parameters with different level configurations")
        void shouldInjectParametersWithDifferentLevelConfigurations(
                @Slf4jMock(value = "logger.config1", traceEnabled = false, debugEnabled = false) final Logger logger1,
                @Slf4jMock(value = "logger.config2", warnEnabled = false, errorEnabled = false) final Logger logger2) {

            assertFalse(logger1.isTraceEnabled(), "logger1 should have trace disabled");
            assertFalse(logger1.isDebugEnabled(), "logger1 should have debug disabled");
            assertTrue(logger1.isInfoEnabled(), "logger1 should have info enabled");

            assertTrue(logger2.isTraceEnabled(), "logger2 should have trace enabled");
            assertFalse(logger2.isWarnEnabled(), "logger2 should have warn disabled");
            assertFalse(logger2.isErrorEnabled(), "logger2 should have error disabled");
        }

        @Test
        @DisplayName("Should inject parameters with different enabled states")
        void shouldInjectParametersWithDifferentEnabledStates(
                @Slf4jMock(value = "logger.enabled", enabled = true) final Logger enabledLogger,
                @Slf4jMock(value = "logger.disabled", enabled = false) final Logger disabledLogger) {

            enabledLogger.info("Should be recorded");
            disabledLogger.info("Should not be recorded");

            final MockLogger enabled = (MockLogger) enabledLogger;
            final MockLogger disabled = (MockLogger) disabledLogger;

            assertEquals(1, enabled.getEventCount(), "enabled logger should record events");
            assertEquals(0, disabled.getEventCount(), "disabled logger should not record events");
        }
    }

    /**
     * Tests for combination of empty value and Void type (should fall back to test class name).
     */
    @Nested
    @DisplayName("Empty Value and Void Type Combination")
    @ExtendWith(MockLoggerExtension.class)
    class EmptyValueVoidTypeCombinationTest {

        @Slf4jMock(value = "", type = Void.class)
        private Logger fallbackLogger;

        @Test
        @DisplayName("Should fall back to test class name when both value is empty and type is Void")
        void shouldFallBackToTestClassNameWhenBothEmpty() {
            final MockLogger mockLogger = (MockLogger) fallbackLogger;
            assertEquals(EmptyValueVoidTypeCombinationTest.class.getName(), mockLogger.getName(),
                "should use test class name as fallback");
        }
    }

    /**
     * Tests for selective level configuration with only some levels specified.
     */
    @Nested
    @DisplayName("Partial Level Configuration")
    @ExtendWith(MockLoggerExtension.class)
    class PartialLevelConfigurationTest {

        @Slf4jMock(infoEnabled = false)
        private Logger partiallyConfiguredLogger;

        @Test
        @DisplayName("Should disable only specified level and enable others by default")
        void shouldDisableOnlySpecifiedLevelAndEnableOthersByDefault() {
            assertTrue(partiallyConfiguredLogger.isTraceEnabled(), "trace should be enabled by default");
            assertTrue(partiallyConfiguredLogger.isDebugEnabled(), "debug should be enabled by default");
            assertFalse(partiallyConfiguredLogger.isInfoEnabled(), "info should be disabled as specified");
            assertTrue(partiallyConfiguredLogger.isWarnEnabled(), "warn should be enabled by default");
            assertTrue(partiallyConfiguredLogger.isErrorEnabled(), "error should be enabled by default");
        }
    }

    /**
     * Tests for logger name resolution with various class types.
     */
    @Nested
    @DisplayName("Logger Name with Various Types")
    @ExtendWith(MockLoggerExtension.class)
    class LoggerNameVariousTypesTest {

        @Slf4jMock(type = java.util.ArrayList.class)
        private Logger arrayListLogger;

        @Slf4jMock(type = org.slf4j.Logger.class)
        private Logger loggerTypeLogger;

        @Test
        @DisplayName("Should use ArrayList fully qualified name as logger name")
        void shouldUseArrayListFullyQualifiedNameAsLoggerName() {
            final MockLogger mockLogger = (MockLogger) arrayListLogger;
            assertEquals(java.util.ArrayList.class.getName(), mockLogger.getName(),
                "should use ArrayList class name");
        }

        @Test
        @DisplayName("Should use Logger interface fully qualified name as logger name")
        void shouldUseLoggerInterfaceFullyQualifiedNameAsLoggerName() {
            final MockLogger mockLogger = (MockLogger) loggerTypeLogger;
            assertEquals(org.slf4j.Logger.class.getName(), mockLogger.getName(),
                "should use Logger interface name");
        }
    }

    /**
     * Tests for reinitializing logger between test methods maintains independence.
     */
    @Nested
    @DisplayName("Logger Independence Between Tests")
    @ExtendWith(MockLoggerExtension.class)
    class LoggerIndependenceBetweenTestsTest {

        @Slf4jMock(value = "independence.test")
        private Logger logger;

        @Test
        @DisplayName("First test modifies logger state")
        void firstTestModifiesLoggerState() {
            logger.info("First message");
            logger.warn("Second message");
            logger.error("Third message");

            final MockLogger mockLogger = (MockLogger) logger;
            assertEquals(3, mockLogger.getEventCount(), "should have 3 events");
        }

        @Test
        @DisplayName("Second test starts with clean logger state")
        void secondTestStartsWithCleanLoggerState() {
            final MockLogger mockLogger = (MockLogger) logger;
            assertEquals(0, mockLogger.getEventCount(),
                "should start with 0 events (cleared from previous test)");
        }
    }

    /**
     * Tests for parameter with empty value annotation.
     */
    @Nested
    @DisplayName("Parameter with Empty Value")
    @ExtendWith(MockLoggerExtension.class)
    class ParameterEmptyValueTest {

        @Test
        @DisplayName("Should use test class name for parameter with empty value")
        void shouldUseTestClassNameForParameterWithEmptyValue(@Slf4jMock(value = "") final Logger logger) {
            final MockLogger mockLogger = (MockLogger) logger;
            assertEquals(ParameterEmptyValueTest.class.getName(), mockLogger.getName(),
                "should fall back to test class name");
        }
    }

    /**
     * Tests for verifying events are actually cleared before each test.
     */
    @Nested
    @DisplayName("Event Clearing Verification")
    @ExtendWith(MockLoggerExtension.class)
    class EventClearingVerificationTest {

        @Slf4jMock
        private Logger logger;

        @Test
        @DisplayName("Test 1 - Add events")
        void test1AddEvents() {
            logger.info("Test 1 message");
            logger.error("Test 1 error");

            final MockLogger mockLogger = (MockLogger) logger;
            assertEquals(2, mockLogger.getEventCount(), "should have 2 events in test 1");
        }

        @Test
        @DisplayName("Test 2 - Verify clean state")
        void test2VerifyCleanState() {
            final MockLogger mockLogger = (MockLogger) logger;
            assertEquals(0, mockLogger.getEventCount(),
                "should have 0 events at start of test 2");

            logger.debug("Test 2 message");
            assertEquals(1, mockLogger.getEventCount(), "should have 1 event after logging");
        }

        @Test
        @DisplayName("Test 3 - Verify clean state again")
        void test3VerifyCleanStateAgain() {
            final MockLogger mockLogger = (MockLogger) logger;
            assertEquals(0, mockLogger.getEventCount(),
                "should have 0 events at start of test 3");
        }
    }

    /**
     * Tests for resetting additional loggers specified in @WithMockLogger.
     */
    @Nested
    @DisplayName("Reset Additional Loggers")
    @WithMockLogger(reset = {"reset.logger.1", "reset.logger.2"})
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ResetAdditionalLoggersTest {

        @Test
        @Order(1)
        @DisplayName("Should reset specified loggers before test")
        void shouldResetSpecifiedLoggersBeforeTest() {
            // Loggers should be clean at start of test
            MockLogger logger1 = (MockLogger) LoggerFactory.getLogger("reset.logger.1");
            MockLogger logger2 = (MockLogger) LoggerFactory.getLogger("reset.logger.2");

            assertEquals(0, logger1.getEventCount(), "logger1 should be empty at start");
            assertEquals(0, logger2.getEventCount(), "logger2 should be empty at start");

            // Add events
            logger1.info("Event 1");
            logger2.info("Event 2");
        }

        @Test
        @Order(2)
        @DisplayName("Should reset specified loggers between tests")
        void shouldResetSpecifiedLoggersBetweenTests() {
            // Loggers should be clean again, even if previous test added events
            MockLogger logger1 = (MockLogger) LoggerFactory.getLogger("reset.logger.1");
            MockLogger logger2 = (MockLogger) LoggerFactory.getLogger("reset.logger.2");

            assertEquals(0, logger1.getEventCount(), "logger1 should be empty at start of second test");
            assertEquals(0, logger2.getEventCount(), "logger2 should be empty at start of second test");
        }
    }
}
