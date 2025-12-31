# TDR-0006: Test-Scoped Logger Isolation for Parallel Execution

**Status**: Accepted  
**Date**: 2025-12-31

## Context

When JUnit 5 executes tests in parallel, multiple test methods may run simultaneously in different threads. If two parallel tests request a logger with the same name (e.g., `LoggerFactory.getLogger("MyService")`), the original `MockLoggerFactory` implementation returned the same shared `MockLogger` instance to both tests.

This design caused test interference:

*   **Shared event storage**: Both tests appended events to the same in-memory list, making assertions unreliable
*   **Shared configuration**: Calling `setDebugEnabled(false)` in one test affected the other
*   **Unpredictable assertion results**: `AssertLogger.assertEventCount(logger, 3)` could fail if the parallel test logged additional events

The problem stemmed from `MockLoggerFactory` maintaining a global cache keyed only by logger name:

```java
Map<String, Logger> nameToLogger = new ConcurrentHashMap<>();
```

Any code path requesting the same logger name received the same instance, regardless of which test or thread made the request.

### Requirements

*   **Isolation**: Tests using the same logger name must receive independent `MockLogger` instances when running in parallel
*   **Backward compatibility**: Existing tests without JUnit extensions must continue to work unchanged in serial execution
*   **Transparent integration**: Users should not need to manually manage scope; the JUnit extension should handle it automatically
*   **No test code changes**: Tests already using `@WithMockLogger` or `@Slf4jMock` should gain isolation without modification

## Decision

Implement **test-scoped logger isolation** using a per-test scope identifier combined with logger name as the cache key.

### How It Works

1.  **Scoped cache key**: Replace the simple `String` key with a composite key `(scopeId, loggerName)`
2.  **Thread-local scope**: Store the current `scopeId` in an `InheritableThreadLocal<String>` so child threads inherit the parent's scope
3.  **Extension integration**: JUnit extensions (`MockLoggerExtension`, `MockLoggerDebugExtension`) set the scope to `ExtensionContext.getUniqueId()` at the start of each test and clear it afterward
4.  **Fallback to global**: When no scope is active (`scopeId == null`), behavior matches the original global cache, preserving backward compatibility

### Implementation Flow

**Parallel Test Example**:
```java
@WithMockLogger
class ParallelTestA {
    @Slf4jMock Logger logger;  // name = "ParallelTestA"
    
    @Test void test1() { logger.info("A1"); }
}

@WithMockLogger  
class ParallelTestB {
    @Slf4jMock Logger logger;  // name = "ParallelTestB"
    
    @Test void test1() { logger.info("B1"); }
}
```

When tests run in parallel:

1.  **Test A**: Extension sets `scopeId = "[engine:junit-jupiter]/[class:ParallelTestA]/[method:test1()]"`
2.  **Test B**: Extension sets `scopeId = "[engine:junit-jupiter]/[class:ParallelTestB]/[method:test1()]"`
3.  **Factory cache**:
    *   Key for Test A: `(scopeId_A, "ParallelTestA")` → MockLogger instance A
    *   Key for Test B: `(scopeId_B, "ParallelTestB")` → MockLogger instance B
4.  Each test receives an independent logger instance

**Same Logger Name Example**:
```java
@Test void testServiceA() {
    Logger logger = LoggerFactory.getLogger("MyService");
    // scopeId = unique_A, key = (unique_A, "MyService")
}

@Test void testServiceB() {
    Logger logger = LoggerFactory.getLogger("MyService");
    // scopeId = unique_B, key = (unique_B, "MyService")
}
```

Even though both request `"MyService"`, they receive different instances because their scope IDs differ.

## Consequences

### Positive

*   **Parallel test safety**: Tests using the same logger name no longer interfere when running in parallel
*   **Zero test migration effort**: Existing tests using `@WithMockLogger` or `@Slf4jMock` gain isolation automatically
*   **Backward compatible**: Tests without extensions continue to work in serial execution (scope remains `null`, falling back to global cache)
*   **Transparent**: Scope management is invisible to test code; extensions handle lifecycle automatically
*   **Inherited scope**: Child threads spawned during a test inherit the parent's `scopeId` via `InheritableThreadLocal`

### Negative

*   **Increased complexity**: Cache key is now a composite object instead of a simple string
*   **Thread pool limitation**: If test code uses a thread pool, worker threads may not inherit the correct scope (thread pool threads are reused, not spawned fresh)
*   **Legacy test limitation**: Tests that manually obtain loggers via static fields without extensions remain unscoped and unsafe for parallel execution

### Neutral

*   **Memory overhead**: Cache now holds multiple instances of the same logger name (one per scope), but instances are cleared after test completion
*   **Scope cleanup**: Extensions must properly clear scope in `afterEach` to avoid leaking scope into subsequent tests

## Alternatives

### Alternative 1: Simple ThreadLocal Without Composite Key

**Description**: Store logger instances in a `ThreadLocal<Map<String, Logger>>` instead of using a composite key.

**Rejected because**:
*   Does not support child threads (non-inheritable `ThreadLocal`)
*   Requires duplicating the entire cache map per thread, increasing memory usage
*   Does not integrate cleanly with extensions (harder to correlate thread to test)

### Alternative 2: WeakReference-Based Scoping

**Description**: Use `WeakHashMap` or `WeakReference` to automatically clean up logger instances when tests complete.

**Rejected because**:
*   Weak references are cleaned up by the garbage collector, which is non-deterministic
*   Cannot guarantee isolation during test execution (GC may not have run yet)
*   Difficult to correlate weak references to specific test executions

### Alternative 3: UUID Per Logger Instance

**Description**: Generate a unique ID for each logger at creation time and return a new instance on every `getLogger()` call.

**Rejected because**:
*   Breaks SLF4J's logger caching contract (same name should return same instance within a context)
*   Test code expecting `LoggerFactory.getLogger("X") == LoggerFactory.getLogger("X")` would break
*   Does not solve the problem of manually cached loggers in static fields

### Alternative 4: Manual Scope Management

**Description**: Require users to explicitly call `MockLoggerFactory.setScope("my-scope")` at the start of each test.

**Rejected because**:
*   Violates the transparency requirement (users must add boilerplate to all tests)
*   Error-prone (forgetting to set or clear scope causes subtle bugs)
*   Does not integrate with existing `@WithMockLogger` annotation usage

## Implementation

### Modified Components

**`MockLoggerFactory`**:
*   Added `InheritableThreadLocal<String> CURRENT_SCOPE_ID` for per-thread scope tracking
*   Changed cache from `Map<String, Logger>` to `Map<LoggerKey, Logger>` where `LoggerKey = (scopeId, loggerName)`
*   Exposed public API: `setCurrentScopeId(String)`, `getCurrentScopeId()`, `clearCurrentScopeId()`
*   Updated `getLogger(String)` to use composite key: `new LoggerKey(CURRENT_SCOPE_ID.get(), name)`
*   Modified `getLoggers()` to filter by current scope

**`MockServiceProvider` (SLF4J 2.0)**:
*   Changed `initialize()` to use singleton factory (`MockLoggerFactory.getInstance()`) instead of creating new instances

**`MockLoggerExtension`**:
*   `beforeEach()`: Sets `scopeId = context.getUniqueId()`
*   `beforeEach()`: Re-injects logger fields to ensure they reference the scoped instance
*   `afterEach()`: Clears scope via `MockLoggerFactory.clearCurrentScopeId()`
*   `postProcessTestInstance()`: Sets scope temporarily during field initialization

**`MockLoggerDebugExtension`**:
*   Wraps test execution with scope set/restore to ensure debug logging respects scope

**New Test**:
*   Added `MockLoggerFactoryScopeIsolationTest` to verify isolation behavior using multiple threads and scopes

**Documentation**:
*   Updated `README.md` to mention parallel test support when using JUnit extensions

### Validation

*   All 370 existing tests pass without modification (backward compatibility confirmed)
*   Tests pass under both SLF4J 2.0 (default) and SLF4J 1.7 (legacy) profiles
*   New isolation test verifies scoped cache behavior with concurrent access

## References

*   [TDR-0001: In-Memory Event Storage](TDR-0001-in-memory-event-storage.md) — Original decision to store events in memory per logger instance
*   [TDR-0004: JUnit Extension for Debug Logging](TDR-0004-junit-extension-for-debug-logging.md) — Extension lifecycle hooks used for scope management
