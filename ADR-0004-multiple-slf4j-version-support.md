# ADR-0004: Support for Multiple SLF4J API Versions

## Status
Accepted

## Context
SLF4J has two major API versions in widespread use:
- **SLF4J 1.7.x**: The traditional version, with the last release being 1.7.36
- **SLF4J 2.0.x**: The modern version with breaking changes and new features

Projects using different SLF4J versions need a compatible mock implementation for testing. Supporting both versions allows the library to be useful for a wider range of projects, including legacy systems on 1.7.x and modern applications on 2.0.x.

## Decision
We will support both SLF4J 1.7.x and 2.0.x through Maven profiles:

### Implementation Strategy
1. **Maven Profiles**: Two profiles (`slf4j-1.7` and `slf4j-2.0`) control the SLF4J API dependency version
2. **Conditional Compilation**: The `MockServiceProvider` class (required for SLF4J 2.0) is excluded from compilation in the 1.7 profile
3. **Dual Service Provider Mechanism**:
   - SLF4J 1.7: Uses `StaticLoggerBinder`, `StaticMarkerBinder`, `StaticMDCBinder`
   - SLF4J 2.0: Uses `MockServiceProvider` implementing `SLF4JServiceProvider`
4. **API Compatibility**: MDCAdapter implements all methods including SLF4J 2.0 Deque methods, but without `@Override` annotations to maintain 1.7 compatibility

### Technical Details

#### Profile Configuration
```xml
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
                        <exclude>**/MockServiceProvider.java</exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

#### Service Provider Registration
- For SLF4J 2.0: `META-INF/services/org.slf4j.spi.SLF4JServiceProvider`
- For SLF4J 1.7: Static Binder classes in `org.slf4j.impl` package

## Consequences

### Positive
- **Wider Adoption**: Library can be used with both SLF4J 1.7 and 2.0 projects
- **Future-Proof**: Easy to add support for future SLF4J versions
- **No Code Duplication**: Single codebase with minimal conditional logic
- **Backward Compatible**: Existing SLF4J 1.7 users can continue without changes

### Negative
- **Build Complexity**: Requires testing with multiple profiles in CI/CD
- **Maintenance Overhead**: Need to ensure compatibility with both APIs
- **Documentation**: Need to clearly document which profile to use

### Neutral
- **Profile Selection**: Users must explicitly choose the correct profile for their SLF4J version
- **Service Provider Discovery**: Different mechanisms for 1.7 vs 2.0 (both transparent to users)

## Alternatives Considered

### 1. Separate Artifacts
Create two separate artifacts: `slf4j-test-mock-1.7` and `slf4j-test-mock-2.0`

**Rejected because:**
- Duplicates maintenance effort
- Confusing for users
- More complex release process

### 2. Support Only SLF4J 2.0
Drop support for SLF4J 1.7 and only support 2.0+

**Rejected because:**
- Many projects still use SLF4J 1.7
- Would limit library adoption
- Unnecessary breaking change for existing users

### 3. Multi-Release JAR (Java 9+)
Use Java 9 Multi-Release JARs to handle version differences

**Rejected because:**
- More complex build process
- Harder to debug
- Maven profile approach is simpler and more explicit

## Implementation Notes

### Key Files
- `MockServiceProvider.java`: SLF4J 2.0 service provider (excluded in 1.7 profile)
- `MockMDCAdapter.java`: Implements both 1.7 and 2.0 MDC methods
- `META-INF/services/org.slf4j.spi.SLF4JServiceProvider`: Service registration for 2.0
- `pom.xml`: Profile definitions

### Testing Strategy
- CI pipeline must run tests with both profiles
- All 318 tests pass with both SLF4J 1.7.36 and 2.0.16

### CI/CD Implementation

**Build and Test Workflow (`maven-build-test.yml`):**
- Uses matrix strategy to test with both `slf4j-1.7` and `slf4j-2.0` profiles
- Runs in parallel for faster feedback
- Coverage reports uploaded only from SLF4J 1.7 build to avoid duplication
- Fails if either version fails (`fail-fast: false` for better visibility)

**Release Workflow (`release-deploy-version.yml`):**
- Runs compatibility tests with both versions before deployment
- Uses `needs: test-compatibility` to ensure tests pass first
- Deploys artifact built with SLF4J 1.7 profile (default)
- Uses `fail-fast: true` to abort release if any version fails

This ensures:
1. Every PR and push is validated against both SLF4J versions
2. Releases only happen if both versions pass all tests
3. The published artifact is compatible with both versions

## References
- [SLF4J 2.0 Migration Guide](https://www.slf4j.org/faq.html#changesInVersion200)
- [SLF4J Service Provider Interface](https://www.slf4j.org/manual.html#swapping)
- ADR-0003: Focus on SLF4J Facade

## Date
2025-12-07

