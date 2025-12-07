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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JUnit 5 Extension that automatically prints logged events when a test fails.
 * <p>
 * This extension intercepts test execution and catches assertion errors.
 * When an assertion fails, it prints all logged events from MockLogger instances
 * found in the test method parameters, making it easier to debug test failures.
 * <p>
 * Usage:
 * <pre>{@code
 * @ExtendWith(AssertLoggerDebugExtension.class)
 * class MyTest {
 *     @Test
 *     void testSomething() {
 *         Logger logger = LoggerFactory.getLogger("test");
 *         logger.info("test message");
 *         AssertLogger.assertEvent(logger, 0, "expected"); // If fails, shows all events
 *     }
 * }
 * }</pre>
 *
 * @author Daniel Felix Ferber
 * @see LoggerEventFormatter
 */
@AIGenerated("copilot")
public class MockLoggerDebugExtension implements InvocationInterceptor {

    /**
     * Intercepts test method execution to catch assertion errors and print logged events.
     *
     * @param invocation        the invocation to proceed with
     * @param invocationContext the context of the invocation
     * @param extensionContext  the extension context
     * @throws Throwable if the test method throws an exception
     */
    @Override
    public void interceptTestMethod(
            final Invocation<Void> invocation,
            final ReflectiveInvocationContext<Method> invocationContext,
            final ExtensionContext extensionContext) throws Throwable {

        try {
            invocation.proceed();
        } catch (final AssertionError e) {
            // Find and print all MockLogger instances in test parameters
            final List<MockLogger> mockLoggers = findMockLoggers(invocationContext);

            // If none found in parameters, fall back to collecting all loggers from the factory
            if (mockLoggers.isEmpty()) {
                mockLoggers.addAll(findMockLoggersFromFactory());
            }

            if (!mockLoggers.isEmpty()) {
                printLoggedEvents(mockLoggers, extensionContext);
            }

            throw e;
        }
    }

    /**
     * Finds all MockLogger instances in the test method's parameters.
     *
     * @param context the invocation context containing test parameters
     * @return a list of MockLogger instances found in the parameters
     */
    private static List<MockLogger> findMockLoggers(final ReflectiveInvocationContext<Method> context) {
        final List<MockLogger> mockLoggers = new ArrayList<>(5);
        final List<Object> arguments = context.getArguments();
        final Method method = context.getExecutable();
        final Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length && i < arguments.size(); i++) {
            final Object arg = arguments.get(i);
            if (arg instanceof MockLogger) {
                mockLoggers.add((MockLogger) arg);
            }
        }

        return mockLoggers;
    }

    /**
     * Retrieves MockLogger instances from the MockLoggerFactory.
     * This is used as a fallback when no MockLogger instances are present in method parameters.
     */
    private static List<MockLogger> findMockLoggersFromFactory() {
        final List<MockLogger> result = new ArrayList<>(8);
        final Map<String, org.slf4j.Logger> loggers = MockLoggerFactory.getLoggers();
        for (final org.slf4j.Logger logger : loggers.values()) {
            if (logger instanceof MockLogger) {
                result.add((MockLogger) logger);
            }
        }
        return result;
    }

    /**
     * Prints all logged events from the MockLogger instances to standard error.
     *
     * @param mockLoggers      the list of MockLogger instances to print events from
     * @param extensionContext the extension context for getting test information
     */
    private static void printLoggedEvents(final List<MockLogger> mockLoggers, final ExtensionContext extensionContext) {
        final String testName = extensionContext.getDisplayName();

        System.err.println();
        System.err.println("╔════════════════════════════════════════════════════════════╗");
        System.err.println("║           LOGGED EVENTS (Assertion Failed)                ║");
        System.err.println("╠════════════════════════════════════════════════════════════╣");
        System.err.printf("║ Test: %-54s ║%n", testName);
        System.err.println("╚════════════════════════════════════════════════════════════╝");

        for (int i = 0; i < mockLoggers.size(); i++) {
            final MockLogger logger = mockLoggers.get(i);

            if (mockLoggers.size() > 1) {
                final String loggerName = logger.getName();
                System.err.println();
                System.err.printf("Logger: %s%n", loggerName);
                System.err.println("────────────────────────────────────────────────────────");
            }

            System.err.println(LoggerEventFormatter.formatLoggedEvents(logger));
        }

        System.err.println();
    }
}
