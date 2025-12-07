package org.usefultoys.slf4jtestmock;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject a {@link org.slf44j.impl.MockLogger} into a field or parameter.
 * <p>
 * When applied to a field, the field must be of type {@link org.slf4j.Logger}.
 * The {@link org.slf4j.impl.MockLogger} instance will be automatically created and
 * configured based on the annotation's attributes.
 * <p>
 * Example usage:
 * <pre>{@code
 * public class MyTest {
 *     @Slf4jMock(value = "my.custom.logger")
 *     Logger customLogger;
 *
 *     @Slf4jMock(type = MyService.class, infoEnabled = false)
 *     Logger serviceLogger;
 *
 *     @Test
 *     void testSomething(@Slf4jMock Logger methodLogger) {
 *         customLogger.info("This goes to the custom logger");
 *         serviceLogger.info("This will not be logged by serviceLogger");
 *         methodLogger.debug("This is a method-scoped logger");
 *     }
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Slf4jMock {

    /**
     * Explicit name for the logger (e.g., "security.audit").
     * This takes precedence over {@link #type()}.
     * If both {@link #value()} and {@link #type()} are empty, a default name will be used.
     *
     * @return The explicit name for the logger.
     */
    String value() default "";

    /**
     * The class whose canonical name will be used as the logger name (e.g., MyService.class -> "com.example.MyService").
     * This is used if {@link #value()} is empty.
     *
     * @return The class to derive the logger name from.
     */
    Class<?> type() default Void.class;

    /**
     * Enables or disables the logger as a whole. If set to {@code false}, no events will be recorded by this logger,
     * regardless of individual level settings.
     *
     * @return {@code true} if the logger is enabled, {@code false} otherwise.
     */
    boolean enabled() default true;

    /**
     * Controls whether TRACE level logging is enabled for this logger.
     *
     * @return {@code true} if TRACE level is enabled, {@code false} otherwise.
     */
    boolean traceEnabled() default true;

    /**
     * Controls whether DEBUG level logging is enabled for this logger.
     *
     * @return {@code true} if DEBUG level is enabled, {@code false} otherwise.
     */
    boolean debugEnabled() default true;

    /**
     * Controls whether INFO level logging is enabled for this logger.
     *
     * @return {@code true} if INFO level is enabled, {@code false} otherwise.
     */
    boolean infoEnabled()  default true;

    /**
     * Controls whether WARN level logging is enabled for this logger.
     *
     * @return {@code true} if WARN level is enabled, {@code false} otherwise.
     */
    boolean warnEnabled()  default true;

    /**
     * Controls whether ERROR level logging is enabled for this logger.
     *
     * @return {@code true} if ERROR level is enabled, {@code false} otherwise.
     */
    boolean errorEnabled() default true;
}
