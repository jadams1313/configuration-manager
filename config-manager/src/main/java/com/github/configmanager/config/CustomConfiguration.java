package com.github.configmanager.config;
import com.github.configmanager.annotations.ConfigurationValue;
import com.github.configmanager.annotations.FieldNameMappings;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomConfiguration implements Configuration {
    private Map<String, String> configMap;
    private Class<?>[] configClasses;
    private FieldNameMappings fieldNameMapper = new FieldNameMappings();

    public CustomConfiguration(Class<?>... configClasses) {
        this.configMap = new ConcurrentHashMap<>();
        this.configClasses = configClasses;
        scanConfigClasses();
    }

    public CustomConfiguration(Map<String, String> initialMap, Class<?>... configClasses) {
        this.configMap = new ConcurrentHashMap<>(initialMap);
        this.configClasses = configClasses;
        scanConfigClasses();
    }

    private void scanConfigClasses() {
        for (Class<?> clazz : configClasses) {
            scanClass(clazz);
        }
    }

    private void scanClass(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(ConfigurationValue.class)) {
                ConfigurationValue annotation = field.getAnnotation(ConfigurationValue.class);
                String key = determineKey(field, annotation);
                String defaultValue = annotation.defaultValue();

                // Only add if not already present (allows precedence)
                if (!configMap.containsKey(key) && !defaultValue.isEmpty()) {
                    configMap.put(key, defaultValue);
                }
            }
        }
    }

    private String determineKey(Field field, ConfigurationValue annotation) {
        if (!annotation.key().isEmpty()) {
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