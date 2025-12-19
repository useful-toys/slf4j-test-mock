# TDR-0003: Focus on the SLF4J Facade

**Status**: Accepted

**Date**: 2025-12-03

## Context

The Java logging ecosystem is fragmented, with multiple competing logging libraries such as Logback, Log4j2, `java.util.logging` (JUL), and Apache Commons Logging (JCL). To abstract this complexity, most modern libraries and applications use a logging facade, with the **Simple Logging Facade for Java (SLF4J)** being the most widespread and adopted in the market.

When developing a testing tool for logs, a decision had to be made whether the mock should simulate a specific implementation (like Logback) or couple itself to a facade.

## Decision

We decided to design and implement `slf4j-test-mock` as an **implementation of the SLF4J API**.

Instead of simulating the internal behavior of a specific logging library, the project positions itself as one of the possible "implementations" that SLF4J can find on the classpath at runtime. To do this, it provides the `org.slf4j.impl.StaticLoggerBinder` class, which is the entry point SLF4J uses to bind to a logging implementation.

## Consequences

### Positive

*   **Maximum Compatibility**: Works with any project using SLF4J, regardless of the production logging implementation (Logback, Log4j2, etc.). Simply replace the production dependency with `slf4j-test-mock` in test scope.
*   **Abstraction and Simplicity**: Only deals with SLF4J abstractions (`Logger`, `Marker`, `MDC`), which is simpler and more stable than internal logging implementation APIs.
*   **Focus on Contract, Not Implementation**: Tests verify the application's logging "contract," not implementation details, making tests robust against configuration and library changes.

### Negative

*   **Implementation-Specific Features Not Testable**: Cannot test implementation-specific features like advanced Logback appender configurations or Log4j2 lookups. Tool scope is limited to the SLF4J API.
*   **Requires SLF4J**: Only works in projects that have adopted SLF4J as their logging facade (low risk; this is the recommended practice).
