# TDR-0001: In-Memory Event Storage for MockLogger

**Status**: Accepted

**Date**: 2025-11-29

## Context

The primary purpose of the `slf4j-test-mock` library is to enable developers to verify logging behavior within unit tests. To achieve this, log events generated during test execution must be captured in a way that is fast, reliable, and easy to inspect.

Tests need a mechanism to:
1.  Isolate log output from different tests to prevent interference.
2.  Access captured log events programmatically to perform assertions.
3.  Inspect structured log data, such as log level, message, arguments, markers, and throwables, not just the final formatted string.
4.  Ensure that the test execution remains fast, without the overhead of I/O operations (e.g., writing to disk or console).

## Decision

We decided to store captured log events in an in-memory `java.util.List<MockLoggerEvent>` within each `MockLogger` instance.

Each time a logging method (e.g., `logger.info(...)`) is called on a `MockLogger` instance, a new `MockLoggerEvent` object is created and added to this internal list.

The `MockLogger` class provides public methods to access these captured events, such as `getEvent(int)`, `getEventCount()`, and `getLoggerEvents()`, which allow test assertion utilities like `AssertLogger` to inspect the results.

## Consequences

### Positive

*   **High Performance**: Storing events in an in-memory `ArrayList` is extremely fast, minimizing the impact on test execution time.
*   **Rich Assertions**: Storing structured `MockLoggerEvent` objects allows for precise assertions on any part of the log event (level, marker, throwable, raw message, arguments), rather than parsing a formatted string.
*   **Test Isolation**: Since each `MockLogger` instance holds its own list of events, tests remain isolated from one another, provided that logger instances are not shared across tests without being cleared.
*   **Simplicity**: The solution is simple to implement and understand, relying only on standard Java Collection APIs. It introduces no external dependencies.
*   **No I/O Overhead**: Avoids the performance penalties and complexities associated with file or network I/O, making it ideal for a unit testing environment.
*   **Clear Event Boundaries**: A list of objects provides clear separation between individual log events. In contrast, text-based streams or files can make it ambiguous where one event ends and another begins, especially with multi-line stack traces.
*   **Clean Test Output**: Since log events are captured in memory instead of being printed to the console, the test execution output remains clean and free from log noise, making it easier to spot actual test failures.

### Negative

*   **Memory Consumption**: For tests that generate an extremely large number of log events, storing them all in memory could lead to high RAM usage and potentially an `OutOfMemoryError`. This is considered an edge case for typical unit tests.
*   **Limited Thread Safety**: The use of a standard `ArrayList` makes the collection of events inherently not thread-safe. The library's documentation explicitly states it is designed for single-threaded test environments. Concurrent logging to the same `MockLogger` instance from multiple threads would require external synchronization.
*   **Transient Storage**: Events are discarded once the `MockLogger` instance is garbage collected, which is the desired behavior for tests but makes it unsuitable for any kind of persistent log analysis.
*   **Requires Manual State Management**: Developers must explicitly clear the logger's state (i.e., the event list) between tests (e.g., in a `@BeforeEach` method). Failure to do so can cause events from one test to leak into another, leading to flaky and unreliable assertions.
*   **Challenges with Parallel Test Execution**: Since the `MockLoggerFactory` provides the same logger instance for the same name, running tests in parallel that request loggers with identical names will lead to interference. This requires a strategy of using unique logger names per test to ensure isolation in parallel execution environments.

## Alternatives Considered

### 1. Writing to a File

*   **Description**: Log events would be written to a temporary file on disk. Tests would then read this file to perform assertions.
*   **Rejected because**: This approach is significantly slower due to I/O latency. It also adds complexity related to file management (creation, cleanup). Most importantly, it requires parsing plain text, which is fragile and loses the structured, high-fidelity information of the original log event (e.g., distinct arguments, marker objects, throwable type). The final text representation depends on the formatter and may not be a faithful representation of all event attributes.

### 2. Writing to an In-Memory Stream (e.g., `ByteArrayOutputStream`)

*   **Description**: Log events would be formatted into a string and written to an in-memory byte stream.
*   **Rejected because**: While this avoids disk I/O, it still requires parsing a raw string to make assertions on specific event properties. This approach loses the structured data, offers less flexibility for detailed validation, and is dependent on a specific log format, making tests brittle. It is less powerful than inspecting a list of structured objects.

### 3. Using a Database (In-Memory or File-Based)

*   **Description**: Log events would be inserted into a database like H2 or SQLite.
*   **Rejected because**: This would introduce significant overhead and external dependencies (e.g., a JDBC driver), which is overly complex for the problem domain. It goes against the goal of creating a lightweight, dependency-free testing utility.

---

This ADR clarifies why the current approach was chosen and documents the trade-offs involved. It serves as a reference for future development and for users seeking to understand the library's design.