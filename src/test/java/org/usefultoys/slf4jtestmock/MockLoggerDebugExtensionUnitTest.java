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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AIGenerated("copilot")
@DisplayName("MockLoggerDebugExtension (Unit)")
class MockLoggerDebugExtensionUnitTest {

    /**
     * Unit tests for {@link MockLoggerDebugExtension}.
     * <p>
     * Tests validate that MockLoggerDebugExtension correctly finds and prints logged events
     * from MockLogger instances in various test contexts when assertions fail.
     * <p>
     * <b>Coverage:</b>
     * <ul>
     *   <li><b>No Debug Output on Success:</b> Verifies no output printed when test passes</li>
     *   <li><b>Logger as Method Parameter:</b> Finds MockLogger passed as test method argument</li>
     *   <li><b>Fallback to Factory:</b> Uses MockLoggerFactory when no parameter loggers found</li>
     *   <li><b>No Output When No Logger Exists:</b> Handles cases with no loggers gracefully</li>
     *   <li><b>Logger as Class Field:</b> Discovers MockLogger in test class fields via reflection</li>
     *   <li><b>Logger in Outer Class (@Nested):</b> Finds logger in outer class via synthetic this$0 field</li>
     *   <li><b>Logger in Inherited Fields:</b> Searches superclasses for MockLogger fields</li>
     *   <li><b>Multiple Loggers:</b> Finds and prints events from multiple MockLogger instances</li>
     * </ul>
     */

    private PrintStream originalErr;
    private ByteArrayOutputStream errCapture;

    @BeforeEach
    void setUp() {
        clearMockLoggerFactory();
        captureSystemErr();
    }

    @AfterEach
    void tearDown() {
        restoreSystemErr();
        clearMockLoggerFactory();
    }

    @Nested
    @DisplayName("interceptTestMethod")
    class InterceptTestMethod {

        @Test
        @DisplayName("should not print anything when invocation succeeds")
        void shouldNotPrintAnythingWhenInvocationSucceeds() throws Throwable {
            final MockLoggerDebugExtension extension = new MockLoggerDebugExtension();

            final InvocationInterceptor.Invocation<Void> invocation = () -> null;

            final Method method = DummyMethods.class.getDeclaredMethod("noArgs");
            final ReflectiveInvocationContext<Method> invocationContext = newInvocationContext(method, Collections.emptyList());
            final ExtensionContext extensionContext = newExtensionContext("passing test");

            extension.interceptTestMethod(invocation, invocationContext, extensionContext);

            assertEquals("", readErr(), "should not write to System.err when no AssertionError occurs");
        }

        @Test
        @DisplayName("should print logged events when invocation throws AssertionError and MockLogger is in parameters")
        void shouldPrintLoggedEventsWhenAssertionErrorAndMockLoggerIsInParameters() throws Throwable {
            final MockLoggerDebugExtension extension = new MockLoggerDebugExtension();

            final Logger logger = LoggerFactory.getLogger("unit.parameter");
            assertTrue(logger instanceof MockLogger, "should get a MockLogger from LoggerFactory in tests");

            logger.info(MarkerFactory.getMarker("SECURITY"), "marker event");
            logger.error("error with throwable", new IllegalStateException("boom"));
            logger.info("plain event");

            final InvocationInterceptor.Invocation<Void> invocation = () -> {
                throw new AssertionError("expected failure");
            };

            final Method method = DummyMethods.class.getDeclaredMethod("oneMockLogger", MockLogger.class);
            final ReflectiveInvocationContext<Method> invocationContext = newInvocationContext(method, Collections.singletonList(logger));
            final ExtensionContext extensionContext = newExtensionContext("failing test");

            final AssertionError thrown = assertThrows(AssertionError.class,
                () -> extension.interceptTestMethod(invocation, invocationContext, extensionContext),
                "should rethrow the original AssertionError");
            assertEquals("expected failure", thrown.getMessage(), "should preserve original AssertionError message");

            final String err = readErr();
            assertTrue(err.contains("LOGGED EVENTS"), "should print a header into System.err");
            assertTrue(err.contains("Total events: 3"), "should include total event count");
            assertTrue(err.contains("marker=SECURITY"), "should include marker name when present");
            assertTrue(err.contains("marker event"), "should include formatted message for marker event");
            assertTrue(err.contains("throwable:"), "should include throwable details when present");
            assertTrue(err.contains("plain event"), "should include formatted message for non-marker event");
        }

        @Test
        @DisplayName("should fall back to factory loggers when no MockLogger is in parameters")
        void shouldFallBackToFactoryLoggersWhenNoMockLoggerIsInParameters() throws Throwable {
            final MockLoggerDebugExtension extension = new MockLoggerDebugExtension();

            final ILoggerFactory factory = MockLoggerFactory.getInstance();
            final Logger factoryLogger = factory.getLogger("unit.factory." + System.nanoTime());
            assertTrue(factoryLogger instanceof MockLogger, "should create a MockLogger via MockLoggerFactory");
            factoryLogger.warn("factory event");

            final InvocationInterceptor.Invocation<Void> invocation = () -> {
                throw new AssertionError("expected failure");
            };

            final Method method = DummyMethods.class.getDeclaredMethod("oneString", String.class);
            final ReflectiveInvocationContext<Method> invocationContext = newInvocationContext(method, Collections.singletonList("not a logger"));
            final ExtensionContext extensionContext = newExtensionContext("failing test");

            assertThrows(AssertionError.class,
                () -> extension.interceptTestMethod(invocation, invocationContext, extensionContext),
                "should rethrow AssertionError");

            final String err = readErr();
            assertTrue(err.contains("factory event"), "should print events from MockLoggerFactory fallback");
            assertTrue(err.contains("Total events: 1"), "should include total event count from factory logger");
        }

        @Test
        @DisplayName("should not print anything when invocation throws AssertionError and no MockLogger exists")
        void shouldNotPrintAnythingWhenAssertionErrorAndNoMockLoggerExists() throws Throwable {
            final MockLoggerDebugExtension extension = new MockLoggerDebugExtension();

            final InvocationInterceptor.Invocation<Void> invocation = () -> {
                throw new AssertionError("expected failure");
            };

            final Method method = DummyMethods.class.getDeclaredMethod("oneString", String.class);
            final ReflectiveInvocationContext<Method> invocationContext = newInvocationContext(method, Collections.singletonList("not a logger"));
            final ExtensionContext extensionContext = newExtensionContext("failing test");

            clearMockLoggerFactory();

            assertThrows(AssertionError.class,
                () -> extension.interceptTestMethod(invocation, invocationContext, extensionContext),
                "should rethrow AssertionError");

            assertEquals("", readErr(), "should not print anything when no MockLogger is found");
        }

        @Test
        @DisplayName("should find MockLogger in test class field")
        void shouldFindMockLoggerInTestClassField() throws Throwable {
            final MockLoggerDebugExtension extension = new MockLoggerDebugExtension();

            final TestClassWithField testInstance = new TestClassWithField();
            testInstance.logger.info("field event 1");
            testInstance.logger.warn("field event 2");

            final InvocationInterceptor.Invocation<Void> invocation = () -> {
                throw new AssertionError("expected failure");
            };

            final Method method = DummyMethods.class.getDeclaredMethod("noArgs");
            final ReflectiveInvocationContext<Method> invocationContext = newInvocationContext(method, Collections.emptyList());
            final ExtensionContext extensionContext = newExtensionContextWithInstance("field test", testInstance);

            assertThrows(AssertionError.class,
                () -> extension.interceptTestMethod(invocation, invocationContext, extensionContext),
                "should rethrow AssertionError");

            final String err = readErr();
            assertTrue(err.contains("field event 1"), "should find logger in test class field");
            assertTrue(err.contains("field event 2"), "should print all events from field logger");
            assertTrue(err.contains("Total events: 2"), "should count events correctly");
        }

        @Test
        @DisplayName("should find MockLogger in outer class of @Nested test")
        void shouldFindMockLoggerInOuterClassOfNestedTest() throws Throwable {
            final MockLoggerDebugExtension extension = new MockLoggerDebugExtension();

            final OuterTestClass outerInstance = new OuterTestClass();
            outerInstance.logger.info("outer logger event");

            final OuterTestClass.InnerTestClass innerInstance = outerInstance.new InnerTestClass();

            final InvocationInterceptor.Invocation<Void> invocation = () -> {
                throw new AssertionError("expected failure");
            };

            final Method method = DummyMethods.class.getDeclaredMethod("noArgs");
            final ReflectiveInvocationContext<Method> invocationContext = newInvocationContext(method, Collections.emptyList());
            final ExtensionContext extensionContext = newExtensionContextWithInstance("nested test", innerInstance);

            assertThrows(AssertionError.class,
                () -> extension.interceptTestMethod(invocation, invocationContext, extensionContext),
                "should rethrow AssertionError");

            final String err = readErr();
            assertTrue(err.contains("outer logger event"), "should find logger in outer class via this$0");
        }

        @Test
        @DisplayName("should find MockLogger in inherited field from base class")
        void shouldFindMockLoggerInInheritedField() throws Throwable {
            final MockLoggerDebugExtension extension = new MockLoggerDebugExtension();

            final DerivedTestClass testInstance = new DerivedTestClass();
            testInstance.logger.info("inherited logger event");

            final InvocationInterceptor.Invocation<Void> invocation = () -> {
                throw new AssertionError("expected failure");
            };

            final Method method = DummyMethods.class.getDeclaredMethod("noArgs");
            final ReflectiveInvocationContext<Method> invocationContext = newInvocationContext(method, Collections.emptyList());
            final ExtensionContext extensionContext = newExtensionContextWithInstance("inheritance test", testInstance);

            assertThrows(AssertionError.class,
                () -> extension.interceptTestMethod(invocation, invocationContext, extensionContext),
                "should rethrow AssertionError");

            final String err = readErr();
            assertTrue(err.contains("inherited logger event"), "should find logger in base class via getSuperclass()");
        }

        @Test
        @DisplayName("should find multiple MockLoggers in test class fields")
        void shouldFindMultipleMockLoggersInFields() throws Throwable {
            final MockLoggerDebugExtension extension = new MockLoggerDebugExtension();

            final TestClassWithMultipleLoggers testInstance = new TestClassWithMultipleLoggers();
            testInstance.logger1.info("logger1 event");
            testInstance.logger2.warn("logger2 event");

            final InvocationInterceptor.Invocation<Void> invocation = () -> {
                throw new AssertionError("expected failure");
            };

            final Method method = DummyMethods.class.getDeclaredMethod("noArgs");
            final ReflectiveInvocationContext<Method> invocationContext = newInvocationContext(method, Collections.emptyList());
            final ExtensionContext extensionContext = newExtensionContextWithInstance("multiple loggers test", testInstance);

            assertThrows(AssertionError.class,
                () -> extension.interceptTestMethod(invocation, invocationContext, extensionContext),
                "should rethrow AssertionError");

            final String err = readErr();
            assertTrue(err.contains("logger1 event"), "should find first logger in fields");
            assertTrue(err.contains("logger2 event"), "should find second logger in fields");
            assertTrue(err.contains("Logger: unit.test.logger1"), "should display logger1 name");
            assertTrue(err.contains("Logger: unit.test.logger2"), "should display logger2 name");
        }
    }

    private static final class DummyMethods {
        private DummyMethods() {
            // Utility class
        }

        @SuppressWarnings("unused")
        private static void noArgs() {
            // no-op
        }

        @SuppressWarnings("unused")
        private static void oneMockLogger(final MockLogger logger) {
            // no-op
        }

        @SuppressWarnings("unused")
        private static void oneString(final String ignored) {
            // no-op
        }
    }

    /**
     * Test class with a MockLogger field to validate field search functionality.
     */
    private static final class TestClassWithField {
        private final Logger logger = LoggerFactory.getLogger("unit.test.field");
    }

    /**
     * Test class with multiple MockLogger fields to validate multiple logger detection.
     */
    private static final class TestClassWithMultipleLoggers {
        private final Logger logger1 = LoggerFactory.getLogger("unit.test.logger1");
        private final Logger logger2 = LoggerFactory.getLogger("unit.test.logger2");
    }

    /**
     * Base test class with a MockLogger field to validate inherited field search.
     */
    private static class BaseTestClass {
        protected final Logger logger = LoggerFactory.getLogger("unit.test.inherited");
    }

    /**
     * Derived test class extending BaseTestClass to validate inheritance.
     */
    private static final class DerivedTestClass extends BaseTestClass {
        // Inherits logger field from BaseTestClass
    }

    /**
     * Outer test class with a MockLogger field and nested test class.
     * Used to validate @Nested class logger detection via this$0 field.
     */
    private static final class OuterTestClass {
        private final Logger logger = LoggerFactory.getLogger("unit.test.outer");

        /**
         * Inner test class (simulates @Nested test structure).
         * The compiler generates a synthetic this$0 field pointing to outer instance.
         */
        private final class InnerTestClass {
            // this$0 field is synthetic, added by compiler
        }
    }

    /**
     * Multi-level nested test classes to validate recursive outer class search.
     */
    private static final class MultiLevelNested {
        private final Logger logger = LoggerFactory.getLogger("unit.test.multilevel");

        private final class MiddleNested {
            // this$0 points to MultiLevelNested

            private final class InnerNested {
                // this$0 points to MiddleNested
            }
        }
    }

    private void captureSystemErr() {
        originalErr = System.err;
        errCapture = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(errCapture, true, "UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 should be supported", e);
        }
    }

    private void restoreSystemErr() {
        if (originalErr != null) {
            System.setErr(originalErr);
        }
    }

    private String readErr() {
        assertNotNull(errCapture, "should have initialized System.err capture");
        System.err.flush();
        try {
            return errCapture.toString("UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 should be supported", e);
        }
    }

    private static void clearMockLoggerFactory() {
        try {
            final Field instanceField = MockLoggerFactory.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            final Object instance = instanceField.get(null);

            final Field mapField = MockLoggerFactory.class.getDeclaredField("nameToLogger");
            mapField.setAccessible(true);
            @SuppressWarnings("unchecked")
            final Map<String, org.slf4j.Logger> map = (Map<String, org.slf4j.Logger>) mapField.get(instance);
            map.clear();
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("should be able to clear MockLoggerFactory for test isolation", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static ReflectiveInvocationContext<Method> newInvocationContext(final Method method, final List<Object> arguments) {
        return (ReflectiveInvocationContext<Method>) Proxy.newProxyInstance(
            MockLoggerDebugExtensionUnitTest.class.getClassLoader(),
            new Class<?>[]{ReflectiveInvocationContext.class},
            (proxy, invokedMethod, args) -> {
                switch (invokedMethod.getName()) {
                    case "getExecutable":
                        return method;
                    case "getArguments":
                        return arguments;
                    case "toString":
                        return "ReflectiveInvocationContextProxy";
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "equals":
                        return proxy == args[0];
                    default:
                        return defaultValue(invokedMethod.getReturnType());
                }
            }
        );
    }

    @SuppressWarnings("unchecked")
    private static ExtensionContext newExtensionContext(final String displayName) {
        return (ExtensionContext) Proxy.newProxyInstance(
            MockLoggerDebugExtensionUnitTest.class.getClassLoader(),
            new Class<?>[]{ExtensionContext.class},
            (proxy, invokedMethod, args) -> {
                switch (invokedMethod.getName()) {
                    case "getDisplayName":
                        return displayName;
                    case "getTestInstance":
                        return Optional.empty();
                    case "toString":
                        return "ExtensionContextProxy";
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "equals":
                        return proxy == args[0];
                    default:
                        return defaultValue(invokedMethod.getReturnType());
                }
            }
        );
    }

    @SuppressWarnings("unchecked")
    private static ExtensionContext newExtensionContextWithInstance(final String displayName, final Object testInstance) {
        return (ExtensionContext) Proxy.newProxyInstance(
            MockLoggerDebugExtensionUnitTest.class.getClassLoader(),
            new Class<?>[]{ExtensionContext.class},
            (proxy, invokedMethod, args) -> {
                switch (invokedMethod.getName()) {
                    case "getDisplayName":
                        return displayName;
                    case "getTestInstance":
                        return Optional.of(testInstance);
                    case "toString":
                        return "ExtensionContextProxyWithInstance";
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "equals":
                        return proxy == args[0];
                    default:
                        return defaultValue(invokedMethod.getReturnType());
                }
            }
        );
    }

    private static Object defaultValue(final Class<?> returnType) {
        if (returnType == void.class) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0f;
        }
        if (returnType == double.class) {
            return 0d;
        }
        if (returnType == char.class) {
            return (char) 0;
        }
        if (Optional.class.isAssignableFrom(returnType)) {
            return Optional.empty();
        }
        if (List.class.isAssignableFrom(returnType)) {
            return Collections.emptyList();
        }
        if (Map.class.isAssignableFrom(returnType)) {
            return Collections.emptyMap();
        }
        return null;
    }
}
