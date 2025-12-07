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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MockMDCAdapter}.
 *
 * @author Daniel Felix Ferber
 */
@DisplayName("MockMDCAdapter Tests")
class MockMDCAdapterTest {

    private MockMDCAdapter mdcAdapter;

    @BeforeEach
    void setUp() {
        mdcAdapter = new MockMDCAdapter();
        // Clear ThreadLocal to prevent data leakage between tests
        // This is necessary because the ThreadLocal is static
        mdcAdapter.clearAll();
    }

    @Test
    @DisplayName("Should put and get values correctly")
    void shouldPutAndGetValuesCorrectly() {
        // When
        mdcAdapter.put("key1", "value1");
        mdcAdapter.put("key2", "value2");
        
        // Then
        assertEquals("value1", mdcAdapter.get("key1"));
        assertEquals("value2", mdcAdapter.get("key2"));
    }

    @Test
    @DisplayName("Should return null for non-existent key")
    void shouldReturnNullForNonExistentKey() {
        // When/Then
        assertNull(mdcAdapter.get("nonexistent"));
    }

    @Test
    @DisplayName("Should handle null key and value")
    void shouldHandleNullKeyAndValue() {
        // When
        mdcAdapter.put(null, "value");
        mdcAdapter.put("key", null);
        
        // Then
        assertEquals("value", mdcAdapter.get(null));
        assertNull(mdcAdapter.get("key"));
    }

    @Test
    @DisplayName("Should overwrite existing values")
    void shouldOverwriteExistingValues() {
        // Given
        mdcAdapter.put("key", "oldValue");
        
        // When
        mdcAdapter.put("key", "newValue");
        
        // Then
        assertEquals("newValue", mdcAdapter.get("key"));
    }

    @Test
    @DisplayName("Should remove values correctly")
    void shouldRemoveValuesCorrectly() {
        // Given
        mdcAdapter.put("key1", "value1");
        mdcAdapter.put("key2", "value2");
        
        // When
        mdcAdapter.remove("key1");
        
        // Then
        assertNull(mdcAdapter.get("key1"));
        assertEquals("value2", mdcAdapter.get("key2"));
    }

    @Test
    @DisplayName("Should handle removal of non-existent key gracefully")
    void shouldHandleRemovalOfNonExistentKeyGracefully() {
        // When/Then
        assertDoesNotThrow(() -> mdcAdapter.remove("nonexistent"));
        assertNull(mdcAdapter.get("nonexistent"));
    }

    @Test
    @DisplayName("Should clear all values")
    void shouldClearAllValues() {
        // Given
        mdcAdapter.put("key1", "value1");
        mdcAdapter.put("key2", "value2");
        mdcAdapter.put("key3", "value3");
        
        // When
        mdcAdapter.clear();
        
        // Then
        assertNull(mdcAdapter.get("key1"));
        assertNull(mdcAdapter.get("key2"));
        assertNull(mdcAdapter.get("key3"));
    }

    @Test
    @DisplayName("Should return copy of context map")
    void shouldReturnCopyOfContextMap() {
        // Given
        mdcAdapter.put("key1", "value1");
        mdcAdapter.put("key2", "value2");
        
        // When
        Map<String, String> contextMap = mdcAdapter.getCopyOfContextMap();
        
        // Then
        assertNotNull(contextMap);
        assertEquals(2, contextMap.size());
        assertEquals("value1", contextMap.get("key1"));
        assertEquals("value2", contextMap.get("key2"));
        
        // Verify it's a copy (modifications don't affect original)
        contextMap.put("key3", "value3");
        assertNull(mdcAdapter.get("key3"));
    }

    @Test
    @DisplayName("Should return empty map when no context")
    void shouldReturnEmptyMapWhenNoContext() {
        // When
        Map<String, String> contextMap = mdcAdapter.getCopyOfContextMap();
        
        // Then
        assertNotNull(contextMap);
        assertTrue(contextMap.isEmpty());
    }

    @Test
    @DisplayName("Should set context map correctly")
    void shouldSetContextMapCorrectly() {
        // Given
        Map<String, String> newContext = new HashMap<>();
        newContext.put("newKey1", "newValue1");
        newContext.put("newKey2", "newValue2");
        
        mdcAdapter.put("oldKey", "oldValue");
        
        // When
        mdcAdapter.setContextMap(newContext);
        
        // Then
        assertEquals("newValue1", mdcAdapter.get("newKey1"));
        assertEquals("newValue2", mdcAdapter.get("newKey2"));
        assertNull(mdcAdapter.get("oldKey")); // Old context should be cleared
    }

    @Test
    @DisplayName("Should handle null context map in setContextMap")
    void shouldHandleNullContextMapInSetContextMap() {
        // Given
        mdcAdapter.put("key", "value");
        
        // When/Then
        assertThrows(NullPointerException.class, () -> mdcAdapter.setContextMap(null));
    }

    @Test
    @DisplayName("Should handle empty context map in setContextMap")
    void shouldHandleEmptyContextMapInSetContextMap() {
        // Given
        Map<String, String> emptyContext = new HashMap<>();
        mdcAdapter.put("key", "value");
        
        // When
        mdcAdapter.setContextMap(emptyContext);
        
        // Then
        assertNull(mdcAdapter.get("key"));
        assertTrue(mdcAdapter.getCopyOfContextMap().isEmpty());
    }

    @Test
    @DisplayName("Should isolate context between threads")
    void shouldIsolateContextBetweenThreads() throws InterruptedException {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        String[] thread1Result = new String[1];
        String[] thread2Result = new String[1];
        
        // When
        executor.submit(() -> {
            try {
                mdcAdapter.put("threadKey", "thread1Value");
                Thread.sleep(100); // Allow other thread to potentially interfere
                thread1Result[0] = mdcAdapter.get("threadKey");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });
        
        executor.submit(() -> {
            try {
                mdcAdapter.put("threadKey", "thread2Value");
                Thread.sleep(100); // Allow other thread to potentially interfere
                thread2Result[0] = mdcAdapter.get("threadKey");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });
        
        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
        
        assertEquals("thread1Value", thread1Result[0]);
        assertEquals("thread2Value", thread2Result[0]);
    }

    @Test
    @DisplayName("Should preserve context map modifications")
    void shouldPreserveContextMapModifications() {
        // Given
        Map<String, String> originalContext = new HashMap<>();
        originalContext.put("key1", "value1");
        originalContext.put("key2", "value2");
        
        mdcAdapter.setContextMap(originalContext);
        
        // When
        originalContext.put("key3", "value3"); // Modify original map
        
        // Then
        assertNull(mdcAdapter.get("key3")); // Should not affect MDC
        assertEquals("value1", mdcAdapter.get("key1"));
        assertEquals("value2", mdcAdapter.get("key2"));
    }

    @Test
    @DisplayName("Should handle large number of key-value pairs")
    void shouldHandleLargeNumberOfKeyValuePairs() {
        // Given
        int numEntries = 1000;
        
        // When
        for (int i = 0; i < numEntries; i++) {
            mdcAdapter.put("key" + i, "value" + i);
        }
        
        // Then
        for (int i = 0; i < numEntries; i++) {
            assertEquals("value" + i, mdcAdapter.get("key" + i));
        }
        
        Map<String, String> contextMap = mdcAdapter.getCopyOfContextMap();
        assertEquals(numEntries, contextMap.size());
    }

    @Test
    @DisplayName("Should handle special characters in keys and values")
    void shouldHandleSpecialCharactersInKeysAndValues() {
        // Given
        String specialKey = "key.with-special_chars@123";
        String specialValue = "value with spaces, symbols !@#$%^&*()";
        
        // When
        mdcAdapter.put(specialKey, specialValue);
        
        // Then
        assertEquals(specialValue, mdcAdapter.get(specialKey));
    }

    @Test
    @DisplayName("Should clear ThreadLocal with clearAll")
    void shouldClearThreadLocalWithClearAll() {
        // Given
        mdcAdapter.put("key1", "value1");
        mdcAdapter.put("key2", "value2");
        assertEquals("value1", mdcAdapter.get("key1"));

        // When
        mdcAdapter.clearAll();

        // Then
        // After clearAll, the ThreadLocal is removed, so getCopyOfContextMap should return an empty map
        Map<String, String> contextMap = mdcAdapter.getCopyOfContextMap();
        assertTrue(contextMap.isEmpty());
        assertNull(mdcAdapter.get("key1"));
        assertNull(mdcAdapter.get("key2"));
    }

    @Test
    @DisplayName("Should allow reuse after clearAll")
    void shouldAllowReuseAfterClearAll() {
        // Given
        mdcAdapter.put("key1", "value1");
        mdcAdapter.clearAll();

        // When
        mdcAdapter.put("key2", "value2");

        // Then
        assertNull(mdcAdapter.get("key1"));
        assertEquals("value2", mdcAdapter.get("key2"));
    }

    // SLF4J 2.0 Deque Support Tests

    @Test
    @DisplayName("Should push and pop values from deque")
    void shouldPushAndPopValuesFromDeque() {
        // When
        mdcAdapter.pushByKey("key1", "value1");
        mdcAdapter.pushByKey("key1", "value2");
        mdcAdapter.pushByKey("key1", "value3");

        // Then
        assertEquals("value3", mdcAdapter.popByKey("key1"), "should pop last pushed value");
        assertEquals("value2", mdcAdapter.popByKey("key1"), "should pop second value");
        assertEquals("value1", mdcAdapter.popByKey("key1"), "should pop first value");
    }

    @Test
    @DisplayName("Should return null when popping from empty deque")
    void shouldReturnNullWhenPoppingFromEmptyDeque() {
        // When/Then
        assertNull(mdcAdapter.popByKey("nonexistent"), "should return null for non-existent key");

        // Given
        mdcAdapter.pushByKey("key1", "value1");
        mdcAdapter.popByKey("key1");

        // Then
        assertNull(mdcAdapter.popByKey("key1"), "should return null when deque is empty");
    }

    @Test
    @DisplayName("Should get copy of deque by key")
    void shouldGetCopyOfDequeByKey() {
        // Given
        mdcAdapter.pushByKey("key1", "value1");
        mdcAdapter.pushByKey("key1", "value2");
        mdcAdapter.pushByKey("key1", "value3");

        // When
        final Deque<String> deque = mdcAdapter.getCopyOfDequeByKey("key1");

        // Then
        assertNotNull(deque, "should return deque for existing key");
        assertEquals(3, deque.size(), "should contain all pushed values");
        assertEquals("value3", deque.pop(), "should return values in LIFO order");
        assertEquals("value2", deque.pop(), "should return values in LIFO order");
        assertEquals("value1", deque.pop(), "should return values in LIFO order");

        // Verify it's a copy (modifications don't affect original)
        mdcAdapter.pushByKey("key1", "value4");
        assertEquals("value4", mdcAdapter.popByKey("key1"), "should have new value in original");
    }

    @Test
    @DisplayName("Should return null for non-existent deque")
    void shouldReturnNullForNonExistentDeque() {
        // When/Then
        assertNull(mdcAdapter.getCopyOfDequeByKey("nonexistent"), "should return null for non-existent key");
    }

    @Test
    @DisplayName("Should clear deque by key")
    void shouldClearDequeByKey() {
        // Given
        mdcAdapter.pushByKey("key1", "value1");
        mdcAdapter.pushByKey("key1", "value2");
        mdcAdapter.pushByKey("key2", "value3");

        // When
        mdcAdapter.clearDequeByKey("key1");

        // Then
        assertNull(mdcAdapter.popByKey("key1"), "should return null after clearing deque");
        assertEquals("value3", mdcAdapter.popByKey("key2"), "should not affect other deques");
    }

    @Test
    @DisplayName("Should handle clearing non-existent deque gracefully")
    void shouldHandleClearingNonExistentDequeGracefully() {
        // When/Then
        assertDoesNotThrow(() -> mdcAdapter.clearDequeByKey("nonexistent"), "should not throw exception");
    }

    @Test
    @DisplayName("Should handle multiple deques independently")
    void shouldHandleMultipleDequesIndependently() {
        // Given
        mdcAdapter.pushByKey("key1", "value1a");
        mdcAdapter.pushByKey("key1", "value1b");
        mdcAdapter.pushByKey("key2", "value2a");
        mdcAdapter.pushByKey("key2", "value2b");
        mdcAdapter.pushByKey("key3", "value3a");

        // When/Then
        assertEquals("value1b", mdcAdapter.popByKey("key1"), "should pop from key1");
        assertEquals("value2b", mdcAdapter.popByKey("key2"), "should pop from key2");
        assertEquals("value3a", mdcAdapter.popByKey("key3"), "should pop from key3");
        assertEquals("value1a", mdcAdapter.popByKey("key1"), "should pop remaining from key1");
        assertEquals("value2a", mdcAdapter.popByKey("key2"), "should pop remaining from key2");
    }

    @Test
    @DisplayName("Should handle empty string values in deque push")
    void shouldHandleEmptyStringValuesInDequePush() {
        // When - ArrayDeque does not support null values, but empty strings are fine
        mdcAdapter.pushByKey("key1", "");
        mdcAdapter.pushByKey("key1", "value1");

        // Then
        assertEquals("value1", mdcAdapter.popByKey("key1"), "should pop non-empty value");
        assertEquals("", mdcAdapter.popByKey("key1"), "should pop empty string value");
    }

    @Test
    @DisplayName("Should handle empty key in deque operations")
    void shouldHandleEmptyKeyInDequeOperations() {
        // When - Using empty string instead of null (HashMap/ArrayDeque don't support null keys)
        mdcAdapter.pushByKey("", "value1");
        mdcAdapter.pushByKey("", "value2");

        // Then
        assertEquals("value2", mdcAdapter.popByKey(""), "should handle empty key");
        assertEquals("value1", mdcAdapter.popByKey(""), "should handle empty key");
    }

    @Test
    @DisplayName("Should isolate deques between threads")
    void shouldIsolateDequessBetweenThreads() throws InterruptedException {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        String[] thread1Result = new String[1];
        String[] thread2Result = new String[1];

        // When
        executor.submit(() -> {
            try {
                mdcAdapter.pushByKey("threadKey", "thread1Value1");
                mdcAdapter.pushByKey("threadKey", "thread1Value2");
                Thread.sleep(100);
                thread1Result[0] = mdcAdapter.popByKey("threadKey");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                mdcAdapter.pushByKey("threadKey", "thread2Value1");
                mdcAdapter.pushByKey("threadKey", "thread2Value2");
                Thread.sleep(100);
                thread2Result[0] = mdcAdapter.popByKey("threadKey");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS), "should complete within timeout");
        executor.shutdown();

        assertEquals("thread1Value2", thread1Result[0], "should isolate thread 1 deque");
        assertEquals("thread2Value2", thread2Result[0], "should isolate thread 2 deque");
    }

    @Test
    @DisplayName("Should clear both context map and deques with clearAll")
    void shouldClearBothContextMapAndDequesWithClearAll() {
        // Given
        mdcAdapter.put("key1", "value1");
        mdcAdapter.pushByKey("dequeKey", "dequeValue1");
        mdcAdapter.pushByKey("dequeKey", "dequeValue2");

        // When
        mdcAdapter.clearAll();

        // Then
        assertNull(mdcAdapter.get("key1"), "should clear context map");
        assertNull(mdcAdapter.popByKey("dequeKey"), "should clear deques");
        assertTrue(mdcAdapter.getCopyOfContextMap().isEmpty(), "should have empty context map");
    }

    @Test
    @DisplayName("Should allow pushing multiple values with same content")
    void shouldAllowPushingMultipleValuesWithSameContent() {
        // Given
        String sameValue = "sameValue";

        // When
        mdcAdapter.pushByKey("key1", sameValue);
        mdcAdapter.pushByKey("key1", sameValue);
        mdcAdapter.pushByKey("key1", sameValue);

        // Then
        assertEquals(sameValue, mdcAdapter.popByKey("key1"), "should pop first duplicate");
        assertEquals(sameValue, mdcAdapter.popByKey("key1"), "should pop second duplicate");
        assertEquals(sameValue, mdcAdapter.popByKey("key1"), "should pop third duplicate");
        assertNull(mdcAdapter.popByKey("key1"), "should be empty after popping all");
    }

    @Test
    @DisplayName("Should maintain deque order correctly")
    void shouldMaintainDequeOrderCorrectly() {
        // Given
        int numValues = 100;

        // When
        for (int i = 0; i < numValues; i++) {
            mdcAdapter.pushByKey("key1", "value" + i);
        }

        // Then
        for (int i = numValues - 1; i >= 0; i--) {
            assertEquals("value" + i, mdcAdapter.popByKey("key1"),
                "should maintain LIFO order for value" + i);
        }
        assertNull(mdcAdapter.popByKey("key1"), "should be empty after popping all values");
    }

    @Test
    @DisplayName("Should handle mixed operations on deque")
    void shouldHandleMixedOperationsOnDeque() {
        // When/Then
        mdcAdapter.pushByKey("key1", "value1");
        assertEquals("value1", mdcAdapter.popByKey("key1"), "should pop value1");

        mdcAdapter.pushByKey("key1", "value2");
        mdcAdapter.pushByKey("key1", "value3");
        assertEquals("value3", mdcAdapter.popByKey("key1"), "should pop value3");

        mdcAdapter.pushByKey("key1", "value4");
        final Deque<String> deque = mdcAdapter.getCopyOfDequeByKey("key1");
        assertNotNull(deque, "should get deque copy");
        assertEquals(2, deque.size(), "should have 2 remaining values");

        mdcAdapter.clearDequeByKey("key1");
        assertNull(mdcAdapter.popByKey("key1"), "should be empty after clear");
    }
}