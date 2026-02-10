package com.github.configmanager.config;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public interface Configuration {

    Map<String, String> toMap();

    default Map<String, String> getProperties() {
        final Map<String, String> configMap = new ConcurrentHashMap<>();
        System.getProperties().forEach((key, value) ->
                configMap.put(key.toString(), value.toString())
        );
        return configMap;
    }

    default Map<String, String> getEnv() {
        final Map<String, String> configMap = new ConcurrentHashMap<>(System.getenv());
        return configMap;
    }
}
