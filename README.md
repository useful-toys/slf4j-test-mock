# SLF4J Test Mock

A comprehensive mock implementation of the SLF4J logging framework designed specifically for unit testing. This library provides complete mock implementations of all SLF4J components, allowing developers to capture and inspect log events during test execution.

**Compatible with both SLF4J 1.7.x and 2.0.x** - Works seamlessly with whichever version is on your classpath.

## Features

- **SLF4J 1.7 & 2.0 Compatible**: Works seamlessly with both SLF4J 1.7.x and 2.0.x
- **Complete SLF4J Implementation**: Full mock implementations of Logger, LoggerFactory, MDC, and Marker components
- **Event Capturing**: All log events are captured in memory for test verification
- **Assertion Utilities**: Rich API for asserting log events with descriptive error messages  
- **Level Control**: Fine-grained control over which log levels are enabled during tests
- **Marker Support**: Full support for SLF4J markers in logging and assertions
- **MDC Support**: Mock implementation of Mapped Diagnostic Context (MDC) including SLF4J 2.0 Deque methods
- **Parallel Test Isolation (JUnit 5)**: When used with `@WithMockLogger` and/or `@WithMockLoggerDebug`, logger instances are isolated per test execution to avoid interference when two parallel tests use the same logger name
- **Java 8+ Compatible**: Works with Java 8 and higher versions

## SLF4J Compatibility

This library supports both major versions of the SLF4J API:

- **SLF4J 1.7.x** (tested with 1.7.36) - The traditional version
- **SLF4J 2.0.x** (tested with 2.0.16) - The modern version with enhanced features

### Which Version Should I Use?

The library automatically works with whichever SLF4J version is on your classpath. Simply add this mock implementation to your test dependencies, and it will integrate seamlessly with your existing SLF4J setup.

**For projects using SLF4J 1.7.x:**
- Uses traditional Static Binder mechanism
- Full compatibility with SLF4J 1.7 API
- Java 8+ compatible

**For projects using SLF4J 2.0.x:**
- Uses modern Service Provider Interface
- Supports new SLF4J 2.0 features including MDC Deque methods
- Java 8+ compatible

For more details on the multi-version support strategy, see [TDR-0005: Multiple SLF4J Version Support](doc/TDR-0005-multiple-slf4j-version-support.md).

## Maven Dependency

```xml
<dependency>
    <groupId>org.usefultoys</groupId>
    <artifactId>slf4j-test-mock</artifactId>
    <version>0.0.6</version>
    <scope>test</scope>
</dependency>
```

## Usage

This library provides multiple ways to test logging in your application. Choose the approach that best fits your testing style.

### Recommended: JUnit 5 Integration with @WithMockLogger

The easiest and most convenient way to use this library is with the `@WithMockLogger` annotation, which automatically registers the JUnit 5 extension and manages logger lifecycle:

```java
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent.Level;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;

import static org.usefultoys.slf4jtestmock.AssertLogger.*;

@WithMockLogger
class MyServiceTest {
    
    @Slf4jMock
    Logger logger;
    
    @Test
    void shouldLogUserLogin() {
        // Act - your code logs via the injected logger
        logger.info("User {} logged in from {}", "alice", "192.168.1.1");
        logger.warn("Invalid password attempt for user {}", "bob");
        
        // Assert - verify the logged events using message parts
        // You don't need to match the exact message - just key fragments
        // This is useful when messages contain variable values
        assertEventCount(logger, 2);
        assertEvent(logger, 0, Level.INFO, "alice", "logged in", "192.168.1.1");
        assertEvent(logger, 1, Level.WARN, "Invalid password", "bob");
    }
}
```

**Benefits:**
- The mocked Logger is automatically and transparently injected by the extension
- Events are automatically cleared before each test
- No manual setup or teardown code required
- Logger is ready to use immediately

**Configuration Options:**

You can declare multiple loggers in the same test class using different `@Slf4jMock` annotations (though typically one logger per test is sufficient):

```java
@WithMockLogger
class ConfigurationExampleTest {
    
    // Multiple loggers in the same test (less common, but supported)
    @Slf4jMock(type = MyService.class)
    Logger serviceLogger;
    
    @Slf4jMock("security.audit")
    Logger auditLogger;
    
    // Disable specific levels
    @Slf4jMock(debugEnabled = false, traceEnabled = false)
    Logger productionLogger;
    
    @Test
    void testWithConfiguration() {
        productionLogger.debug("This won't be captured");
        productionLogger.info("This will be captured");
        
        assertEventCount(productionLogger, 1);
    }
}
```

### Debugging Failed Tests

To automatically print logged events when a test fails, add the `@WithMockLoggerDebug` annotation:

```java
@WithMockLogger
@WithMockLoggerDebug
class DebugExampleTest {
    @Slf4jMock
    Logger logger;
    
    @Test
    void testSomething() {
        logger.info("This will be printed to stderr if the test fails");
        // ...
    }
}
```

### Resetting Additional Loggers

Sometimes your code under test uses internal loggers that are not injected into the test class. You can ensure these loggers are reset (cleared) before and after each test using the `@WithMockLogger` annotation:

```java
@WithMockLogger(reset = {"com.example.InternalLogger", "org.thirdparty.LibLogger"})
class MyTest {
    // ...
}
```

This is particularly useful when testing static methods or singletons that use their own loggers.

### Alternative: Manual Logger Management

If you prefer more control or aren't using JUnit 5, you can manage loggers manually:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent.Level;

import static org.usefultoys.slf4jtestmock.AssertLogger.*;

class ManualTest {
    
    @BeforeEach
    void setUp() {
        MockLogger mockLogger = (MockLogger) LoggerFactory.getLogger("test");
        mockLogger.clearEvents();
    }
    
    @Test
    void testLogging() {
        Logger logger = LoggerFactory.getLogger("test");
        
        logger.info("User {} logged in", "john");
        logger.warn("Low disk space: {} GB", 2.5);
        
        assertEventCount(logger, 2);
        assertEvent(logger, 0, Level.INFO, "john", "logged in");
        assertEvent(logger, 1, Level.WARN, "Low disk space");
    }
}
```

### AssertLogger API

The `AssertLogger` utility class provides comprehensive assertion methods for verifying log events:

**Index-Based Event Assertions:**
- `assertEvent(Logger, int, String...)` - Assert message contains one or more text parts
- `assertEvent(Logger, int, Level, String...)` - Assert level and message parts
- `assertEvent(Logger, int, Marker, String...)` - Assert marker and message parts
- `assertEvent(Logger, int, Level, Marker, String...)` - Assert level, marker, and message parts

**Existence-Based Event Assertions:**
- `assertHasEvent(Logger, String...)` - Assert any event contains all message parts
- `assertHasEvent(Logger, Level, String...)` - Assert any event has level and all message parts
- `assertHasEvent(Logger, Marker, String...)` - Assert any event has marker and all message parts
- `assertHasEvent(Logger, Level, Marker, String...)` - Assert any event has level, marker, and all message parts

**Throwable Assertions:**
- `assertEventWithThrowable(Logger, int, Class)` - Assert event has specific throwable type
- `assertEventWithThrowable(Logger, int, Class, String...)` - Assert event has throwable type and its message contains all parts
- `assertEventHasThrowable(Logger, int)` - Assert event has any throwable
- `assertHasEventWithThrowable(Logger, Class)` - Assert any event has specific throwable type
- `assertHasEventWithThrowable(Logger, Class, String...)` - Assert any event has throwable type and its message contains all parts
- `assertHasEventWithThrowable(Logger)` - Assert any event has any throwable

**Argument Assertions:**
- `assertEventWithArgument(Logger, int, int, Object)` - Assert event has expected argument at the specified argument index
- `assertEventWithArguments(Logger, int, Object...)` - Assert event has exactly the expected arguments (same count and order)
- `assertEventHasArgument(Logger, int, Object)` - Assert event has the expected argument in any argument position
- `assertHasEventWithArgument(Logger, Object)` - Assert any event has the expected argument in any argument position
- `assertHasEventHasArgument(Logger, Object)` - Alias for `assertHasEventWithArgument(Logger, Object)`
- `assertEventNotHasArgument(Logger, int, Object)` - Assert event does not contain the unexpected argument
- `assertEventNotWithArgument(Logger, int, int, Object)` - Assert event does not have the unexpected argument at the specified argument index
- `assertEventNotWithArguments(Logger, int, Object...)` - Assert event does not have exactly the unexpected arguments

**Argument Count Assertions:**
- `assertEventHasArgumentCount(Logger, int, int)` - Assert event has exactly N arguments
- `assertHasEventWithArgumentCount(Logger, int)` - Assert any event has exactly N arguments
- `assertNoEventWithArgumentCount(Logger, int)` - Assert no event has exactly N arguments

**Event Counting Assertions:**
- `assertEventCount(Logger, int)` - Assert total number of events
- `assertNoEvents(Logger)` - Assert no events were logged
- `assertEventCountByLevel(Logger, Level, int)` - Assert count of events by level
- `assertEventCountByMarker(Logger, Marker, int)` - Assert count of events by marker
- `assertEventCountByMessage(Logger, String, int)` - Assert count of events containing a message part

**Event Sequence Assertions:**
- `assertEventSequence(Logger, Level...)` - Assert exact sequence of log levels
- `assertEventSequence(Logger, Marker...)` - Assert exact sequence of markers
- `assertEventSequence(Logger, String...)` - Assert exact sequence of message parts

## Common Testing Scenarios

### Testing Messages with Message Parts

The recommended approach is to verify log messages using **message parts** instead of matching the exact message. This makes tests more resilient to message format changes and handles variable content gracefully.

```java
@WithMockLogger
class MessagePartsTest {
    
    @Slf4jMock
    Logger logger;
    
    @Test
    void shouldVerifyMessageParts() {
        // Log message with variable values
        String userId = "alice";
        String ipAddress = "192.168.1.100";
        long timestamp = System.currentTimeMillis();
        
        logger.info("User {} logged in from {} at {}", userId, ipAddress, timestamp);
        
        // Assert using message parts - no need to match the exact message or timestamp
        // Just verify the key parts you care about are present
        assertEvent(logger, 0, Level.INFO, "User", "alice", "logged in", "192.168.1.100");
        
        // This is better than trying to match the exact formatted message:
        // ❌ assertEquals("User alice logged in from 192.168.1.100 at 1234567890", event.getFormattedMessage());
        // ✅ The message parts approach works regardless of exact formatting
    }
    
    @Test
    void shouldHandleVariableMessages() {
        // Message format may vary based on conditions
        double diskSpace = 15.7;
        logger.warn("Low disk space: {} GB remaining", diskSpace);
        
        // Verify key parts without worrying about exact number formatting
        assertEvent(logger, 0, Level.WARN, "Low disk space", "GB");
        // The exact value "15.7" might be formatted differently, but key words are stable
    }
}
```

**Advantages of using message parts:**
- **Resilient to formatting changes**: Tests don't break when message format changes slightly
- **Handles variable values**: No need to know exact timestamps, IDs, or calculated values
- **Focus on what matters**: Verify the important keywords and data, ignore formatting details
- **More readable**: Clear intent about what you're verifying

### Testing with Markers

```java
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@WithMockLogger
class SecurityTest {
    
    @Slf4jMock("security.audit")
    Logger logger;
    
    @Test
    void shouldLogSecurityEvents() {
        Marker securityMarker = MarkerFactory.getMarker("SECURITY");
        
        logger.warn(securityMarker, "Unauthorized access attempt from IP: {}", "192.168.1.100");
        
        assertEvent(logger, 0, Level.WARN, securityMarker, "Unauthorized", "192.168.1.100");
    }
}
```

### Testing Exception Logging

```java
@WithMockLogger
class ExceptionHandlingTest {
    
    @Slf4jMock
    Logger logger;
    
    @Test
    void shouldLogExceptions() {
        Exception ex = new RuntimeException("Database connection failed due to network timeout");
        
        logger.error("Operation failed while processing user data", ex);
        
        // Verify message parts - no need to match the exact error message
        assertEvent(logger, 0, Level.ERROR, "Operation failed", "processing");
        
        // Verify exception type and exception message parts
        assertEventWithThrowable(logger, 0, RuntimeException.class, "Database", "connection", "network");
    }
}
```

### Testing Multiple Events with Index

When your code logs multiple messages with different levels and markers, use the event index to verify each one individually:

```java
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@WithMockLogger
class MultipleEventsTest {
    
    @Slf4jMock
    Logger logger;
    
    @Test
    void shouldLogMultipleEventsAndVerifyEachByIndex() {
        // Arrange - create markers
        Marker performanceMarker = MarkerFactory.getMarker("PERFORMANCE");
        Marker securityMarker = MarkerFactory.getMarker("SECURITY");
        
        // Act - log multiple events with different levels and markers
        logger.info("Application started successfully");
        logger.debug(performanceMarker, "Database query took {} ms", 145);
        logger.warn(securityMarker, "Failed login attempt for user {}", "admin");
        logger.info(performanceMarker, "Cache hit ratio: {}%", 87.5);
        logger.error("Critical system error detected");
        
        // Assert - verify total count first
        assertEventCount(logger, 5);
        
        // Then verify each event individually by index
        assertEvent(logger, 0, Level.INFO, "Application started");
        assertEvent(logger, 1, Level.DEBUG, performanceMarker, "Database query", "145 ms");
        assertEvent(logger, 2, Level.WARN, securityMarker, "Failed login", "admin");
        assertEvent(logger, 3, Level.INFO, performanceMarker, "Cache hit", "87.5");
        assertEvent(logger, 4, Level.ERROR, "Critical system error");
    }
}
```

### Controlling Log Levels

```java
@WithMockLogger
class LogLevelTest {
    
    @Slf4jMock(debugEnabled = false)
    Logger logger;
    
    @Test
    void shouldNotCaptureDebugMessages() {
        logger.debug("This won't be captured");
        logger.info("This will be captured");
        
        assertEventCount(logger, 1);
        assertEvent(logger, 0, Level.INFO, "This will be captured");
    }
}
```

### Verifying Event Sequences

```java
@WithMockLogger
class WorkflowTest {
    
    @Slf4jMock
    Logger logger;
    
    @Test
    void shouldLogWorkflowSteps() {
        logger.info("Process starting");
        logger.debug("Step 1 completed");
        logger.warn("Warning occurred");
        logger.info("Process finished");
        
        // Verify exact sequence of levels
        assertEventSequence(logger, Level.INFO, Level.DEBUG, Level.WARN, Level.INFO);
        
        // Verify sequence of message parts
        assertEventSequence(logger, "starting", "Step 1", "Warning", "finished");
    }
}
```

### Counting Events

```java
@WithMockLogger
class EventCountTest {
    
    @Slf4jMock
    Logger logger;
    
    @Test
    void shouldCountEvents() {
        Marker securityMarker = MarkerFactory.getMarker("SECURITY");
        
        logger.info("Application started");
        logger.warn(securityMarker, "Authentication failed");
        logger.info("Processing request");
        logger.error("Critical error occurred");
        
        // Count total events
        assertEventCount(logger, 4);
        
        // Count by level
        assertEventCountByLevel(logger, Level.INFO, 2);
        assertEventCountByLevel(logger, Level.ERROR, 1);
        
        // Count by marker
        assertEventCountByMarker(logger, securityMarker, 1);
        
        // Count by message content
        assertEventCountByMessage(logger, "error", 1);
    }
}
```

### Using Existence-Based Assertions

When event order doesn't matter, use existence-based assertions:

```java
@WithMockLogger
class ExistenceAssertionTest {
    
    @Slf4jMock
    Logger logger;
    
    @Test
    void shouldContainExpectedEvents() {
        logger.info("User alice logged in from 127.0.0.1");
        logger.warn("Invalid password attempt");
        logger.error("Database connection failed");
        
        // Check if any event contains specific text parts (order doesn't matter)
        assertHasEvent(logger, "alice", "127.0.0.1");
        assertHasEvent(logger, Level.ERROR, "Database");
        assertHasEvent(logger, Level.WARN, "password");
    }
}
```

## Best Practices

### 1. Use JUnit 5 Extension for Convenience

```java
// Recommended - automatic logger management
@WithMockLogger
class MyTest {
    @Slf4jMock Logger logger;
    // No setup or cleanup needed!
}
```

### 2. Choose the Right Assertion Type

**Use index-based assertions** when order matters:
```java
assertEvent(logger, 0, Level.INFO, "Starting");
assertEvent(logger, 1, Level.INFO, "Completed");
```

**Use existence-based assertions** when order doesn't matter:
```java
assertHasEvent(logger, Level.ERROR, "Database", "failed");
```

**Use counting assertions** for volume verification:
```java
assertEventCount(logger, 5);
assertEventCountByLevel(logger, Level.ERROR, 0); // No errors expected
```

**Use sequence assertions** for workflow validation:
```java
assertEventSequence(logger, Level.INFO, Level.DEBUG, Level.WARN, Level.INFO);
```

### 3. Test Both Messages and Exceptions

```java
logger.error("Operation failed", new SQLException("Connection timeout"));

assertEvent(logger, 0, Level.ERROR, "Operation failed");
assertEventWithThrowable(logger, 0, SQLException.class, "Connection", "timeout");
```

## Thread Safety

This mock implementation is designed for single-threaded test environments. For parallel test execution, use unique logger names per test class or method.

## Requirements

- Java 8 or higher
- SLF4J API 1.7.x or 2.0.x
- JUnit 5 (for JUnit extension and assertion utilities)

## Documentation

For more detailed information about the implementation and advanced topics:

- **[Mock Logger Implementation Guide](doc/mock-logger-implementation.md)** - Internal architecture, data structures, and design decisions
- **[SLF4J API Integration Guide](doc/slf4j-api-integration.md)** - How the library integrates with SLF4J 1.7.x and 2.0.x
- **[TDR-0001: In-Memory Event Storage](doc/TDR-0001-in-memory-event-storage.md)** - Why ArrayList for event storage
- **[TDR-0002: Use of JUnit5 Assertions](doc/TDR-0002-use-of-junit5-assertions.md)** - Why JUnit Jupiter assertions
- **[TDR-0003: Focus on SLF4J Facade](doc/TDR-0003-focus-on-slf4j-facade.md)** - Design philosophy
- **[TDR-0004: JUnit Extension for Debug Logging](doc/TDR-0004-junit-extension-for-debug-logging.md)** - Debug output on test failures
- **[TDR-0005: Multiple SLF4J Version Support](doc/TDR-0005-multiple-slf4j-version-support.md)** - Dual version compatibility strategy
- **[BUILD-PROFILES.md](BUILD-PROFILES.md)** - Maven build profiles and how to build from source

## FAQ

### How do I know which SLF4J version I'm using?

Check your project's dependencies:

```bash
# Maven
mvn dependency:tree | findstr slf4j-api

# Gradle
gradle dependencies | grep slf4j-api
```

### Do I need special configuration for SLF4J 2.0?

No! The library automatically detects and works with both SLF4J 1.7.x and 2.0.x. Just add it to your test dependencies.

### Can I use this during SLF4J migration?

Yes! Since this library supports both versions, your tests will continue to work when migrating from SLF4J 1.7 to 2.0.

### How does the dual-version support work?

The library uses different integration mechanisms based on the SLF4J version on your classpath:
- **SLF4J 1.7.x**: Static Binder pattern
- **SLF4J 2.0.x**: Service Provider Interface

For technical details, see the [SLF4J API Integration Guide](doc/slf4j-api-integration.md).

## License

Licensed under the Apache License, Version 2.0. See the [LICENSE](../LICENSE) file for details.
