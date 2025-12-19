# TDR-0005: Support for Multiple SLF4J API Versions

**Status**: Accepted

**Date**: 2025-12-07

## Context

SLF4J has two major API versions in widespread use:
- **SLF4J 1.7.x**: The traditional version, with the last release being 1.7.36
- **SLF4J 2.0.x**: The modern version with breaking changes and new features

Projects using different SLF4J versions need a compatible mock implementation for testing. Supporting both versions allows the library to be useful for a wider range of projects, including legacy systems on 1.7.x and modern applications on 2.0.x.

## Decision

We will support both SLF4J 1.7.x and 2.0.x through Maven profiles:

1. **Maven Profiles**: Two profiles (`slf4j-1.7` and `slf4j-2.0`) control the SLF4J API dependency version
2. **Conditional Compilation**: The `MockServiceProvider` class (required for SLF4J 2.0) is excluded from compilation in the 1.7 profile
3. **Dual Service Provider Mechanism**:
   - SLF4J 1.7: Uses `StaticLoggerBinder`, `StaticMarkerBinder`, `StaticMDCBinder`
   - SLF4J 2.0: Uses `MockServiceProvider` implementing `SLF4JServiceProvider`
4. **API Compatibility**: MDCAdapter implements all methods including SLF4J 2.0 Deque methods, but without `@Override` annotations to maintain 1.7 compatibility

## Consequences

### Positive

*   **Wider Adoption**: Library can be used with both SLF4J 1.7 and 2.0 projects.
*   **Future-Proof**: Easy to add support for future SLF4J versions.
*   **No Code Duplication**: Single codebase with minimal conditional logic.
*   **Backward Compatible**: Existing SLF4J 1.7 users can continue without changes.

### Negative

*   **Build Complexity**: Requires testing with multiple profiles in CI/CD.
*   **Maintenance Overhead**: Need to ensure compatibility with both APIs.
*   **Documentation**: Need to clearly document which profile to use.

### Neutral

*   **Profile Selection**: Users must explicitly choose the correct profile for their SLF4J version.
*   **Service Provider Discovery**: Different mechanisms for 1.7 vs 2.0 (both transparent to users).

## Alternatives Considered

### 1. Separate Artifacts
Create two separate artifacts: `slf4j-test-mock-1.7` and `slf4j-test-mock-2.0`

*   **Description**: Maintain two independent JAR files, each optimized for its respective SLF4J version.
*   **Rejected because**: Duplicates maintenance effort, confusing for users, and complicates the release process.

### 2. Support Only SLF4J 2.0
Drop support for SLF4J 1.7 and only support 2.0+

*   **Description**: Focus exclusively on modern SLF4J 2.0 for new development.
*   **Rejected because**: Many projects still use SLF4J 1.7, would limit library adoption, and constitutes an unnecessary breaking change.

### 3. Multi-Release JAR (Java 9+)
Use Java 9 Multi-Release JARs to handle version differences

*   **Description**: Bundle different implementations for different Java versions in a single JAR.
*   **Rejected because**: More complex build process, harder to debug, and Maven profile approach is simpler and more explicit.

