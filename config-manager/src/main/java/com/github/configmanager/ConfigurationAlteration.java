package com.github.configmanager;

import com.github.configmanager.listeners.ConfigurationChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class ConfigurationAlteration {
    private final Map<String, String> pendingChanges = new ConcurrentHashMap<>();
    private final Map<String, String> targetConfigMap;
    private final ExecutorService executor;
    private final List<ConfigurationChangeListener> listeners;

    public ConfigurationAlteration(
            Map<String, String> targetConfigMap,
            ExecutorService executor,
            List<ConfigurationChangeListener> listeners) {
        if (targetConfigMap == null) {
            throw new IllegalArgumentException("Target config map cannot be null");
        }
        if (executor == null) {
            throw new IllegalArgumentException("Executor service cannot be null");
        }
        this.targetConfigMap = targetConfigMap;
        this.executor = executor;
        this.listeners = listeners != null ? new ArrayList<>(listeners) : new ArrayList<>();
    }

    public ConfigurationAlteration setConfigValue(String key, String value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Config key cannot be null or empty");
        }
        pendingChanges.put(key, value);
        return this;
    }

    public ConfigurationAlteration setConfigValue(String key, Object value) {
        return setConfigValue(key, value != null ? value.toString() : null);
    }

    /**
     * Apply all pending changes asynchronously.
     * Returns a CompletableFuture that completes when all changes are applied.
     */
    public CompletableFuture<Void> apply() {
        return CompletableFuture.runAsync(() -> {
            try {
                // Create a copy of changes for listeners
                Map<String, String> appliedChanges = new ConcurrentHashMap<>(pendingChanges);

                targetConfigMap.putAll(pendingChanges);
                // Also update system properties for compatibility
                pendingChanges.forEach((key, value) -> {
                    if (value != null) {
                        System.setProperty(key, value);
                    }
                });

                // Notify listeners
                notifyListeners(appliedChanges);

            } catch (Exception e) {
                throw new RuntimeException("Failed to apply configuration changes", e);
            }
        }, executor);
    }

    private void notifyListeners(Map<String, String> changes) {
        if (listeners.isEmpty()) {
            return;
        }

        for (ConfigurationChangeListener listener : listeners) {
            try {
                listener.onConfigurationChanged(changes);
            } catch (Exception e) {
                // Prevent listener errors from breaking the change
            }
        }
    }

    public Map<String, String> getPendingChanges() {
        return new ConcurrentHashMap<>(pendingChanges);
    }

    public void clear() {
        pendingChanges.clear();
    }
}