package org.usefultoys.slf4jtestmock;

import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MockLoggerExtension implements
        TestInstancePostProcessor,
        BeforeEachCallback,
        ParameterResolver {

    // --- Inicializa campos na instância de teste ---
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

    // --- Antes de cada teste, limpa e reconfigura os loggers já injetados ---
    @Override
    public void beforeEach(final ExtensionContext context) throws SecurityException, IllegalArgumentException, IllegalAccessException {
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

    // --- Suporte a injeção via parâmetro de método ---
    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        final Class<?> type = parameterContext.getParameter().getType();
        return Logger.class.equals(type) || MockLogger.class.equals(type);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        final Parameter parameter = parameterContext.getParameter();
        final Class<?> testClass = extensionContext.getRequiredTestClass();
        final Logger logger = createAndConfigureLogger(parameter, testClass, parameter.getName());

        if (MockLogger.class.equals(parameter.getType())) {
            return (MockLogger) logger;
        }
        return logger;
    }

    // --- Helpers principais ---

    private boolean isLoggerField(final Field field) {
        final Class<?> type = field.getType();
        return Logger.class.equals(type) || MockLogger.class.equals(type);
    }

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

    private void applyConfig(final MockLogger mock, final AnnotatedElement element) {
        final Slf4jMock cfg = element.getAnnotation(Slf4jMock.class);

        // Sempre limpa eventos antes de cada teste
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

    private void enableAllLevels(final MockLogger mock) {
        setIfExists(mock, "setTraceEnabled", true);
        setIfExists(mock, "setDebugEnabled", true);
        setIfExists(mock, "setInfoEnabled",  true);
        setIfExists(mock, "setWarnEnabled",  true);
        setIfExists(mock, "setErrorEnabled", true);
    }

    // Usa reflexão para não quebrar se alguma versão não tiver certos setters
    private void setIfExists(final MockLogger mock, final String methodName, final boolean value) {
        try {
            final Method m = mock.getClass().getMethod(methodName, boolean.class);
            m.invoke(mock, Boolean.valueOf(value));
        } catch (final NoSuchMethodException e) {
            // Versão da lib sem esse método – ignora
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (final Exception e) {
            throw new ExtensionConfigurationException("Error invoking " + methodName, e);
        }
    }

    private String resolveLoggerName(final AnnotatedElement element,
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
        // fallback: nome da classe de teste
        return testClass.getName();
    }

    private void setField(final Field field, final Object instance, final Logger logger) {
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
