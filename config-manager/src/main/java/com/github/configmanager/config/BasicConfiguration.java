package com.github.configmanager.config;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class BasicConfiguration implements Configuration {
    private final Map<String, String> configMap;

    public BasicConfiguration(final Map<String, String> configMap) {
        this.configMap = new ConcurrentHashMap<>(configMap);
    }

    @Override
    public Map<String, String> toMap() {
        // Create a new map to avoid mutating internal state (thread-safety fix)
        Map<String, String> result = new ConcurrentHashMap<>(configMap);
        result.putAll(this.getProperties());
        result.putAll(this.getEnv());
        return result;
    }

    // Added to support ConfigManager.alterConfiguration()
    public Map<String, String> getConfigMap() {
        return configMap;
    }
}
