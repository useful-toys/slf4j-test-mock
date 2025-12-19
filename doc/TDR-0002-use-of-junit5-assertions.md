# TDR-0002: Use JUnit Jupiter for Tests and Assertions

**Status**: Accepted

**Date**: 2025-12-03

## Context

To validate the correctness of `slf4j-test-mock`, a robust testing framework is necessary. Furthermore, one of the core features of this library is to provide assertion utilities (`AssertLogger`) that integrate with the test lifecycle of the consuming project. The library, therefore, needs a foundation for its own tests and for the features it exposes to its users. Common options in the Java ecosystem are JUnit 4, JUnit 5 (Jupiter), and TestNG.

## Decision

We decided to use **JUnit Jupiter (JUnit 5)** as the fundamental dependency for the library's internal tests and assertion features.

The dependency was added with `compile` scope in the `pom.xml`, which means that `junit-jupiter-api` is not only used for the internal tests of `slf4j-test-mock` but also becomes a transitive dependency for projects that use this library.

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>${junit5.version}</version>
    <scope>compile</scope> <!-- Not just for internal tests, but also for the assertion API exposed to consumers -->
</dependency>
```

## Consequences

### Positive

*   **Modern and Extensible API**: JUnit 5 offers modern API with clear annotations (`@Test`, `@BeforeEach`, `@DisplayName`) and a powerful extension model.
*   **Market Adoption**: JUnit 5 is the de facto standard for new Java projects, ensuring greater compatibility and familiarity.
*   **Integration of Assertions**: `AssertLogger` can directly use JUnit's assertion exceptions (like `AssertionFailedError`), integrating natively with test reports.
*   **No Custom Exceptions**: Reuses standard JUnit exceptions, simplifying code and maintenance.

### Negative

*   **Transitive Dependency**: Any project using `slf4j-test-mock` will have a transitive dependency on `junit-jupiter-api`, which can rarely cause conflicts with incompatible JUnit 5 versions.
*   **Coupling with Test Framework**: The library becomes coupled to JUnit 5; assertion features may not work with other testing frameworks like TestNG without adaptation.
