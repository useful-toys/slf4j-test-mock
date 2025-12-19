# TDR-0004: Use JUnit 5 Extension for Debug Logging on Test Failures

**Status**: Accepted

**Date**: 2025-12-07

## Context

When unit tests using `AssertLogger` fail, developers cannot see what events were actually logged, making debugging time-consuming. The current API only shows what was expected, not what was actually logged.

**Problem**: When assertions fail, developers must manually add `System.out.println()` statements, use the debugger, or inspect `MockLogger.getLoggerEvents()` to understand what went wrong.

**Example**:
```java
@Test
void testLogging() {
    logger.info("Starting process");
    logger.warn("Problem detected");
    logger.error("Process failed");
    
    AssertLogger.assertEvent(logger, 0, "Expected message");
    // Fails with: "Expected event not found at index 0"
    // But which events WERE logged?
}
```

## Decision

We will implement a **JUnit 5 Extension** (`AssertLoggerDebugExtension`) that automatically prints all logged events to `System.err` when any test assertion fails.

### How It Works

1. **Extension Class**: Implements `InvocationInterceptor` from JUnit 5
2. **Event Formatter**: Utility class for human-readable output of logged events
3. **Usage**: Add `@ExtendWith(AssertLoggerDebugExtension.class)` to test classes

```java
@ExtendWith(AssertLoggerDebugExtension.class)
class MyTest {
    @Test
    void test() {
        Logger logger = LoggerFactory.getLogger("test");
        // Test code...
        // On assertion failure, all logged events are printed to stderr
    }
}
```

When a test fails, output shows:
```
Logged events:
  Total events: 3
  [0] INFO  | message="Starting process"
  [1] WARN  | message="Problem detected"
  [2] ERROR | message="Process failed"
```

## Consequences

### Positive

*   **Zero Code Changes to AssertLogger**: Existing API remains unchanged and fully backwards compatible.
*   **Automatic Behavior**: Works automatically once applied via `@ExtendWith`; impossible to forget to use.
*   **Clean Separation of Concerns**: Debug functionality is separate from assertion logic; can be enabled/disabled per test.
*   **JUnit 5 Standard Pattern**: Uses established JUnit 5 extension mechanism, well-documented and understood.
*   **Improved Developer Experience**: Faster debugging of test failures with immediate visibility into what was logged.
*   **Zero Overhead on Success**: No performance impact when tests pass; only formats events when assertion fails.

### Negative

*   **Additional Test Output**: Failed tests produce more output, which can be verbose in build logs.
*   **Learning Curve**: Developers need to understand JUnit 5 extensions and `@ExtendWith` annotation.
*   **Not Automatic for All Tests**: Requires explicit annotation; must be added to each test class or base class.
*   **Limited Auto-Discovery**: Current implementation detects loggers passed as test method parameters; doesn't automatically find logger fields.

## Alternatives Considered

### 1. Try-Catch Wrapper in AssertLogger Methods
Wrap every assertion method with try-catch to print events on failure.

**Rejected because**: Would require modifying 20+ methods with code repetition, making it intrusive and difficult to maintain.

### 2. Utility Wrapper Class
Users explicitly call a utility around assertions: `AssertLoggerDebug.withDebug(logger, () -> {...})`

**Rejected because**: Requires manual wrapping in every test, verbose, easy to forget, defeats the purpose of being automatic.

### 3. Custom AssertionError Subclass
Throw custom exception that includes event list.

**Rejected because**: Still requires modifying all `AssertLogger` methods and changes exception types, which could be a breaking change.

### 4. TestWatcher Extension
Use `TestWatcher` to detect failures after the test completes.

**Rejected because**: `TestWatcher` runs after the test, cannot easily access logger instances, and would require test-level registration of loggers.

## Implementation

- **Extension Class**: `org.usefultoys.slf4jtestmock.AssertLoggerDebugExtension` (implements `InvocationInterceptor`)
- **Formatter Class**: `org.usefultoys.slf4jtestmock.LoggerEventFormatter` (utility for formatting events)
- **Tests**: `AssertLoggerDebugExtensionTest.java` with examples for single/multiple loggers, markers, and exceptions
- **No new dependencies**: Uses only existing JUnit 5 and SLF4J dependencies

## References

- [JUnit 5 Extensions Documentation](https://junit.org/junit5/docs/current/user-guide/#extensions)
- [InvocationInterceptor API](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/InvocationInterceptor.html)
- TDR-0002: Use JUnit Jupiter for Tests and Assertions

