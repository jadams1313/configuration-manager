package com.github.configmanger;

import com.github.configmanager.ConfigManager;
import com.github.configmanager.annotations.ConfigValue;
import com.github.configmanager.config.BasicConfiguration;
import com.github.configmanager.config.CustomConfiguration;
import com.github.configmanager.listeners.ConfigurationChangeListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ConfigManager functionality.
 */
class ConfigManagerTest {

    @BeforeEach
    void setUp() {
        // Reset to basic configuration before each test
        ConfigManager.setConfiguration(
                ConfigManager.createBasicConfiguration(new ConcurrentHashMap<>())
        );
        ConfigManager.clearConfigurationChangeListeners();
    }

    @Test
    void testBasicConfiguration() {
        Map<String, String> initial = Map.of(
                "key1", "value1",
                "key2", "value2"
        );

        BasicConfiguration config = ConfigManager.createBasicConfiguration(initial);
        ConfigManager.setConfiguration(config);

        assertEquals("value1", ConfigManager.getConfigValue("key1"));
        assertEquals("value2", ConfigManager.getConfigValue("key2"));
    }

    @Test
    void testCustomConfiguration() {
        CustomConfiguration config = ConfigManager.createCustomConfiguration(TestConfig.class);
        ConfigManager.setConfiguration(config);

        assertEquals("localhost", ConfigManager.getConfigValue("test_host"));
        assertEquals("8080", ConfigManager.getConfigValue("test_port"));
    }

    @Test
    void testGetConfigValueWithDefault() {
        assertEquals("default", ConfigManager.getConfigValue("nonexistent", "default"));
        assertNull(ConfigManager.getConfigValue("nonexistent"));
    }

    @Test
    void testTypedConfigValues() {
        ConfigManager.alterConfiguration()
                .setConfigValue("int_value", "42")
                .setConfigValue("bool_value", "true")
                .setConfigValue("long_value", "999999")
                .apply()
                .join();

        assertEquals(42, ConfigManager.getConfigValue("int_value", Integer.class, 0));
        assertEquals(true, ConfigManager.getConfigValue("bool_value", Boolean.class, false));
        assertEquals(999999L, ConfigManager.getConfigValue("long_value", Long.class, 0L));
    }

    @Test
    void testTypedConfigValuesWithInvalidData() {
        ConfigManager.alterConfiguration()
                .setConfigValue("invalid_int", "not_a_number")
                .apply()
                .join();

        // Should return default value when parsing fails
        assertEquals(100, ConfigManager.getConfigValue("invalid_int", Integer.class, 100));
    }

    @Test
    void testAsyncSingleValueUpdate() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        ConfigManager.alterConfigurationAsync("async_key", "async_value")
                .thenRun(latch::countDown);

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("async_value", ConfigManager.getConfigValue("async_key"));
    }

    @Test
    void testAsyncBatchUpdate() throws Exception {
        Map<String, String> updates = Map.of(
                "batch1", "value1",
                "batch2", "value2",
                "batch3", "value3"
        );

        CountDownLatch latch = new CountDownLatch(1);

        ConfigManager.alterConfigurationAsync(updates)
                .thenRun(latch::countDown);

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("value1", ConfigManager.getConfigValue("batch1"));
        assertEquals("value2", ConfigManager.getConfigValue("batch2"));
        assertEquals("value3", ConfigManager.getConfigValue("batch3"));
    }

    @Test
    void testConfigurationChangeListener() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        ConfigurationChangeListener listener = changes -> {
            callCount.incrementAndGet();
            assertEquals(2, changes.size());
            assertTrue(changes.containsKey("listener_key1"));
            assertTrue(changes.containsKey("listener_key2"));
            latch.countDown();
        };

        ConfigManager.addConfigurationChangeListener(listener);

        ConfigManager.alterConfiguration()
                .setConfigValue("listener_key1", "value1")
                .setConfigValue("listener_key2", "value2")
                .apply();

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(1, callCount.get());

        ConfigManager.removeConfigurationChangeListener(listener);
    }

    @Test
    void testMultipleListeners() throws Exception {
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);

        ConfigurationChangeListener listener1 = changes -> latch1.countDown();
        ConfigurationChangeListener listener2 = changes -> latch2.countDown();

        ConfigManager.addConfigurationChangeListener(listener1);
        ConfigManager.addConfigurationChangeListener(listener2);

        assertEquals(2, ConfigManager.getListenerCount());

        ConfigManager.alterConfigurationAsync("multi_key", "multi_value");

        assertTrue(latch1.await(5, TimeUnit.SECONDS));
        assertTrue(latch2.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testHasConfigValue() {
        ConfigManager.alterConfiguration()
                .setConfigValue("exists", "value")
                .apply()
                .join();

        assertTrue(ConfigManager.hasConfigValue("exists"));
        assertFalse(ConfigManager.hasConfigValue("does_not_exist"));
        assertFalse(ConfigManager.hasConfigValue(null));
        assertFalse(ConfigManager.hasConfigValue(""));
    }

    @Test
    void testGetConfigurationSnapshot() {
        ConfigManager.alterConfiguration()
                .setConfigValue("snap1", "value1")
                .setConfigValue("snap2", "value2")
                .apply()
                .join();

        Map<String, String> snapshot = ConfigManager.getConfigurationSnapshot();

        assertTrue(snapshot.containsKey("snap1"));

        // Snapshot should be immutable
        assertThrows(UnsupportedOperationException.class, () ->
                snapshot.put("new_key", "new_value")
        );
    }

    @Test
    void testConcurrentModifications() throws Exception {
        int threadCount = 10;
        int operationsPerThread = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        ConfigManager.alterConfigurationAsync(
                                "thread_" + threadId + "_key_" + j,
                                "value_" + j
                        );
                    }
                    endLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(endLatch.await(10, TimeUnit.SECONDS));

        // Give async operations time to complete
        Thread.sleep(500);

        // Verify some values were set
        assertTrue(ConfigManager.getConfigurationSize() > 0);
    }

    @Test
    void testNullValueHandling() {
        assertThrows(IllegalArgumentException.class, () -> {
            ConfigManager.alterConfiguration()
                    .setConfigValue("null_key", null)
                    .apply()
                    .join();
        });
    }

    @Test
    void testInvalidOperations() {
        assertThrows(IllegalArgumentException.class, () ->
                ConfigManager.setConfiguration(null)
        );

        assertThrows(IllegalArgumentException.class, () ->
                ConfigManager.createCustomConfiguration((Class<?>[]) null)
        );
    }

    // Test configuration class
    static class TestConfig {
        @ConfigValue(defaultValue = "localhost")
        private String testHost;

        @ConfigValue(defaultValue = "8080")
        private String testPort;
    }
}