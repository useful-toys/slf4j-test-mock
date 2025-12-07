package org.usefultoys.slf4jtestmock;

import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * JUnit Jupiter extension to integrate {@link MockLogger} instances into tests.
 * <p>
 * This extension provides automatic injection and configuration of {@link MockLogger}
 * instances into test class fields and method parameters annotated with {@link Slf4jMock}.
 * It ensures that {@link MockLogger} instances are properly initialized and reset
 * before each test.
 * </p>
 */
public class MockLoggerExtension implements
        TestInstancePostProcessor,
        BeforeEachCallback,
        ParameterResolver {

    /**
     * Initializes {@link Logger} fields in the test instance.
     * <p>
     * This method is called once per test instance. It scans for fields of type
     * {@link Logger} or {@link MockLogger} and, if annotated with {@link Slf4jMock},
     * creates and configures a {@link MockLogger} instance, then injects it into the field.
     * </p>
     *
     * @param testInstance The test instance.
     * @param context      The extension context.
     */
    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) {
        final Class<?> testClass = testInstance.getClass();
        final Field[] fields = testClass.getDeclaredFields();
        for (final Field field : fields) {
            if (isLoggerField(field)) {
                final Logger logger = createAndConfigureLogger(field, testClass, field.getName());
                setField(field, testInstance, logger);
            }
        }
    }

    /**
     * Resets and reconfigures injected {@link MockLogger} instances before each test method.
     * <p>
     * This ensures that each test starts with a clean {@link MockLogger} state, preventing
     * test interference.
     * </p>
     *
     * @param context The extension context.
     * @throws SecurityException        If a security manager denies access.
     * @throws IllegalArgumentException If an illegal argument is passed.
     * @throws IllegalAccessException   If the field is inaccessible.
     */
    @Override
    public void beforeEach(final ExtensionContext context) throws IllegalAccessException {
        final Object testInstance = context.getRequiredTestInstance();
        final Class<?> testClass = testInstance.getClass();
        final Field[] fields = testClass.getDeclaredFields();
        for (final Field field : fields) {
            if (isLoggerField(field)) {
                field.setAccessible(true);
                final Object value = field.get(testInstance);
                if (value instanceof Logger) {
                    final Logger slf4jLogger = (Logger) value;
                    reinitializeLogger(field, testClass, slf4jLogger);
                }
            }
        }
    }

    /**
     * Determines if a parameter can be resolved by this extension.
     * <p>
     * This extension supports parameters of type {@link Logger} or {@link MockLogger}.
     * </p>
     *
     * @param parameterContext The parameter context.
     * @param extensionContext The extension context.
     * @return {@code true} if the parameter is of type {@link Logger} or {@link MockLogger}, {@code false} otherwise.
     */
    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        final Class<?> type = parameterContext.getParameter().getType();
        return Logger.class.equals(type) || MockLogger.class.equals(type);
    }

    /**
     * Resolves a parameter of type {@link Logger} or {@link MockLogger}.
     * <p>
     * A new {@link MockLogger} instance is created and configured based on the
     * {@link Slf4jMock} annotation on the parameter, then returned.
     * </p>
     *
     * @param parameterContext The parameter context.
     * @param extensionContext The extension context.
     * @return A configured {@link Logger} or {@link MockLogger} instance.
     * @throws ParameterResolutionException If the parameter cannot be resolved.
     */
    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        final Parameter parameter = parameterContext.getParameter();
        final Class<?> testClass = extensionContext.getRequiredTestClass();
        final Logger logger = createAndConfigureLogger(parameter, testClass, parameter.getName());

        parameter.getType();
        return logger;
    }

    /**
     * Checks if a given field is of type {@link Logger} or {@link MockLogger}.
     *
     * @param field The field to check.
     * @return {@code true} if the field is a logger type, {@code false} otherwise.
     */
    private static boolean isLoggerField(final Field field) {
        final Class<?> type = field.getType();
        return Logger.class.equals(type) || MockLogger.class.equals(type);
    }

    /**
     * Creates and configures a {@link MockLogger} based on the provided annotated element.
     *
     * @param element    The annotated element (field or parameter) that may have {@link Slf4jMock} annotation.
     * @param testClass  The test class.
     * @param fallbackName The name to use if no explicit logger name is provided by the annotation.
     * @return A configured {@link Logger} instance (which will be a {@link MockLogger}).
     * @throws ExtensionConfigurationException If the logger returned by LoggerFactory is not a {@link MockLogger}.
     */
    private Logger createAndConfigureLogger(final AnnotatedElement element,
                                            final Class<?> testClass,
                                            final String fallbackName) {

        final String loggerName = resolveLoggerName(element, testClass, fallbackName);
        final Logger slf4jLogger = LoggerFactory.getLogger(loggerName);

        if (!(slf4jLogger instanceof MockLogger)) {
            throw new ExtensionConfigurationException(
                    "Expected LoggerFactory.getLogger(\"" + loggerName +
                    "\") to return a MockLogger, but got: " + slf4jLogger.getClass()
            );
        }

        final MockLogger mock = (MockLogger) slf4jLogger;
        applyConfig(mock, element);

        return slf4jLogger;
    }

    /**
     * Reinitializes an existing {@link Logger} (expected to be a {@link MockLogger}) with configuration
     * from the annotated element.
     *
     * @param element    The annotated element (field or parameter) that may have {@link Slf4jMock} annotation.
     * @param testClass  The test class.
     * @param slf4jLogger The existing {@link Logger} instance to reinitialize.
     * @throws ExtensionConfigurationException If the provided logger is not a {@link MockLogger}.
     */
    private void reinitializeLogger(final AnnotatedElement element,
                                    final Class<?> testClass,
                                    final Logger slf4jLogger) {

        if (!(slf4jLogger instanceof MockLogger)) {
            throw new ExtensionConfigurationException(
                    "Expected a MockLogger but got: " + slf4jLogger.getClass()
            );
        }

        final MockLogger mock = (MockLogger) slf4jLogger;
        applyConfig(mock, element);
    }

    /**
     * Applies the configuration from the {@link Slf4jMock} annotation to the {@link MockLogger}.
     * <p>
     * This includes clearing events and setting enabled states for the logger and its levels.
     * </p>
     *
     * @param mock    The {@link MockLogger} instance to configure.
     * @param element The annotated element providing the configuration.
     */
    private void applyConfig(final MockLogger mock, final AnnotatedElement element) {
        final Slf4jMock cfg = element.getAnnotation(Slf4jMock.class);

        // Always clear events before each test
        mock.clearEvents();

        if (cfg == null) {
            mock.setEnabled(true);
            enableAllLevels(mock);
            return;
        }

        mock.setEnabled(cfg.enabled());

        if (cfg.enabled()) {
            setIfExists(mock, "setTraceEnabled", cfg.traceEnabled());
            setIfExists(mock, "setDebugEnabled", cfg.debugEnabled());
            setIfExists(mock, "setInfoEnabled",  cfg.infoEnabled());
            setIfExists(mock, "setWarnEnabled",  cfg.warnEnabled());
            setIfExists(mock, "setErrorEnabled", cfg.errorEnabled());
        }
    }

    /**
     * Enables all logging levels (TRACE, DEBUG, INFO, WARN, ERROR) for the given {@link MockLogger}.
     *
     * @param mock The {@link MockLogger} instance.
     */
    private void enableAllLevels(final MockLogger mock) {
        setIfExists(mock, "setTraceEnabled", true);
        setIfExists(mock, "setDebugEnabled", true);
        setIfExists(mock, "setInfoEnabled",  true);
        setIfExists(mock, "setWarnEnabled",  true);
        setIfExists(mock, "setErrorEnabled", true);
    }

    /**
     * Sets a boolean property on the {@link MockLogger} using reflection, if the setter method exists.
     * <p>
     * This is used to maintain compatibility with different versions of {@link MockLogger}
     * that might not have all setter methods for log levels.
     * </p>
     *
     * @param mock       The {@link MockLogger} instance.
     * @param methodName The name of the setter method (e.g., "setTraceEnabled").
     * @param value      The boolean value to set.
     * @throws RuntimeException                If a security exception occurs during reflection.
     * @throws ExtensionConfigurationException If an error occurs during method invocation.
     */
    private static void setIfExists(final MockLogger mock, final String methodName, final boolean value) {
        try {
            final Method m = mock.getClass().getMethod(methodName, boolean.class);
            m.invoke(mock, Boolean.valueOf(value));
        } catch (final NoSuchMethodException e) {
            // Version of the library without this method – ignore
        } catch (final SecurityException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (final Exception e) {
            throw new ExtensionConfigurationException("Error invoking " + methodName, e);
        }
    }

    /**
     * Resolves the logger name based on the {@link Slf4jMock} annotation or a fallback name.
     * <p>
     * The order of precedence is: {@link Slf4jMock#value()}, then {@link Slf4jMock#type()},
     * then the provided fallback name.
     * </p>
     *
     * @param element    The annotated element (field or parameter).
     * @param testClass  The test class.
     * @param fallbackName The name to use if no explicit name is found in the annotation.
     * @return The resolved logger name.
     */
    private static String resolveLoggerName(final AnnotatedElement element,
                                            final Class<?> testClass,
                                            final String fallbackName) {

        final Slf4jMock cfg = element.getAnnotation(Slf4jMock.class);
        if (cfg != null) {
            if (!cfg.value().isEmpty()) {
                return cfg.value();
            }
            if (!cfg.type().equals(Void.class)) {
                return cfg.type().getName();
            }
        }
        // fallback: test class name
        return testClass.getName();
    }

    /**
     * Sets the value of a {@link Logger} field in the test instance.
     *
     * @param field    The field to set.
     * @param instance The test instance.
     * @param logger   The {@link Logger} instance to set.
     * @throws ExtensionConfigurationException If the field cannot be set.
     */
    private static void setField(final Field field, final Object instance, final Logger logger) {
        try {
            field.setAccessible(true);
            if (field.getType().equals(MockLogger.class) && logger instanceof MockLogger) {
                field.set(instance, (MockLogger) logger);
            } else {
                field.set(instance, logger);
            }
        } catch (final IllegalAccessException e) {
            throw new ExtensionConfigurationException("Could not set logger field: " + field, e);
        }
    }
}
