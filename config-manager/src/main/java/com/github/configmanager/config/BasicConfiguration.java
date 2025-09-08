package com.github.configmanager.config;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class BasicConfiguration implements Configuration {
    private Map<String, String> configMap;

    public BasicConfiguration(final Map<String, String> configMap) {
        this.configMap = configMap;
    }

    @Override
    public Map<String, String> toMap() {
        configMap.putAll(this.getProperties());
        configMap.putAll(this.getEnv());
        return configMap;
    }


}
