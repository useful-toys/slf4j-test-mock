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
- **Thread-Safe**: Safe for use in single-threaded test environments
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

### Build from Source

If you're building this library from source and need to test with a specific SLF4J version:

```bash
# Build with SLF4J 1.7.x (default)
mvn clean install

# Build with SLF4J 2.0.x
mvn clean install -P slf4j-2.0
```

For more details on the multi-version support strategy, see [ADR-0004: Multiple SLF4J Version Support](ADR-0004-multiple-slf4j-version-support.md) and [BUILD-PROFILES.md](BUILD-PROFILES.md).

## Maven Dependency

```xml
<dependency>
    <groupId>org.usefultoys</groupId>
    <artifactId>slf4j-test-mock</artifactId>
    <version>0.0.2</version>
    <scope>test</scope>
</dependency>
```

## Quick Start

### Recommended Usage with AssertLogger

The `AssertLogger` class provides convenient static methods for asserting log events - this is the recommended way to use this library:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent.Level;
import static org.slf4j.impl.AssertLogger.*;

class MyTest {
    private Logger logger;
    private MockLogger mockLogger;
    
    @BeforeEach
    void setUp() {
        logger = LoggerFactory.getLogger("test");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
        mockLogger.setEnabled(true);
    }
    
    @Test
    void testWithAssertLogger() {
        logger.info("Processing user: {}", "alice");
        logger.error("Failed to process user: {}", "bob");
        
        // Use AssertLogger for cleaner assertions
        assertEvent(logger, 0, Level.INFO, "Processing", "alice");
        assertEvent(logger, 1, Level.ERROR, "Failed", "bob");
    }
}
```

### Alternative: Direct MockLogger Usage

You can also work directly with `MockLogger` for more detailed inspection:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent.Level;

@Test
void testLogging() {
    // Get logger - automatically returns MockLogger during tests
    Logger logger = LoggerFactory.getLogger("test.logger");
    MockLogger mockLogger = (MockLogger) logger;
    mockLogger.clearEvents();
    mockLogger.setEnabled(true);
    
    // Log some messages
    logger.info("User {} logged in", "john");
    logger.warn("Low disk space: {} GB remaining", 2.5);
    
    // Assert log events
    assertEquals(2, mockLogger.getEventCount());
    assertEquals("User john logged in", mockLogger.getEvent(0).getFormattedMessage());
    assertEquals(Level.INFO, mockLogger.getEvent(0).getLevel());
}
```

## Core Classes

### MockLogger

The main mock logger implementation that captures log events in memory.

**Key Methods:**
- `getEventCount()` - Returns the number of captured events
- `getEvent(int index)` - Returns a specific event by index
- `getLoggerEvents()` - Returns all events as an unmodifiable list
- `clearEvents()` - Clears all captured events
- `setEnabled(boolean)` - Enables/disables all log levels
- `setInfoEnabled(boolean)` - Controls specific log levels
- `toText()` - Returns all messages as a formatted string

### MockLoggerEvent

Immutable representation of a single log event.

**Key Properties:**
- `getLevel()` - The log level (TRACE, DEBUG, INFO, WARN, ERROR)
- `getFormattedMessage()` - The formatted message with placeholders resolved
- `getMessage()` - The original message template
- `getArguments()` - The message arguments
- `getMarker()` - The associated marker (if any)
- `getThrowable()` - The associated throwable (if any)
- `getLoggerName()` - The name of the logger that created this event
- `getMdc()` - The MDC context at the time of logging

### AssertLogger

Utility class providing static assertion methods for testing log events.

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

## Advanced Usage

### Controlling Log Levels

```java
@Test
void testLogLevelControl() {
    Logger logger = LoggerFactory.getLogger("test");
    MockLogger mockLogger = (MockLogger) logger;
    mockLogger.clearEvents();
    mockLogger.setEnabled(true);
    
    // Disable debug logging
    mockLogger.setDebugEnabled(false);
    
    logger.debug("This won't be captured");
    logger.info("This will be captured");
    
    assertEquals(1, mockLogger.getEventCount());
    assertEvent(logger, 0, Level.INFO, "This will be captured");
}
```

### Working with Markers

```java
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Test
void testWithMarkers() {
    Logger logger = LoggerFactory.getLogger("test");
    MockLogger mockLogger = (MockLogger) logger;
    mockLogger.clearEvents();
    mockLogger.setEnabled(true);
    Marker securityMarker = MarkerFactory.getMarker("SECURITY");
    
    logger.warn(securityMarker, "Unauthorized access attempt from IP: {}", "192.168.1.100");
    
    assertEvent(logger, 0, Level.WARN, securityMarker, "Unauthorized", "192.168.1.100");
}
```

### Testing Exception Logging

```java
@Test
void testExceptionLogging() {
    Logger logger = LoggerFactory.getLogger("test");
    MockLogger mockLogger = (MockLogger) logger;
    mockLogger.clearEvents();
    mockLogger.setEnabled(true);
    Exception ex = new RuntimeException("Test exception with details");
    
    logger.error("Operation failed", ex);
    
    assertEvent(logger, 0, Level.ERROR, "Operation failed");
    assertEventWithThrowable(logger, 0, RuntimeException.class, "Test", "details");
}
```

### Clearing Events Between Tests

```java
@BeforeEach
void setUp() {
    MockLogger mockLogger = (MockLogger) LoggerFactory.getLogger("test");
    mockLogger.clearEvents();
}
```

### Inspecting All Log Output

```java
@Test
void testCompleteLogOutput() {
    Logger logger = LoggerFactory.getLogger("test");
    MockLogger mockLogger = (MockLogger) logger;
    mockLogger.clearEvents();
    mockLogger.setEnabled(true);
    
    logger.info("Starting process");
    logger.debug("Processing step 1");
    logger.info("Process completed");
    
    // Get all messages as formatted text
    String allMessages = mockLogger.toText();
    assertTrue(allMessages.contains("Starting process"));
    
    // Or inspect individual events
    List<MockLoggerEvent> events = mockLogger.getLoggerEvents();
    assertEquals(3, events.size());
}
```

### Using Existence-Based Assertions

```java
@Test
void testExistenceAssertions() {
    Logger logger = LoggerFactory.getLogger("test");
    MockLogger mockLogger = (MockLogger) logger;
    mockLogger.clearEvents();
    mockLogger.setEnabled(true);
    
    logger.info("User john logged in from 127.0.0.1");
    logger.warn("Invalid password attempt");
    logger.error("Database connection failed");
    
    // Check if any event contains specific text parts (order doesn't matter)
    assertHasEvent(logger, "john", "127.0.0.1");
    assertHasEvent(logger, Level.ERROR, "Database");
    assertHasEvent(logger, Level.WARN, "password");
}
```

### Testing Exception Handling

```java
@Test
void testExceptionAssertions() {
    Logger logger = LoggerFactory.getLogger("test");
    MockLogger mockLogger = (MockLogger) logger;
    mockLogger.clearEvents();
    mockLogger.setEnabled(true);
    
    logger.error("Database error", new SQLException("Connection timeout"));
    logger.warn("Network issue", new IOException("Connection refused"));
    
    // Test specific throwable types and message parts
    assertEventWithThrowable(logger, 0, SQLException.class, "Connection", "timeout");
    assertEventWithThrowable(logger, 1, IOException.class, "refused");
    
    // Test existence of any throwable with specific properties
    assertHasEventWithThrowable(logger, SQLException.class, "timeout");
    assertHasEventWithThrowable(logger, IOException.class);
}
```

### Counting Events

```java
@Test
void testEventCounting() {
    Logger logger = LoggerFactory.getLogger("test");
    MockLogger mockLogger = (MockLogger) logger;
    mockLogger.clearEvents();
    mockLogger.setEnabled(true);
    Marker securityMarker = MarkerFactory.getMarker("SECURITY");
    
    logger.info("Application started");
    logger.warn(securityMarker, "Authentication failed");
    logger.info("Processing request");
    logger.error("Critical error occurred");
    
    // Count total events
    assertEventCount(logger, 4);
    
    // Count by level
    assertEventCountByLevel(logger, Level.INFO, 2);
    
    // Count by marker
    assertEventCountByMarker(logger, securityMarker, 1);
    
    // Count by message content
    assertEventCountByMessage(logger, "error", 1); // Case sensitive
}
```

### Validating Event Sequences

```java
@Test
void testEventSequences() {
    Logger logger = LoggerFactory.getLogger("test");
    MockLogger mockLogger = (MockLogger) logger;
    mockLogger.clearEvents();
    mockLogger.setEnabled(true);
    Marker startMarker = MarkerFactory.getMarker("START");
    Marker endMarker = MarkerFactory.getMarker("END");
    
    logger.info(startMarker, "Process starting");
    logger.debug("Step 1 completed");
    logger.warn("Warning occurred");
    logger.info(endMarker, "Process finished");
    
    // Verify exact sequence of levels
    assertEventSequence(logger, Level.INFO, Level.DEBUG, Level.WARN, Level.INFO);
    
    // Verify sequence of markers (null for events without markers)
    assertEventSequence(logger, startMarker, null, null, endMarker);
    
    // Verify sequence of message parts
    assertEventSequence(logger, "starting", "Step 1", "Warning", "finished");
}
```

## Integration with Test Frameworks

### JUnit 5

```java
@ExtendWith(MockLoggerExtension.class)
class MyServiceTest {
    
    private MockLogger logger;
    
    @BeforeEach
    void setUp() {
        logger = (MockLogger) LoggerFactory.getLogger(MyService.class);
        logger.clearEvents(); // Start each test clean
    }
    
    @Test
    void testServiceOperation() {
        MyService service = new MyService();
        service.performOperation();
        
        assertEvent(logger, 0, Level.INFO, "Operation started");
        assertEvent(logger, 1, Level.INFO, "Operation completed");
    }
}
```

### Testing Configuration

Since this is a test-scoped dependency, ensure your test classpath includes the mock implementation:

1. Add the dependency to your `pom.xml` with `<scope>test</scope>`
2. SLF4J will automatically discover and use the mock implementation during tests
3. Your production code will use the real logging implementation

## Best Practices

### 1. Use AssertLogger for Cleaner Tests

```java
// Good - Uses AssertLogger for cleaner code
assertEvent(logger, 0, Level.INFO, "User", "logged in");

// Less preferred - Manual casting and assertion
MockLogger mockLogger = (MockLogger) logger;
MockLoggerEvent event = mockLogger.getEvent(0);
assertEquals(Level.INFO, event.getLevel());
assertTrue(event.getFormattedMessage().contains("User"));
assertTrue(event.getFormattedMessage().contains("logged in"));
```

### 2. Clear Events Between Test Methods

```java
@BeforeEach
void setUp() {
    MockLogger mockLogger = (MockLogger) LoggerFactory.getLogger("test");
    mockLogger.clearEvents();
}
```

### 3. Use Descriptive Assertion Messages

The AssertLogger methods include descriptive error messages automatically:

```java
// This will provide clear error messages if assertions fail
assertEvent(logger, 0, Level.ERROR, "Expected", "error", "message");
```

### 4. Test Different Log Levels

```java
@Test
void testDifferentLogLevels() {
    Logger logger = LoggerFactory.getLogger("test");
    MockLogger mockLogger = (MockLogger) logger;
    mockLogger.clearEvents();
    mockLogger.setEnabled(true);
    
    logger.trace("Trace message");
    logger.debug("Debug message");
    
    assertEvent(logger, 0, Level.TRACE, "Trace");
    assertEvent(logger, 1, Level.DEBUG, "Debug");
}
```

### 5. Choose the Right Assertion Type

**Use index-based assertions** when order matters:
```java
// When testing specific sequence of events
assertEvent(logger, 0, Level.INFO, "Starting");
assertEvent(logger, 1, Level.INFO, "Completed");
```

**Use existence-based assertions** when order doesn't matter:
```java
// When testing that events occurred, regardless of order
assertHasEvent(logger, Level.ERROR, "Database", "failed");
assertHasEvent(logger, "authentication");
```

**Use counting assertions** for volume verification:
```java
// When testing logging frequency or filtering
assertEventCount(logger, 5);
assertEventCountByLevel(logger, Level.ERROR, 0); // No errors expected
```

**Use sequence assertions** for workflow validation:
```java
// When testing state machine transitions or process flows
assertEventSequence(logger, Level.INFO, Level.DEBUG, Level.WARN, Level.INFO);
```

### 6. Test Exception Logging Properly

```java
@Test
void testExceptionHandling() {
    // Test both the message and the exception
    logger.error("Operation failed", new SQLException("Connection timeout"));
    
    assertEvent(logger, 0, Level.ERROR, "Operation failed");
    assertEventWithThrowable(logger, 0, SQLException.class, "Connection", "timeout");
}
```

## Thread Safety

This mock implementation is designed for single-threaded test environments. If you need to test multi-threaded logging scenarios, ensure proper synchronization in your test code.
You may run test in parallel as long as each test uses its own logger name.
For example, use unique logger names per test class or method.

## Requirements

- Java 8 or higher
- SLF4J API 1.7.x or 2.0.x (tested with 1.7.36 and 2.0.16)
- JUnit 5 (for assertion utilities)
- Lombok (build-time dependency for code generation)

## Technical Implementation

### SLF4J Version Support

This library uses different service provider mechanisms depending on the SLF4J version:

**SLF4J 1.7.x:**
- Uses Static Binder pattern (`StaticLoggerBinder`, `StaticMarkerBinder`, `StaticMDCBinder`)
- Compatible with all SLF4J 1.7.x releases
- Implements core MDC functionality

**SLF4J 2.0.x:**
- Uses Service Provider Interface (`MockServiceProvider` implementing `SLF4JServiceProvider`)
- Registered via `META-INF/services/org.slf4j.spi.SLF4JServiceProvider`
- Supports enhanced MDC features including Deque methods (`pushByKey`, `popByKey`, `getCopyOfDequeByKey`, `clearDequeByKey`)

The correct implementation is automatically selected at runtime based on your SLF4J classpath. No configuration needed!

For detailed information about the multi-version support strategy, see:
- [ADR-0004: Multiple SLF4J Version Support](ADR-0004-multiple-slf4j-version-support.md)
- [BUILD-PROFILES.md](BUILD-PROFILES.md) - For building from source

## FAQ

### How do I know which SLF4J version I'm using?

Check your project's dependencies. If you see `slf4j-api` version 1.7.x, you're using SLF4J 1.7. If it's 2.0.x or higher, you're using SLF4J 2.0.

```bash
# Maven
mvn dependency:tree | findstr slf4j-api

# Gradle
gradle dependencies | grep slf4j-api
```

### Do I need to do anything special for SLF4J 2.0?

No! The library automatically detects and works with SLF4J 2.0. Just add it to your test dependencies as usual.

### Can I use this library to migrate from SLF4J 1.7 to 2.0?

Yes! Since this library supports both versions, your tests will continue to work during the migration. This makes it safe to upgrade your SLF4J version without changing your test code.

### What are the differences between SLF4J 1.7 and 2.0 in this library?

From a user perspective, there are minimal differences:
- Both versions support all core logging and assertion features
- SLF4J 2.0 includes additional MDC Deque methods (`pushByKey`, `popByKey`, etc.)
- The service provider mechanism differs internally, but this is transparent to users

### Does this work with SLF4J 1.8 or other versions?

SLF4J jumped from 1.7.x directly to 2.0.x. There is no 1.8 version. This library is tested with:
- Latest SLF4J 1.7.x (1.7.36)
- Latest SLF4J 2.0.x (2.0.16)

It should work with any 1.7.x or 2.0.x release.

## License

Licensed under the Apache License, Version 2.0. See the [LICENSE](../LICENSE) file for details.

## Contributing

This library follows structured guidelines for code style and development practices.

### AI Instructions

The project provides instructions for AI assistants to maintain consistency:

- **[AI-INSTRUCTIONS.md](AI-INSTRUCTIONS.md)** - Shared instructions for all AI assistants (Gemini, Copilot, etc.)
- **[.github/copilot-instructions.md](.github/copilot-instructions.md)** - GitHub Copilot-specific instructions (official location)
- **[GEMINI.md](GEMINI.md)** - Google Gemini-specific instructions

Key guidelines include:
- Use Java 8
- Always use Maven wrapper (`mvnw.cmd` on Windows, `./mvnw` on Unix)
- Prefer Lombok annotations to reduce boilerplate
- Maintain >95% test coverage with JUnit 5
- Use `@DisplayName` for descriptive test names
- Document all classes and members with Javadoc
