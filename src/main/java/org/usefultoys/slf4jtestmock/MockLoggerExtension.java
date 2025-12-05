package org.usefultoys.slf4jtestmock;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
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
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        Class<?> testClass = testInstance.getClass();
        Field[] fields = testClass.getDeclaredFields();
        for (Field field : fields) {
            if (isLoggerField(field)) {
                Logger logger = createAndConfigureLogger(field, testClass, field.getName());
                setField(field, testInstance, logger);
            }
        }
    }

    // --- Antes de cada teste, limpa e reconfigura os loggers já injetados ---
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        Class<?> testClass = testInstance.getClass();
        Field[] fields = testClass.getDeclaredFields();
        for (Field field : fields) {
            if (isLoggerField(field)) {
                field.setAccessible(true);
                Object value = field.get(testInstance);
                if (value instanceof Logger) {
                    Logger slf4jLogger = (Logger) value;
                    reinitializeLogger(field, testClass, slf4jLogger);
                }
            }
        }
    }

    // --- Suporte a injeção via parâmetro de método ---
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return Logger.class.equals(type) || MockLogger.class.equals(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Parameter parameter = parameterContext.getParameter();
        Class<?> testClass = extensionContext.getRequiredTestClass();
        Logger logger = createAndConfigureLogger(parameter, testClass, parameter.getName());

        if (MockLogger.class.equals(parameter.getType())) {
            return (MockLogger) logger;
        }
        return logger;
    }

    // --- Helpers principais ---

    private boolean isLoggerField(Field field) {
        Class<?> type = field.getType();
        return Logger.class.equals(type) || MockLogger.class.equals(type);
    }

    private Logger createAndConfigureLogger(AnnotatedElement element,
                                            Class<?> testClass,
                                            String fallbackName) {

        String loggerName = resolveLoggerName(element, testClass, fallbackName);
        Logger slf4jLogger = LoggerFactory.getLogger(loggerName);

        if (!(slf4jLogger instanceof MockLogger)) {
            throw new ExtensionConfigurationException(
                    "Expected LoggerFactory.getLogger(\"" + loggerName +
                    "\") to return a MockLogger, but got: " + slf4jLogger.getClass()
            );
        }

        MockLogger mock = (MockLogger) slf4jLogger;
        applyConfig(mock, element);

        return slf4jLogger;
    }

    private void reinitializeLogger(AnnotatedElement element,
                                    Class<?> testClass,
                                    Logger slf4jLogger) {

        if (!(slf4jLogger instanceof MockLogger)) {
            throw new ExtensionConfigurationException(
                    "Expected a MockLogger but got: " + slf4jLogger.getClass()
            );
        }

        MockLogger mock = (MockLogger) slf4jLogger;
        applyConfig(mock, element);
    }

    private void applyConfig(MockLogger mock, AnnotatedElement element) {
        Slf4jMock cfg = element.getAnnotation(Slf4jMock.class);

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

    private void enableAllLevels(MockLogger mock) {
        setIfExists(mock, "setTraceEnabled", true);
        setIfExists(mock, "setDebugEnabled", true);
        setIfExists(mock, "setInfoEnabled",  true);
        setIfExists(mock, "setWarnEnabled",  true);
        setIfExists(mock, "setErrorEnabled", true);
    }

    // Usa reflexão para não quebrar se alguma versão não tiver certos setters
    private void setIfExists(MockLogger mock, String methodName, boolean value) {
        try {
            Method m = mock.getClass().getMethod(methodName, boolean.class);
            m.invoke(mock, Boolean.valueOf(value));
        } catch (NoSuchMethodException e) {
            // Versão da lib sem esse método – ignora
        } catch (Exception e) {
            throw new ExtensionConfigurationException("Error invoking " + methodName, e);
        }
    }

    private String resolveLoggerName(AnnotatedElement element,
                                     Class<?> testClass,
                                     String fallbackName) {

        Slf4jMock cfg = element.getAnnotation(Slf4jMock.class);
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

    private void setField(Field field, Object instance, Logger logger) {
        try {
            field.setAccessible(true);
            if (field.getType().equals(MockLogger.class) && logger instanceof MockLogger) {
                field.set(instance, (MockLogger) logger);
            } else {
                field.set(instance, logger);
            }
        } catch (IllegalAccessException e) {
            throw new ExtensionConfigurationException("Could not set logger field: " + field, e);
        }
    }
}
