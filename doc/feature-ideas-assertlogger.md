# Feature Ideas: AssertLogger Enhancements

**Status**: Draft (ideas backlog)  
**Date**: 2025-12-31

This document collects potential future enhancements for the `AssertLogger` API.
These ideas are **not implemented**; they are suggestions to guide future issues and PRs.

## Goals

- Keep tests readable and intention-revealing.
- Avoid coupling tests to internal implementation classes (`MockLogger`, `MockLoggerEvent`).
- Prefer stable assertions that are resilient to formatting differences.
- Provide helpful failure messages with enough context to debug quickly.

## Proposed Assertion Areas

### 1) Argument-based assertions (beyond message parts)

**Problem**: Message-part assertions are robust, but sometimes too permissive. You may want to validate that the *arguments* passed to SLF4J placeholders are correct (type, value, order) without relying on formatted strings.

**Ideas**:
- `assertEventHasArgument(Logger logger, int eventIndex, int argumentIndex, Object expected)`
- `assertEventHasArguments(Logger logger, int eventIndex, Object... expectedArgs)`
- `assertHasEventWithArgument(Logger logger, Object expected)`

**Example**:
```java
logger.info("User {} logged in", userId);
assertEventHasArgument(logger, 0, 0, userId);
```

### 2) Template vs formatted message

**Problem**: Some teams treat the message template as part of a logging contract (e.g., for audits or log parsers).

**Ideas**:
- `assertEventMessageTemplateEquals(Logger logger, int eventIndex, String expectedTemplate)`
- `assertHasEventWithMessageTemplate(Logger logger, String expectedTemplate)`

**Example**:
```java
logger.info("User {} logged in from {}", userId, ip);
assertEventMessageTemplateEquals(logger, 0, "User {} logged in from {}");
```

### 3) Negative assertions (absence checks)

**Problem**: It is common to assert that something was *not* logged, without writing custom loops.

**Ideas**:
- `assertNoEvent(Logger logger, String... messageParts)`
- `assertNoEvent(Logger logger, Level level, String... messageParts)`
- `assertNoEvent(Logger logger, Marker marker, String... messageParts)`
- `assertNoEventsAtLevel(Logger logger, Level level)`

**Example**:
```java
assertNoEventsAtLevel(logger, Level.ERROR);
assertNoEvent(logger, "password");
```

### 4) MDC assertions

**Problem**: Correlation IDs (trace/span/user/session) are commonly propagated via MDC.
When a test relies on MDC, it is useful to validate that the event captured the expected MDC state.

**Ideas**:
- `assertEventMdcContains(Logger logger, int eventIndex, String key, String expectedValue)`
- `assertEventMdcHasKeys(Logger logger, int eventIndex, String... keys)`
- `assertHasEventWithMdc(Logger logger, String key, String expectedValue)`

**Example**:
```java
// MDC.put("traceId", traceId);
logger.info("Request accepted");
assertEventMdcContains(logger, 0, "traceId", traceId);
```

### 5) Partial ordering / subsequence assertions (concurrency-friendly)

**Problem**: For concurrent or asynchronous code, a total order may be non-deterministic,
but you may still want to verify a *partial order* or a subsequence.

**Ideas**:
- `assertContainsSubsequence(Logger logger, Level... levels)`
- `assertContainsSubsequence(Logger logger, String... messagePartsInOrder)`
- `assertEventOccursBefore(Logger logger, String earlierMessagePart, String laterMessagePart)`

**Example**:
```java
// order of all events may vary, but "started" must appear before "finished"
assertEventOccursBefore(logger, "started", "finished");
```

### 6) Count constraints (at least / at most)

**Problem**: “Not more than N warnings” is a common requirement.

**Ideas**:
- `assertEventCountAtMost(Logger logger, int max)`
- `assertEventCountAtLeast(Logger logger, int min)`
- `assertEventCountAtMost(Logger logger, Level level, int max)`

**Example**:
```java
assertEventCountAtMost(logger, Level.WARN, 1);
```

### 7) Regex / pattern matching

**Problem**: Sometimes you need flexible matching (IDs, JSON fragments) without exact strings.

**Ideas**:
- `assertEventMessageMatches(Logger logger, int eventIndex, Pattern pattern)`
- `assertHasEventMessageMatches(Logger logger, Pattern pattern)`

**Example**:
```java
assertEventMessageMatches(logger, 0, Pattern.compile("traceId=[a-f0-9]{32}"));
```

### 8) Richer throwable assertions

**Problem**: Current assertions cover type and message parts; occasionally you need deeper checks.

**Ideas**:
- `assertEventThrowableCauseIs(Logger logger, int eventIndex, Class<? extends Throwable> causeType)`
- `assertEventThrowableHasSuppressed(Logger logger, int eventIndex, Class<? extends Throwable> suppressedType)`
- `assertEventThrowableChainContains(Logger logger, int eventIndex, Class<? extends Throwable> type)`

**Example**:
```java
assertEventThrowableCauseIs(logger, 0, SQLException.class);
```

## Notes and Open Questions

- Should argument assertions treat arrays/iterables specially (deep equals)?
- Should message-template assertions use the raw template (format) stored in events or always use `getFormattedMessage()`?
- For MDC assertions, define behavior when MDC is null/empty (fail with explicit message).
- For subsequence/partial-order assertions, define how to match (by level, marker, message parts, or a predicate).

## Fluent Assertions DSL (Proposal)

This section proposes a fluent assertion API that complements (or eventually replaces) individual `AssertLogger` helper methods.
It is intended to improve readability, provide richer failure messages, and reduce API surface growth.

### Goals

- Express assertions in a more intention-revealing way (builder-style, fluent chaining).
- Keep the existing model where tests assert against the SLF4J `Logger` interface, not internal types.
- Prefer deterministic output and helpful diagnostics on failures.

### Non-goals

- No new runtime dependency on assertion libraries beyond JUnit 5.
- No attempt to “fix” asynchronous/thread-pool propagation issues; those remain a separate concern.

### Sketch API

**Entry points**:

- `LogAssertions.assertThat(Logger logger)`
- `LogAssertions.assertThat(Logger logger).hasEventCount(int)`

**Event selection**:

- `event(int index)` for deterministic tests
- `anyEvent()` for “exists” style assertions

**Event assertions**:

- `hasLevel(Level)` / `hasMarker(Marker)`
- `message().hasTemplate(String)` / `message().containsParts(String...)` / `message().matches(Pattern)`
- `arguments().containsExactly(Object...)` / `argument(int).isEqualTo(Object)`
- `throwable().isInstanceOf(Class)` / `throwable().messageContains(String...)` / `throwable().causeIsInstanceOf(Class)`
- `mdc().containsEntry(String key, String value)`

### Examples

#### Count / empty

**Empty**:

```java
LogAssertions.assertThat(logger)
	.isEmpty();
```

**Exact count**:

```java
LogAssertions.assertThat(logger)
	.hasEventCount(3);
```

**Count constraints (at least / at most)**:

```java
LogAssertions.assertThat(logger)
	.hasAtLeast(1);

LogAssertions.assertThat(logger)
	.hasAtMost(10);
```

**Count with predicates (combinations)**:

```java
LogAssertions.assertThat(logger)
	.countWhere(event -> event.hasLevel(Level.WARN))
	.isAtMost(1);

LogAssertions.assertThat(logger)
	.countWhere(event -> event.hasMarker(marker))
	.isEqualTo(2);

LogAssertions.assertThat(logger)
	.countWhere(event -> event.message().containsParts("rate limit"))
	.isAtLeast(1);
```

#### Existence (order does not matter)

**Exists (at least one event)**:

```java
LogAssertions.assertThat(logger)
	.anyEvent()
	.hasLevel(Level.WARN)
	.message().containsParts("rate limit", "retry");
```

**Exists with combinations (level + marker + message parts)**:

```java
LogAssertions.assertThat(logger)
	.anyEvent()
	.hasLevel(Level.ERROR)
	.hasMarker(marker)
	.message().containsParts("operation", "failed")
	.throwable().isInstanceOf(RuntimeException.class);
```

**Contains multiple required events in any order**:

```java
LogAssertions.assertThat(logger)
	.containsInAnyOrder(
		event -> event.hasLevel(Level.INFO)
			.message().containsParts("started"),
		event -> event.hasLevel(Level.INFO)
			.message().containsParts("finished")
	);
```

#### Sequence (levels / markers / parts)

**Sequence by levels (ignoring other events in between)**:

```java
LogAssertions.assertThat(logger)
	.containsLevelSequence(Level.INFO, Level.WARN, Level.ERROR);
```

**Sequence by markers**:

```java
LogAssertions.assertThat(logger)
	.containsMarkerSequence(auditMarker, auditMarker, securityMarker);
```

**Sequence by message parts**:

```java
LogAssertions.assertThat(logger)
	.containsMessagePartsSequence(
		"starting",
		"processing",
		"done"
	);
```

**Fully-specified sequence steps (combinations)**:

```java
LogAssertions.assertThat(logger)
	.containsSequence(
		step -> step.hasLevel(Level.INFO)
			.message().containsParts("starting"),
		step -> step.hasLevel(Level.DEBUG)
			.hasMarker(marker)
			.message().containsParts("payload"),
		step -> step.hasLevel(Level.INFO)
			.message().containsParts("done")
	);
```

#### Exact-by-index

```java
LogAssertions.assertThat(logger)
	.event(0)
	.hasLevel(Level.INFO)
	.message().hasTemplate("User {} logged in")
	.arguments().containsExactly(userId);
```

#### Exists / any-event

```java
LogAssertions.assertThat(logger)
	.anyEvent()
	.hasLevel(Level.WARN)
	.message().containsParts("rate limit", "retry");
```

#### Negative assertion

```java
LogAssertions.assertThat(logger)
	.hasNoEvent()
	.withLevel(Level.ERROR);
```

#### Partial ordering (concurrency-friendly)

```java
LogAssertions.assertThat(logger)
	.containsSubsequence(
		event -> event.message().containsParts("started"),
		event -> event.message().containsParts("finished")
	);
```

### Implementation Notes

- The fluent DSL should be implemented on top of (or alongside) the existing `AssertLogger` utilities to avoid duplication.
- Failure messages should print a compact deterministic dump of captured events (level, marker, template, args, throwable summary, MDC keys).
- The API should avoid exposing `MockLoggerEvent` directly; use an internal adapter or a minimal read-only event view.

## Additional Feature Ideas (Beyond Assertions)

These ideas are not new `AssertLogger` methods, but features that can add value for validating logs produced by code under test.

### 1) Snapshot / approval testing for log output

**Problem**: For larger flows, asserting each individual event is verbose. A deterministic “snapshot” of logs can validate the overall output.

**Ideas**:
- Provide a stable formatter that produces a canonical representation (level, marker, template, args, throwable summary, MDC).
- Support snapshot file generation and comparison (kept in the *test project*, not in this library).

**Example**:
```java
String snapshot = LoggerEventFormatter.toDeterministicText(logger);
assertEquals(expectedSnapshot, snapshot);
```

### 2) Value scrubbing / normalization

**Problem**: Tests often fail due to variable values (timestamps, UUIDs, random IDs, ports, file paths).

**Ideas**:
- Provide helpers to normalize or redact patterns before comparison.
- Support user-defined scrubbers (regex-based or predicate-based).

**Example**:
```java
String normalized = Scrubbers.uuid().apply(text);
```

### 3) Awaiting async logs (test helper)

**Problem**: When code logs asynchronously, tests can be flaky if they assert too early.

**Ideas**:
- `awaitEventCount(Logger logger, int expected, Duration timeout)`
- `awaitHasEvent(Logger logger, Level level, Duration timeout, String... parts)`

This should be implemented carefully to avoid encouraging arbitrary sleeps.

### 4) Logging quality rules (“log lint”)

**Problem**: Some teams want to enforce logging best practices as part of tests.

**Ideas**:
- Verify that sensitive fields are not logged (simple patterns or user rules).
- Verify that error logs include throwable when expected.
- Verify “no ERROR logs” in happy-path tests.

### 5) Richer debugging reports on failure

**Problem**: When a test fails, it can be helpful to print a structured report.

**Ideas**:
- Group events by logger name and level.
- Include counts per level and an excerpt of recent events.
- Provide deterministic formatting so diffs are stable.

### 6) Selective capture / filtering

**Problem**: Large suites may create many loggers and events, increasing noise and memory usage.

**Ideas**:
- Allow capturing only specific loggers (by name prefix) in a test scope.
- Allow dropping events below a configured level for specific loggers.

### 7) Additional test framework integration

**Problem**: Not all projects use JUnit 5.

**Ideas**:
- Provide optional integration hooks for other runners (e.g., TestNG) while keeping the core independent.

## Prioritization Suggestions

If the goal is maximum practical value with minimal API surface, a reasonable order is:

1. Await helpers for async logs
2. Value scrubbing/normalization
3. Snapshot-friendly deterministic formatting

## Related Documents

- [Mock Logger Implementation Guide](mock-logger-implementation.md)
- [TDR-0002: Use of JUnit5 Assertions](TDR-0002-use-of-junit5-assertions.md)
