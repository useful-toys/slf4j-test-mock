# TDR-0003: Focus on the SLF4J Facade

Date: 2025-12-03

## Context

The Java logging ecosystem is fragmented, with multiple competing logging libraries such as Logback, Log4j2, `java.util.logging` (JUL), and Apache Commons Logging (JCL). To abstract this complexity, most modern libraries and applications use a logging facade, with the **Simple Logging Facade for Java (SLF4J)** being the most widespread and adopted in the market.

When developing a testing tool for logs, a decision had to be made whether the mock should simulate a specific implementation (like Logback) or couple itself to a facade.

## Decision

We decided to design and implement `slf4j-test-mock` as an **implementation of the SLF4J API**.

Instead of simulating the internal behavior of a specific logging library, the project positions itself as one of the possible "implementations" that SLF4J can find on the classpath at runtime. To do this, it provides the `org.slf4j.impl.StaticLoggerBinder` class, which is the entry point SLF4J uses to bind to a logging implementation.

## Consequences

### Positive

1.  **Maximum Compatibility**: By focusing on the SLF4J facade, `slf4j-test-mock` can be used in any project that already uses SLF4J, regardless of the logging implementation the project uses in production (Logback, Log4j2, etc.). To use the mock, one simply needs to replace the production implementation dependency with `slf4j-test-mock` in the test scope.
2.  **Abstraction and Simplicity**: The library only deals with SLF4J abstractions (`Logger`, `Marker`, `MDC`), which is a much simpler and more stable API than the complex internal APIs of logging implementations. This reduces the development and maintenance complexity of the mock.
3.  **Focus on the Contract, Not the Implementation**: Testing what is logged via SLF4J means that the tests are verifying the application's logging "contract," not implementation details. This makes tests more robust against changes in the configuration or the logging library used in production.

### Negative

1.  **Specific Features Are Not Testable**: `slf4j-test-mock` cannot be used to test features that are specific to a logging implementation, such as advanced Logback appender configurations or Log4j2 lookups. The tool's scope is deliberately limited to what is possible through the SLF4J API.
2.  **Requires SLF4J**: The mock only works in projects that have already adopted SLF4J as their logging facade. This is a low risk, as this is the recommended and majority practice in the Java software industry.
