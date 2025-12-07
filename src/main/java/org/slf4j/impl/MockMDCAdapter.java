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
package org.slf4j.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.spi.MDCAdapter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * A mock implementation of {@link MDCAdapter} for testing purposes.
 * <p>
 * This adapter provides a thread-local storage for MDC (Mapped Diagnostic Context) data,
 * allowing each thread to maintain its own set of key-value pairs for logging context.
 * <p>
 * The implementation uses {@link ThreadLocal} to ensure thread safety and isolation
 * of MDC data between different threads during test execution.
 * <p>
 * This class is primarily intended for use in unit tests where you need to verify
 * MDC behavior without depending on a full logging implementation.
 *
 * @author Daniel Felix Ferber
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MockMDCAdapter implements MDCAdapter {

    /**
     * Thread-local storage for MDC data. Each thread gets its own map instance.
     */
    private static final ThreadLocal<Map<String, String>> value = ThreadLocal.withInitial(HashMap::new);

    /**
     * Thread-local storage for MDC Deque data. Each thread gets its own map of deques.
     * This is used for SLF4J 2.0 compatibility.
     */
    private static final ThreadLocal<Map<String, Deque<String>>> dequeMap = ThreadLocal.withInitial(HashMap::new);

    @Override
	public void put(final String key, final String val) {
        value.get().put(key, val);
    }

    @Override
	public String get(final String key) {
        return value.get().get(key);
    }

    @Override
	public void remove(final String key) {
        value.get().remove(key);
    }

    @Override
	public void clear() {
        value.get().clear();
    }

    @Override
	public void setContextMap(final Map<String, String> contextMap) {
        value.set(new HashMap<>(contextMap));
    }

    @Override
	public Map<String, String> getCopyOfContextMap() {
        return new HashMap<>(value.get());
    }

    /**
     * Clears the ThreadLocal storage for the current thread.
     * This is useful for test cleanup to prevent data leakage between tests.
     * <p>
     * Note: This method removes the entire ThreadLocal value for the current thread,
     * not just the entries in the map. This helps with memory management and ensures
     * complete isolation between test runs.
     */
    public void clearAll() {
        value.remove();
        dequeMap.remove();
    }

    // SLF4J 2.0 Deque support methods
    // Note: @Override annotations are omitted to maintain compatibility with SLF4J 1.7
    // where these methods don't exist. In SLF4J 2.0, these will properly override.

    /**
     * Push a value onto the deque associated with the given key.
     * This method is part of SLF4J 2.0 API.
     *
     * @param key the key
     * @param val the value to push
     */
    public void pushByKey(final String key, final String val) {
        final Deque<String> deque = dequeMap.get().computeIfAbsent(key, k -> new ArrayDeque<>(4));
        deque.push(val);
    }

    /**
     * Pop a value from the deque associated with the given key.
     * This method is part of SLF4J 2.0 API.
     *
     * @param key the key
     * @return the popped value, or null if the deque is empty or doesn't exist
     */
    public String popByKey(final String key) {
        final Deque<String> deque = dequeMap.get().get(key);
        if (deque == null || deque.isEmpty()) {
            return null;
        }
        return deque.pop();
    }

    /**
     * Get a copy of the deque associated with the given key.
     * This method is part of SLF4J 2.0 API.
     *
     * @param key the key
     * @return a copy of the deque, or null if it doesn't exist
     */
    public Deque<String> getCopyOfDequeByKey(final String key) {
        final Deque<String> deque = dequeMap.get().get(key);
        if (deque == null) {
            return null;
        }
        return new ArrayDeque<>(deque);
    }

    /**
     * Clear the deque associated with the given key.
     * This method is part of SLF4J 2.0 API.
     *
     * @param key the key
     */
    public void clearDequeByKey(final String key) {
        final Deque<String> deque = dequeMap.get().get(key);
        if (deque != null) {
            deque.clear();
        }
    }

}
