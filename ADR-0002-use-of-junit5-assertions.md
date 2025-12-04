# ADR-0002: Use JUnit Jupiter for Tests and Assertions

Date: 2025-12-03

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

1.  **Modern and Extensible API**: JUnit 5 offers a more modern API, with clear annotations (`@Test`, `@BeforeEach`, `@DisplayName`, etc.) and a powerful extension model, which can be leveraged in the future.
2.  **Market Adoption**: JUnit 5 is the de facto standard for new Java projects, ensuring greater compatibility and familiarity for developers using `slf4j-test-mock`.
3.  **Integration of Assertions**: By including `junit-jupiter-api` with compile scope, `AssertLogger` can directly use JUnit's assertion exceptions (like `AssertionFailedError`), integrating natively and transparently with the test reports of consumer projects.
4.  **No need to create custom exceptions**: Reuses standard JUnit exceptions, simplifying code and maintenance.

### Negative

1.  **Transitive Dependency**: The main consequence is that any project using `slf4j-test-mock` will have a transitive dependency on `junit-jupiter-api`. This can, in rare cases, cause conflicts if the consumer project uses an incompatible version of JUnit 5.
2.  **Coupling with the Test Framework**: The library becomes coupled to JUnit 5, which means that the assertion features may not work as expected with other testing frameworks like TestNG without an adaptation layer. Given the predominance of JUnit, this risk was considered low.
