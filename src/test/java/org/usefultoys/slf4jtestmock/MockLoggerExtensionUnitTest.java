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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AIGenerated("copilot")
@DisplayName("MockLoggerExtension (Unit)")
class MockLoggerExtensionUnitTest {

    @Nested
    @DisplayName("supportsParameter")
    class SupportsParameter {

        @Test
        @DisplayName("should support parameters of type Logger")
        void shouldSupportParametersOfTypeLogger() throws Exception {
            final MockLoggerExtension extension = new MockLoggerExtension();
            final Method method = DummyMethods.class.getDeclaredMethod("oneLogger", Logger.class);
            final Parameter parameter = method.getParameters()[0];

            final ParameterContext parameterContext = newParameterContext(parameter);
            final ExtensionContext extensionContext = newExtensionContext(DummyMethods.class, new Object());

            assertTrue(extension.supportsParameter(parameterContext, extensionContext),
                "should support parameters of type Logger");
        }

        @Test
        @DisplayName("should support parameters of type MockLogger")
        void shouldSupportParametersOfTypeMockLogger() throws Exception {
            final MockLoggerExtension extension = new MockLoggerExtension();
            final Method method = DummyMethods.class.getDeclaredMethod("oneMockLogger", MockLogger.class);
            final Parameter parameter = method.getParameters()[0];

            final ParameterContext parameterContext = newParameterContext(parameter);
            final ExtensionContext extensionContext = newExtensionContext(DummyMethods.class, new Object());

            assertTrue(extension.supportsParameter(parameterContext, extensionContext),
                "should support parameters of type MockLogger");
        }

        @Test
        @DisplayName("should not support parameters of other types")
        void shouldNotSupportParametersOfOtherTypes() throws Exception {
            final MockLoggerExtension extension = new MockLoggerExtension();
            final Method method = DummyMethods.class.getDeclaredMethod("oneString", String.class);
            final Parameter parameter = method.getParameters()[0];

            final ParameterContext parameterContext = newParameterContext(parameter);
            final ExtensionContext extensionContext = newExtensionContext(DummyMethods.class, new Object());

            assertFalse(extension.supportsParameter(parameterContext, extensionContext),
                "should not support parameters other than Logger and MockLogger");
        }
    }

    @Nested
    @DisplayName("beforeEach")
    class BeforeEach {

        @Test
        @DisplayName("should ignore logger fields that are still null")
        void shouldIgnoreLoggerFieldsThatAreStillNull() {
            final MockLoggerExtension extension = new MockLoggerExtension();
            final NullLoggerFieldTestInstance testInstance = new NullLoggerFieldTestInstance();

            final ExtensionContext extensionContext = newExtensionContext(NullLoggerFieldTestInstance.class, testInstance);

            assertDoesNotThrow(() -> extension.beforeEach(extensionContext),
                "should not throw when encountering a Logger-typed field with null value");
            assertNull(testInstance.logger, "should keep null logger field unchanged");
        }

        @Test
        @DisplayName("should clear additional loggers declared in @WithMockLogger(reset = ...)")
        void shouldClearAdditionalLoggersDeclaredInWithMockLoggerReset() {
            final MockLoggerExtension extension = new MockLoggerExtension();
            final WithResetLoggersTestInstance testInstance = new WithResetLoggersTestInstance();

            final Logger additional = LoggerFactory.getLogger(WithResetLoggersTestInstance.RESET_LOGGER_NAME);
            assertTrue(additional instanceof MockLogger,
                "should get a MockLogger from LoggerFactory for reset loggers in tests");

            additional.info("one event");
            assertTrue(((MockLogger) additional).getEventCount() > 0,
                "should have at least one event before extension reset");

            final ExtensionContext extensionContext = newExtensionContext(WithResetLoggersTestInstance.class, testInstance);

            assertDoesNotThrow(() -> extension.beforeEach(extensionContext),
                "should not throw while resetting additional loggers");
            assertEquals(0, ((MockLogger) additional).getEventCount(),
                "should clear events for additional loggers declared in @WithMockLogger(reset = ...)"
            );
        }

        @Test
        @DisplayName("should fail fast when a Logger field is not a MockLogger")
        void shouldFailFastWhenLoggerFieldIsNotAMockLogger() {
            final MockLoggerExtension extension = new MockLoggerExtension();
            final NonMockLoggerFieldTestInstance testInstance = new NonMockLoggerFieldTestInstance();

            final ExtensionContext extensionContext = newExtensionContext(NonMockLoggerFieldTestInstance.class, testInstance);

            assertThrows(ExtensionConfigurationException.class,
                () -> extension.beforeEach(extensionContext),
                "should throw ExtensionConfigurationException when Logger field is not a MockLogger");
        }
    }

    @Nested
    @DisplayName("postProcessTestInstance")
    class PostProcessTestInstance {

        @Test
        @DisplayName("should inject MockLogger into MockLogger-typed field")
        void shouldInjectMockLoggerIntoMockLoggerTypedField() {
            final MockLoggerExtension extension = new MockLoggerExtension();
            final MockLoggerFieldTestInstance testInstance = new MockLoggerFieldTestInstance();

            final ExtensionContext extensionContext = newExtensionContext(MockLoggerFieldTestInstance.class, testInstance);

            assertDoesNotThrow(() -> extension.postProcessTestInstance(testInstance, extensionContext),
                "should not throw while injecting logger into MockLogger-typed field");
            assertNotNull(testInstance.logger, "should inject a non-null MockLogger instance");
            assertEquals(MockLoggerFieldTestInstance.class.getName(), testInstance.logger.getName(),
                "should default logger name to test class name");
        }
    }

    private static ParameterContext newParameterContext(final Parameter parameter) {
        return (ParameterContext) Proxy.newProxyInstance(
            ParameterContext.class.getClassLoader(),
            new Class<?>[]{ParameterContext.class},
            (proxy, method, args) -> {
                if ("getParameter".equals(method.getName())) {
                    return parameter;
                }
                if ("getIndex".equals(method.getName())) {
                    return 0;
                }
                if ("getTarget".equals(method.getName())) {
                    return null;
                }
                if ("isAnnotated".equals(method.getName())) {
                    return Boolean.FALSE;
                }
                if ("findAnnotation".equals(method.getName())) {
                    return java.util.Optional.empty();
                }
                if ("findRepeatableAnnotations".equals(method.getName())) {
                    return java.util.Optional.empty();
                }
                throw new UnsupportedOperationException("Unsupported ParameterContext method: " + method.getName());
            }
        );
    }

    private static ExtensionContext newExtensionContext(final Class<?> requiredTestClass, final Object requiredTestInstance) {
        return (ExtensionContext) Proxy.newProxyInstance(
            ExtensionContext.class.getClassLoader(),
            new Class<?>[]{ExtensionContext.class},
            (proxy, method, args) -> {
                if ("getRequiredTestClass".equals(method.getName())) {
                    return requiredTestClass;
                }
                if ("getRequiredTestInstance".equals(method.getName())) {
                    return requiredTestInstance;
                }
                if ("getRequiredTestMethod".equals(method.getName())) {
                    throw new UnsupportedOperationException("getRequiredTestMethod is not used by these unit tests");
                }
                if ("getStore".equals(method.getName())) {
                    throw new UnsupportedOperationException("getStore is not used by these unit tests");
                }
                if ("getUniqueId".equals(method.getName())) {
                    return "mockloggerextension-unit";
                }
                if (method.getReturnType().equals(boolean.class)) {
                    return Boolean.FALSE;
                }
                if (method.getReturnType().equals(int.class)) {
                    return 0;
                }
                return null;
            }
        );
    }

    static final class DummyMethods {
        static void oneLogger(final Logger logger) {
            // no-op
        }

        static void oneMockLogger(final MockLogger logger) {
            // no-op
        }

        static void oneString(final String value) {
            // no-op
        }
    }

    static final class NullLoggerFieldTestInstance {
        Logger logger;
    }

    @WithMockLogger(reset = {WithResetLoggersTestInstance.RESET_LOGGER_NAME})
    static final class WithResetLoggersTestInstance {
        static final String RESET_LOGGER_NAME = "unit.reset.logger";

        Logger logger;
    }

    static final class MockLoggerFieldTestInstance {
        MockLogger logger;
    }

    static final class NonMockLoggerFieldTestInstance {
        final Logger logger = (Logger) Proxy.newProxyInstance(
            Logger.class.getClassLoader(),
            new Class<?>[]{Logger.class},
            (proxy, method, args) -> {
                if ("getName".equals(method.getName())) {
                    return "non-mock";
                }
                if (method.getReturnType().equals(boolean.class)) {
                    return Boolean.FALSE;
                }
                return null;
            }
        );
    }
}
