# Mock Logger Implementation Guide

This document explains the internal architecture of `slf4j-test-mock`, focusing on the core classes that capture and store log events in memory for test assertions.

## Overview

The mock logger implementation consists of three primary classes that work together to capture, store, and provide access to logged events:

1. **`MockLoggerFactory`** - Manages the creation and registry of logger instances
2. **`MockLogger`** - Implements the SLF4J Logger interface and captures log events
3. **`MockLoggerEvent`** - Represents a single captured log event with all its details

These classes form the **internal infrastructure** of the mock implementation. **They should not be manipulated directly in unit tests.** Instead, tests should use the assertion API provided by `AssertLogger` to inspect logged events.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        Test Code                            │
│  (Uses standard SLF4J API: LoggerFactory.getLogger())       │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                   MockLoggerFactory                         │
│  ┌──────────────────────────────────────────────────┐       │
│  │  Map<String, Logger> nameToLogger                │       │
│  │    - "com.example.Service" → MockLogger          │       │
│  │    - "com.example.Controller" → MockLogger       │       │
│  │    - "com.example.Repository" → MockLogger       │       │
│  └──────────────────────────────────────────────────┘       │
│                                                             │
│  + getLogger(name): Logger                                  │
│  + getLoggers(): Map<String, Logger>  [static]              │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                      MockLogger                             │
│  ┌──────────────────────────────────────────────────┐       │
│  │  List<MockLoggerEvent> loggerEvents              │       │
│  │    [0] → MockLoggerEvent (INFO)                  │       │
│  │    [1] → MockLoggerEvent (WARN)                  │       │
│  │    [2] → MockLoggerEvent (ERROR)                 │       │
│  └──────────────────────────────────────────────────┘       │
│                                                             │
│  + info(message), warn(message), error(message), ...        │
│  + getEventCount(): int                                     │
│  + getEvent(index): MockLoggerEvent                         │
│  + getLoggerEvents(): List<MockLoggerEvent>                 │
│  + clearEvents()                                            │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                   MockLoggerEvent                           │
│  - eventIndex: int                                          │
│  - loggerName: String                                       │
│  - level: Level (TRACE, DEBUG, INFO, WARN, ERROR)           │
│  - marker: Marker                                           │
│  - message: String (format)                                 │
│  - arguments: Object[]                                      │
│  - throwable: Throwable                                     │
│  - mdc: Map<String, String>                                 │
│                                                             │
│  + getFormattedMessage(): String                            │
└─────────────────────────────────────────────────────────────┘
                             ▲
                             │
                             │
┌────────────────────────────┴────────────────────────────────┐
│                     AssertLogger                            │
│              (Assertion API for Tests)                      │
│                                                             │
│  + assertEvent(logger, index, "message")                    │
│  + assertEvent(logger, index, Level.INFO, "message")        │
│  + assertEvent(logger, index, marker, "message")            │
│  + assertEventCount(logger, expectedCount)                  │
└─────────────────────────────────────────────────────────────┘
```

## MockLoggerFactory

**Purpose**: Central registry and factory for creating `MockLogger` instances.

### Internal State

The factory maintains a singleton pattern with an internal map:

```java
Map<String, Logger> nameToLogger = new HashMap<>();
```

This map stores all logger instances created during test execution, keyed by their logger name.

### Key Characteristics

*   **Singleton Pattern**: Single factory instance serves all logger requests
*   **Lazy Logger Creation**: Loggers are created on first request and cached
*   **Thread-Local Scope**: Not thread-safe, designed for single-threaded test execution

### Public API

**For SLF4J Integration** (called automatically):

```java
public Logger getLogger(String name)
```

*   Checks if a logger with the given name already exists
*   If exists, returns the cached instance
*   If not, creates a new `MockLogger`, stores it in the map, and returns it

**For Test Infrastructure** (called by JUnit extensions):

```java
public static Map<String, Logger> getLoggers()
```

*   Returns an unmodifiable view of all created loggers
*   Used by JUnit extensions to iterate over all loggers for cleanup or inspection
*   Example use case: Clearing all logger events between test methods

### Lifecycle

1. **Initialization**: Factory is instantiated once when SLF4J binds to the mock provider
2. **Runtime**: Each `LoggerFactory.getLogger(name)` call delegates to `MockLoggerFactory.getLogger(name)`
3. **Test Execution**: Loggers accumulate events throughout test execution
4. **Cleanup**: Between tests, extensions may call `clearEvents()` on all loggers

## MockLogger

**Purpose**: Implements the SLF4J `Logger` interface and captures all log events in memory.

### Internal State

Each `MockLogger` instance maintains:

```java
private final String name;                          // Logger name
private final List<MockLoggerEvent> loggerEvents;   // Captured events
private boolean traceEnabled = true;                // Level flags
private boolean debugEnabled = true;
private boolean infoEnabled = true;
private boolean warnEnabled = true;
private boolean errorEnabled = true;
private boolean stdoutEnabled = false;              // Print to stdout?
private boolean stderrEnabled = false;              // Print to stderr?
```

### Event Capture Mechanism

When a logging method is called (e.g., `logger.info("message")`):

1. **Level Check**: Verifies if the log level is enabled
   ```java
   if (!isLevelEnabled(level)) {
       return;  // Skip if disabled
   }
   ```

2. **Event Creation**: Creates a `MockLoggerEvent` with all details
   ```java
   final int index = loggerEvents.size();
   final MockLoggerEvent event = new MockLoggerEvent(
       index, name, level, mdc, marker, throwable, format, args
   );
   ```

3. **Storage**: Adds the event to the internal list
   ```java
   loggerEvents.add(event);
   ```

4. **Optional Printing**: If stdout/stderr printing is enabled, formats and prints the event
   ```java
   print(event);  // Prints to System.out or System.err
   ```

### Public API for Event Access

**Direct Access** (for test infrastructure):

```java
public int getEventCount()
```
*   Returns the total number of events captured
*   Example: `assertEquals(3, logger.getEventCount())`

```java
public MockLoggerEvent getEvent(int eventIndex)
```
*   Returns the event at the specified index (0-based)
*   Example: `MockLoggerEvent event = logger.getEvent(0)`

```java
public List<MockLoggerEvent> getLoggerEvents()
```
*   Returns an unmodifiable list of all events
*   Example: `List<MockLoggerEvent> events = logger.getLoggerEvents()`

**Utility Methods**:

```java
public void clearEvents()
```
*   Removes all captured events
*   Typically called by JUnit extensions between test methods

```java
public String toText()
```
*   Returns all formatted log messages as a single text string
*   Each message is on a separate line
*   Useful for debugging or verifying complete log output

**Level Configuration**:

```java
public void setEnabled(boolean enabled)
public void setTraceEnabled(boolean enabled)
public void setDebugEnabled(boolean enabled)
// ... etc
```
*   Controls which log levels are captured
*   Events for disabled levels are discarded, not stored

**Print Configuration**:

```java
public void setStdoutEnabled(boolean enabled)
public void setStderrEnabled(boolean enabled)
```
*   Controls whether events are printed to console
*   Useful for debugging test failures (see events as they are logged)

### Deprecated Assertion Methods

`MockLogger` contains deprecated `assertEvent()` methods:

```java
@Deprecated
public void assertEvent(int eventIndex, String messagePart)
```

**Do NOT use these methods.** They exist for backward compatibility only. Use `AssertLogger` static methods instead:

```java
// OLD (deprecated):
MockLogger logger = (MockLogger) LoggerFactory.getLogger("test");
logger.assertEvent(0, "message");

// NEW (recommended):
Logger logger = LoggerFactory.getLogger("test");
AssertLogger.assertEvent(logger, 0, "message");
```

## MockLoggerEvent

**Purpose**: Immutable data structure representing a single captured log event.

### Internal State

Each event stores:

```java
private final int eventIndex;           // Position in logger's event list
private final String loggerName;        // Name of the logger
private final Level level;              // TRACE, DEBUG, INFO, WARN, ERROR
private final Marker marker;            // Optional SLF4J marker
private final String message;           // Format string
private final Object[] arguments;       // Arguments for formatting
private final Throwable throwable;      // Optional exception
private final Map<String, String> mdc;  // MDC context (currently null)
```

### Key Features

**Automatic Throwable Extraction**:

If no throwable is explicitly passed but the last argument is a `Throwable`, it is automatically extracted:

```java
// These two calls produce the same event:
logger.error("Error occurred: {}", exception);
logger.error("Error occurred", exception);
```

The constructor handles this:

```java
if (throwable == null && arguments != null && arguments.length > 1) {
    final Object last = arguments[arguments.length - 1];
    if (last instanceof Throwable) {
        this.throwable = (Throwable) last;
        this.arguments = Arrays.copyOfRange(arguments, 0, arguments.length - 1);
    }
}
```

**Message Formatting**:

The `getFormattedMessage()` method uses SLF4J's `MessageFormatter` to substitute placeholders:

```java
public String getFormattedMessage() {
    return MessageFormatter.arrayFormat(message, arguments).getMessage();
}
```

Example:
```java
logger.info("User {} logged in from {}", "john", "192.168.1.1");
// message = "User {} logged in from {}"
// arguments = ["john", "192.168.1.1"]
// getFormattedMessage() = "User john logged in from 192.168.1.1"
```

### Public API

All fields are accessible via Lombok-generated getters:

```java
public int getEventIndex()
public String getLoggerName()
public Level getLevel()
public Marker getMarker()
public String getMessage()          // Raw format string
public Object[] getArguments()
public Throwable getThrowable()
public Map<String, String> getMdc()
public String getFormattedMessage() // Formatted with arguments
```

### Level Enum

The `Level` enum defines the five standard SLF4J levels:

```java
public enum Level {
    ERROR,  // Highest severity
    WARN,
    INFO,
    DEBUG,
    TRACE   // Lowest severity
}
```

## Usage in Tests: The Assertion API

**Critical Rule**: Do NOT manipulate `MockLogger`, `MockLoggerFactory`, or `MockLoggerEvent` directly in test code.

### Recommended Approach

Use the `AssertLogger` utility class, which provides a clean assertion API:

```java
import static org.usefultoys.slf4jtestmock.AssertLogger.*;

@Test
void testLogging() {
    // Arrange
    Logger logger = LoggerFactory.getLogger("test.service");
    
    // Act
    logger.info("User {} logged in", "john");
    logger.warn("Invalid attempt from {}", "192.168.1.100");
    
    // Assert
    assertEventCount(logger, 2);
    assertEvent(logger, 0, Level.INFO, "User", "logged in");
    assertEvent(logger, 1, Level.WARN, "Invalid attempt");
}
```

### Why Not Direct Manipulation?

**Bad Practice**:
```java
MockLogger mockLogger = (MockLogger) logger;
MockLoggerEvent event = mockLogger.getEvent(0);
assertEquals(Level.INFO, event.getLevel());
assertTrue(event.getFormattedMessage().contains("User"));
```

**Problems**:
*   Requires casting to `MockLogger` (breaks abstraction)
*   Verbose and repetitive
*   Harder to read and maintain
*   Couples tests to implementation details

**Good Practice**:
```java
assertEvent(logger, 0, Level.INFO, "User");
```

**Benefits**:
*   Works with `Logger` interface (no casting)
*   Concise and readable
*   Clear intent
*   Decouples tests from internal structure

## Event Storage and Lifecycle

### Event List Per Logger

Each `MockLogger` maintains its own independent event list:

```
MockLoggerFactory
  ├─ "com.example.ServiceA" → MockLogger
  │    └─ loggerEvents: [Event0, Event1, Event2]
  │
  ├─ "com.example.ServiceB" → MockLogger
  │    └─ loggerEvents: [Event0, Event1]
  │
  └─ "com.example.Controller" → MockLogger
       └─ loggerEvents: [Event0, Event1, Event2, Event3]
```

Events are **never shared** between loggers. Each logger's list is completely independent.

### Event Indexing

Events are indexed sequentially starting from 0:

```java
logger.info("First message");   // Event index 0
logger.warn("Second message");  // Event index 1
logger.error("Third message");  // Event index 2
```

The index is immutable and stored in the event:

```java
MockLoggerEvent event = logger.getEvent(1);
assertEquals(1, event.getEventIndex());
```

### Event Clearing

Events can be cleared in three ways:

1. **Clear Specific Logger**:
   ```java
   MockLogger logger = (MockLogger) LoggerFactory.getLogger("test");
   logger.clearEvents();
   ```

2. **Clear All Loggers** (manually in @BeforeEach):
   ```java
   @BeforeEach
   void clearAllLoggers() {
       Map<String, Logger> loggers = MockLoggerFactory.getLoggers();
       loggers.values().forEach(l -> ((MockLogger) l).clearEvents());
   }
   ```

3. **Automatic Clearing** (using `MockLoggerExtension`):
   ```java
   @ExtendWith(MockLoggerExtension.class)
   class MyTest {
       @Slf4jMock
       Logger logger;
       
       @Test
       void test() {
           // Logger is automatically cleared before each test by the extension
       }
   }
   ```

## Integration with JUnit Extensions

The mock logger implementation provides hooks for JUnit 5 extensions:

### MockLoggerFactory Integration

```java
Map<String, Logger> allLoggers = MockLoggerFactory.getLoggers();
```

*   Allows extensions to iterate over all created loggers
*   Used for global operations like clearing events or enabling debug output

### MockLogger Integration

```java
MockLogger logger = (MockLogger) someLogger;
logger.setStdoutEnabled(true);  // Enable printing for debugging
```

*   Allows extensions to configure logging behavior
*   Example: Enable console output when a test fails (for debugging)

## Memory Considerations

### Event Retention

Events are retained in memory **indefinitely** unless explicitly cleared. This means:

*   **In short tests**: Memory usage is negligible
*   **In long tests or large test suites**: May accumulate thousands of events

**Best Practice**: Clear events between tests using JUnit extensions:

```java
@BeforeEach
void clearLogs() {
    MockLoggerFactory.getLoggers().values()
        .forEach(l -> ((MockLogger) l).clearEvents());
}
```

### Logger Registry

The `nameToLogger` map in `MockLoggerFactory` retains all created loggers for the lifetime of the test run. This is typically not an issue since:

*   Logger names are finite and reused across tests
*   Each logger instance is lightweight (just a name and configuration flags)

## Design Rationale

### Why Three Separate Classes?

1. **`MockLoggerFactory`**: Singleton pattern matches SLF4J's factory contract
2. **`MockLogger`**: Stateful instance matches SLF4J's logger contract (one logger per name)
3. **`MockLoggerEvent`**: Immutable value object for thread-safety and clarity

### Why ArrayList for Event Storage?

*   **Fast append**: O(1) for adding events during logging
*   **Fast indexed access**: O(1) for retrieving events by index in assertions
*   **Sequential access**: Events are naturally ordered by time
*   **Simple**: No complex data structures needed

Alternative considered: `ConcurrentLinkedQueue`
*   Rejected due to single-threaded test context
*   ArrayList is simpler and faster for the use case

### Why Not MDC Support?

Current implementation has `mdc: null` in events. MDC support is planned but not yet implemented. When implemented:

*   `MockMDCAdapter` will populate MDC map
*   Events will capture MDC state at logging time
*   Assertions will support MDC verification

## Summary

The mock logger implementation provides a clean separation of concerns:

| Class | Responsibility | Used Directly in Tests? |
|-------|----------------|------------------------|
| `MockLoggerFactory` | Manages logger registry | ❌ No (SLF4J calls it) |
| `MockLogger` | Captures log events | ❌ No (use `AssertLogger`) |
| `MockLoggerEvent` | Stores event details | ❌ No (use `AssertLogger`) |
| `AssertLogger` | Assertion API | ✅ Yes (this is the test API) |

**Key Takeaway**: The internal implementation classes (`MockLoggerFactory`, `MockLogger`, `MockLoggerEvent`) provide the infrastructure for capturing logs, but tests should **only** interact with them through the `AssertLogger` API. This maintains clean separation between implementation and test interface.

## References

*   [SLF4J Logger Interface](https://www.slf4j.org/api/org/slf4j/Logger.html)
*   [SLF4J Message Formatting](https://www.slf4j.org/faq.html#logging_performance)
*   [TDR-0001: In-Memory Event Storage](TDR-0001-in-memory-event-storage.md)
*   [TDR-0002: Use of JUnit5 Assertions](TDR-0002-use-of-junit5-assertions.md)
*   [SLF4J API Integration Guide](slf4j-api-integration.md)
