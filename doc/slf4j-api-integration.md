# SLF4J API Integration Guide

This document explains how `slf4j-test-mock` integrates natively with the SLF4J API, including the technical details of supporting both SLF4J 1.7.x and 2.0.x versions.

## Overview

`slf4j-test-mock` implements the SLF4J Service Provider Interface (SPI), positioning itself as a logging implementation that SLF4J can discover and bind to at runtime. This allows the mock to seamlessly replace production logging implementations during test execution without requiring any code changes.

## SLF4J Binding Mechanism

SLF4J uses different binding mechanisms depending on the version:

### SLF4J 1.7.x: Static Binder Pattern

SLF4J 1.7.x uses a **compile-time static binding** pattern where it searches for specific classes in the `org.slf4j.impl` package on the classpath.

**Required Classes:**

1. **`StaticLoggerBinder`** - Provides the logger factory
   - Package: `org.slf4j.impl`
   - Must implement: `org.slf4j.spi.LoggerFactoryBinder` (deprecated in 2.0)
   - Must have: `public static StaticLoggerBinder getSingleton()` method
   - Returns: `MockLoggerFactory` instance

2. **`StaticMarkerBinder`** - Provides the marker factory
   - Package: `org.slf4j.impl`
   - Must implement: `org.slf4j.spi.MarkerFactoryBinder` (deprecated in 2.0)
   - Returns: `BasicMarkerFactory` (standard SLF4J implementation)

3. **`StaticMDCBinder`** - Provides the MDC adapter
   - Package: `org.slf4j.impl`
   - Must have: `public static final StaticMDCBinder SINGLETON` field
   - Returns: `MockMDCAdapter` instance

**Example Implementation:**

```java
@SuppressWarnings("deprecation") // Intentional for SLF4J 1.7.x compatibility
public final class StaticLoggerBinder implements LoggerFactoryBinder {
    
    public static final String REQUESTED_API_VERSION = "1.6";
    static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
    
    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }
    
    @Override
    public ILoggerFactory getLoggerFactory() {
        return MockLoggerFactory.getInstance();
    }
    
    @Override
    public String getLoggerFactoryClassStr() {
        return MockLoggerFactory.class.getName();
    }
}
```

**Discovery Process (SLF4J 1.7.x):**

1. SLF4J looks for `org.slf4j.impl.StaticLoggerBinder` on the classpath
2. Calls `StaticLoggerBinder.getSingleton()` to obtain the binder instance
3. Calls `getLoggerFactory()` to obtain the `ILoggerFactory` implementation
4. All subsequent `LoggerFactory.getLogger()` calls use this factory

### SLF4J 2.0.x: Service Provider Interface

SLF4J 2.0+ uses **Java ServiceLoader** mechanism for more flexible, runtime-based provider discovery.

**Required Components:**

1. **`MockServiceProvider`** - Single provider class
   - Package: `org.slf4j.impl` (by convention)
   - Must implement: `org.slf4j.spi.SLF4JServiceProvider`
   - Provides: Logger factory, Marker factory, and MDC adapter

2. **Service Registration File**
   - Location: `META-INF/services/org.slf4j.spi.SLF4JServiceProvider`
   - Content: `org.slf4j.impl.MockServiceProvider`

**Example Implementation:**

```java
public class MockServiceProvider implements SLF4JServiceProvider {
    
    public static final String REQUESTED_API_VERSION = "2.0.99";
    
    private ILoggerFactory loggerFactory;
    private IMarkerFactory markerFactory;
    private MDCAdapter mdcAdapter;
    
    @Override
    public void initialize() {
        loggerFactory = new MockLoggerFactory();
        markerFactory = StaticMarkerBinder.SINGLETON.getMarkerFactory();
        mdcAdapter = StaticMDCBinder.SINGLETON.getMDCA();
    }
    
    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }
    
    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }
    
    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }
    
    @Override
    public String getRequestedApiVersion() {
        return REQUESTED_API_VERSION;
    }
}
```

**Discovery Process (SLF4J 2.0+):**

1. SLF4J uses `ServiceLoader.load(SLF4JServiceProvider.class)` to find providers
2. Reads `META-INF/services/org.slf4j.spi.SLF4JServiceProvider` to locate the implementation
3. Instantiates `MockServiceProvider` and calls `initialize()`
4. Calls `getLoggerFactory()`, `getMarkerFactory()`, and `getMDCAdapter()` as needed

## Multi-Version Compatibility Strategy

To support both SLF4J 1.7.x and 2.0.x with a single codebase, `slf4j-test-mock` uses the following approach:

### 1. Maven Profiles

Two mutually exclusive profiles control which SLF4J version to compile against:

```xml
<profiles>
    <!-- Default: SLF4J 2.0 -->
    <profile>
        <id>slf4j-2.0</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <slf4j.version>2.0.16</slf4j.version>
        </properties>
    </profile>
    
    <!-- Legacy: SLF4J 1.7 -->
    <profile>
        <id>slf4j-1.7</id>
        <properties>
            <slf4j.version>1.7.36</slf4j.version>
        </properties>
        <build>
            <plugins>
                <plugin>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <excludes>
                            <!-- Exclude SLF4J 2.0-specific class -->
                            <exclude>**/MockServiceProvider.java</exclude>
                        </excludes>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

### 2. Conditional Compilation

- **SLF4J 2.0 profile (default)**: Compiles all classes including `MockServiceProvider`
- **SLF4J 1.7 profile**: Excludes `MockServiceProvider.java` from compilation (uses maven-compiler-plugin exclude)

### 3. Shared Core Implementation

Core classes are version-agnostic and work with both versions:

- `MockLoggerFactory` - Implements `ILoggerFactory` (unchanged between versions)
- `MockLogger` - Implements `Logger` interface (unchanged between versions)
- `MockMDCAdapter` - Implements `MDCAdapter` with all methods (without `@Override` on 2.0-specific methods)
- `StaticMarkerBinder` - Shared by both versions (deprecated but still works in 2.0)
- `StaticMDCBinder` - Shared by both versions

### 4. MDC Compatibility

`MockMDCAdapter` implements **all** MDC methods, including SLF4J 2.0 Deque methods:

```java
public class MockMDCAdapter implements MDCAdapter {
    
    // SLF4J 1.7 methods (with @Override)
    @Override
    public void put(String key, String val) { ... }
    
    @Override
    public String get(String key) { ... }
    
    // SLF4J 2.0 Deque methods (without @Override to avoid compile error in 1.7)
    public void pushByKey(String key, String value) { ... }
    
    public String popByKey(String key) { ... }
    
    public Deque<String> getCopyOfDequeByKey(String key) { ... }
    
    public void clearDequeByKey(String key) { ... }
}
```

**Why no `@Override` on 2.0 methods?**

The Deque methods don't exist in the SLF4J 1.7 `MDCAdapter` interface. Adding `@Override` would cause compilation errors when building with the `slf4j-1.7` profile. By omitting `@Override`, the class compiles successfully with both versions:
- In SLF4J 1.7: Methods exist but aren't called (no harm)
- In SLF4J 2.0: Methods properly implement the interface contract

## Runtime Behavior

At runtime, SLF4J automatically selects the appropriate binding mechanism:

| SLF4J Version | Binding Mechanism | Provider Class | Discovery Method |
|---------------|-------------------|----------------|------------------|
| 1.7.x | Static Binder | `StaticLoggerBinder` | Classpath search for hardcoded class name |
| 2.0.x | Service Provider | `MockServiceProvider` | ServiceLoader with META-INF/services |

**Both versions ultimately return the same implementations:**
- `MockLoggerFactory` for logger creation
- `BasicMarkerFactory` for markers (standard SLF4J)
- `MockMDCAdapter` for MDC operations

## Build and Release Process

1. **Default Build (SLF4J 2.0)**:
   ```bash
   mvnw clean install
   ```
   - Includes `MockServiceProvider`
   - Includes `META-INF/services` registration
   - Includes Static Binders for backward compatibility

2. **SLF4J 1.7 Compatibility Test**:
   ```bash
   mvnw clean test -P slf4j-1.7
   ```
   - Excludes `MockServiceProvider` from compilation
   - Tests pass using Static Binders only

3. **CI/CD Matrix**:
   - Both profiles are tested in parallel
   - Release only happens if both versions pass all tests

## Key Design Principles

1. **Single Artifact**: One JAR works with both SLF4J 1.7 and 2.0
2. **No Runtime Detection**: Version detection happens at compile time via Maven profiles
3. **Backward Compatibility**: SLF4J 2.0 build includes both old (Static Binders) and new (ServiceProvider) mechanisms
4. **Forward Compatibility**: SLF4J 1.7 build includes only Static Binders (ServiceProvider excluded at compile time)
5. **Zero User Configuration**: SLF4J automatically discovers and uses the mock at runtime

## Integration Pattern

When a project includes `slf4j-test-mock` as a test dependency:

```xml
<dependency>
    <groupId>org.usefultoys</groupId>
    <artifactId>slf4j-test-mock</artifactId>
    <version>1.x.x</version>
    <scope>test</scope>
</dependency>
```

**What happens:**

1. Test classpath includes `slf4j-test-mock.jar`
2. SLF4J scans the classpath and finds the mock provider
3. SLF4J binds to `MockLoggerFactory` (via Static Binder or ServiceProvider)
4. All `LoggerFactory.getLogger()` calls return `MockLogger` instances during tests
5. Production classpath (without `slf4j-test-mock`) uses the production logging implementation

## References

- [SLF4J Manual - Binding](https://www.slf4j.org/manual.html#swapping)
- [SLF4J 2.0 Migration Guide](https://www.slf4j.org/faq.html#changesInVersion200)
- [TDR-0003: Focus on the SLF4J Facade](TDR-0003-focus-on-slf4j-facade.md)
- [TDR-0005: Multiple SLF4J Version Support](TDR-0005-multiple-slf4j-version-support.md)
