package com.github.configmanager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationAlteration {
    private final Map<String, String> pendingChanges = new ConcurrentHashMap<>();
    private final Map<String, String> targetConfigMap;

    public ConfigurationAlteration(Map<String, String> targetConfigMap) {
        this.targetConfigMap = targetConfigMap;
    }

    public ConfigurationAlteration setConfigValue(String key, String value) {
        pendingChanges.put(key, value);
        return this;
    }

    public ConfigurationAlteration setConfigValue(String key, Object value) {
        return setConfigValue(key, value != null ? value.toString() : null);
    }

    public void apply() {
        targetConfigMap.putAll(pendingChanges);
        // Also update system properties for compatibility
        pendingChanges.forEach(System::setProperty);
    }

    public Map<String, String> getPendingChanges() {
        return new ConcurrentHashMap<>(pendingChanges);
    }
}