package com.github.configmanager.config;

import com.github.configmanager.annotations.ConfigValue;
import com.github.configmanager.annotations.FieldNameMappings;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomConfiguration implements Configuration {
    private final Map<String, String> configMap;
    private final Class<?>[] configClasses;
    private final FieldNameMappings fieldNameMapper = new FieldNameMappings();

    public CustomConfiguration(Class<?>... configClasses) {
        if (configClasses == null || configClasses.length == 0) {
            throw new IllegalArgumentException("At least one configuration class must be provided");
        }
        this.configMap = new ConcurrentHashMap<>();
        this.configClasses = configClasses;
        scanConfigClasses();
    }

    public CustomConfiguration(Map<String, String> initialMap, Class<?>... configClasses) {
        if (configClasses == null || configClasses.length == 0) {
            throw new IllegalArgumentException("At least one configuration class must be provided");
        }
        if (initialMap == null) {
            throw new IllegalArgumentException("Initial map cannot be null");
        }
        this.configMap = new ConcurrentHashMap<>(initialMap);
        this.configClasses = configClasses;
        scanConfigClasses();
    }

    private void scanConfigClasses() {
        for (Class<?> clazz : configClasses) {
            if (clazz != null) {
                scanClass(clazz);
            }
        }
    }

    private void scanClass(Class<?> clazz) {
        try {
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                try {
                    if (field.isAnnotationPresent(ConfigValue.class)) {
                        ConfigValue annotation = field.getAnnotation(ConfigValue.class);
                        String key = determineKey(field, annotation);
                        String defaultValue = annotation.defaultValue();

                        // Only add if not already present (allows precedence)
                        if (!configMap.containsKey(key) && !defaultValue.isEmpty()) {
                            configMap.put(key, defaultValue);
                        }
                    }
                } catch (Exception e) {
                    // Log or handle field processing errors
                }
            }
        } catch (SecurityException e) {
            // Log or handle security exceptions
        }
    }

    private String determineKey(Field field, ConfigValue annotation) {
        if (annotation.key() != null && !annotation.key().isEmpty()) {
            return annotation.key();
        }

        if (annotation.useFieldNameMapping()) {
            return fieldNameMapper.nameMapping(field.getName());
        }

        return field.getName();
    }

    @Override
    public Map<String, String> toMap() {
        // First add annotated defaults, then system props, then env vars (precedence order)
        Map<String, String> result = new ConcurrentHashMap<>(configMap);
        result.putAll(this.getProperties());
        result.putAll(this.getEnv());
        return result;
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void refresh() {
        configMap.clear();
        scanConfigClasses();
    }
}