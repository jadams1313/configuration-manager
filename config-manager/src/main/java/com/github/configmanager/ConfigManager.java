package com.github.configmanager;

import com.github.configmanager.config.Configuration;
import com.github.configmanager.config.BasicConfiguration;
import com.github.configmanager.config.CustomConfiguration;
import com.github.configmanager.listeners.ConfigurationChangeListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ConfigManager {
    private static final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r);
        thread.setName("ConfigManager-Worker-" + thread.getId());
        thread.setDaemon(true);
        return thread;
    });

    private static Configuration currentConfiguration;
    private static volatile boolean isShutdown = false;
    private static final List<ConfigurationChangeListener> listeners = new CopyOnWriteArrayList<>();

    static {
        // Default to basic configuration
        currentConfiguration = new BasicConfiguration(new ConcurrentHashMap<>());

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();
        }));
    }

    /**
     * Get a snapshot of the current configuration.
     */
    public static Map<String, String> getConfigurationMap() {
        return currentConfiguration.toMap();
    }

    /**
     * Get an immutable snapshot of the current configuration.
     */
    public static Map<String, String> getConfigurationSnapshot() {
        return Map.copyOf(getConfigurationMap());
    }

    /**
     * Create a configuration alteration builder for batch updates.
     * Changes are applied asynchronously when apply() is called.
     */
    public static ConfigurationAlteration alterConfiguration() {
        checkNotShutdown();
        if (currentConfiguration instanceof CustomConfiguration) {
            return new ConfigurationAlteration(
                    ((CustomConfiguration) currentConfiguration).getConfigMap(),
                    executor,
                    listeners
            );
        } else if (currentConfiguration instanceof BasicConfiguration) {
            return new ConfigurationAlteration(
                    ((BasicConfiguration) currentConfiguration).getConfigMap(),
                    executor,
                    listeners
            );
        }
        throw new IllegalStateException("Unknown configuration type");
    }

    /**
     * Asynchronously set a single configuration value.
     */
    public static CompletableFuture<Void> alterConfigurationAsync(String key, String value) {
        checkNotShutdown();
        if (key == null || key.isEmpty()) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Config key cannot be null or empty")
            );
        }
        return alterConfiguration()
                .setConfigValue(key, value)
                .apply();
    }

    /**
     * Asynchronously apply a batch of configuration changes.
     */
    public static CompletableFuture<Void> alterConfigurationAsync(Map<String, String> changes) {
        checkNotShutdown();
        if (changes == null || changes.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        ConfigurationAlteration alteration = alterConfiguration();
        changes.forEach(alteration::setConfigValue);
        return alteration.apply();
    }

    /**
     * Set the current configuration implementation.
     */
    public static void setConfiguration(Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        currentConfiguration = configuration;
    }

    /**
     * Get the current configuration implementation.
     */
    public static Configuration getCurrentConfiguration() {
        return currentConfiguration;
    }

    /**
     * Get a configuration value by key.
     */
    public static String getConfigValue(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        return getConfigurationMap().get(key);
    }

    /**
     * Get a configuration value by key with a default fallback.
     */
    public static String getConfigValue(String key, String defaultValue) {
        if (key == null || key.isEmpty()) {
            return defaultValue;
        }
        return getConfigurationMap().getOrDefault(key, defaultValue);
    }

    /**
     * Get a typed configuration value with automatic conversion.
     * Supports: String, Integer, Long, Double, Float, Boolean
     */
    @SuppressWarnings("unchecked")
    public static <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
        if (key == null || key.isEmpty() || type == null) {
            return defaultValue;
        }

        String value = getConfigValue(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            if (type == String.class) {
                return (T) value;
            } else if (type == Integer.class) {
                return (T) Integer.valueOf(value);
            } else if (type == Long.class) {
                return (T) Long.valueOf(value);
            } else if (type == Double.class) {
                return (T) Double.valueOf(value);
            } else if (type == Float.class) {
                return (T) Float.valueOf(value);
            } else if (type == Boolean.class) {
                return (T) Boolean.valueOf(value);
            } else {
                return defaultValue;
            }
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Refresh the configuration (only works with CustomConfiguration).
     */
    public static void refresh() {
        if (currentConfiguration instanceof CustomConfiguration) {
            ((CustomConfiguration) currentConfiguration).refresh();
        }
    }

    /**
     * Check if a configuration key exists.
     */
    public static boolean hasConfigValue(String key) {
        return key != null && !key.isEmpty() && getConfigurationMap().containsKey(key);
    }

    /**
     * Get the size of the current configuration.
     */
    public static int getConfigurationSize() {
        return getConfigurationMap().size();
    }

    // Convenience factory methods

    /**
     * Create a custom configuration with annotation scanning.
     */
    public static CustomConfiguration createCustomConfiguration(Class<?>... configClasses) {
        return new CustomConfiguration(configClasses);
    }

    /**
     * Create a custom configuration with initial values and annotation scanning.
     */
    public static CustomConfiguration createCustomConfiguration(
            Map<String, String> initialMap,
            Class<?>... configClasses) {
        return new CustomConfiguration(initialMap, configClasses);
    }

    /**
     * Create a basic configuration with initial values.
     */
    public static BasicConfiguration createBasicConfiguration(Map<String, String> initialMap) {
        return new BasicConfiguration(initialMap);
    }

    /**
     * Add a configuration change listener.
     */
    public static void addConfigurationChangeListener(ConfigurationChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a configuration change listener.
     */
    public static void removeConfigurationChangeListener(ConfigurationChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Clear all configuration change listeners.
     */
    public static void clearConfigurationChangeListeners() {
        listeners.clear();
    }

    /**
     * Get the number of registered listeners.
     */
    public static int getListenerCount() {
        return listeners.size();
    }

    /**
     * Shutdown the ConfigManager and release resources.
     */
    public static void shutdown() {
        if (isShutdown) {
            return;
        }

        isShutdown = true;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if the ConfigManager has been shutdown.
     */
    public static boolean isShutdown() {
        return isShutdown;
    }

    private static void checkNotShutdown() {
        if (isShutdown) {
            throw new IllegalStateException("ConfigManager has been shutdown");
        }
    }
}