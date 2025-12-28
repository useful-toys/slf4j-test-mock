/*
 * Copyright 2025 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.usefultoys.slf4jtestmock;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test annotation to enable automatic printing of log events when a test fails.
 * <p>
 * This annotation registers the {@link MockLoggerDebugExtension}, which intercepts
 * test failures (AssertionErrors) and prints all events captured by MockLogger instances
 * to the standard error stream. This is extremely useful for debugging failed tests.
 * </p>
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * @WithMockLogger
 * @WithMockLoggerDebug
 * class MyTestClass {
 *     @Slf4jMock
 *     private Logger logger;
 *
 *     @Test
 *     void testSomething() {
 *         logger.info("This will be printed if assertion fails");
 *         assertEquals(1, 2); // Fails!
 *     }
 * }
 * }</pre>
 *
 * @see MockLoggerDebugExtension
 * @author Daniel Felix Ferber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(MockLoggerDebugExtension.class)
public @interface WithMockLoggerDebug {
}
