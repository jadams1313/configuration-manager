package com.github.configmanager;
import com.github.configmanager.config.Configuration;
import com.github.configmanager.config.BasicConfiguration;
import com.github.configmanager.config.CustomConfiguration;
import com.github.configmanager.ConfigurationAlteration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager {
    private static Configuration currentConfiguration;

    static {
        // Default to basic configuration
        currentConfiguration = new BasicConfiguration(new ConcurrentHashMap<>());
    }

    public static Map<String, String> getConfigurationMap() {
        return currentConfiguration.toMap();
    }

    public static ConfigurationAlteration alterConfiguration() {
        if (currentConfiguration instanceof CustomConfiguration) {
            return new ConfigurationAlteration(((CustomConfiguration) currentConfiguration).getConfigMap());
        } else if (currentConfiguration instanceof BasicConfiguration) {
            return new ConfigurationAlteration(((BasicConfiguration) currentConfiguration).toMap());
        }
        return new ConfigurationAlteration(new ConcurrentHashMap<>());
    }

    public static void setConfiguration(Configuration configuration) {
        currentConfiguration = configuration;
    }

    public static Configuration getCurrentConfiguration() {
        return currentConfiguration;
    }

    public static String getConfigValue(String key) {
        return getConfigurationMap().get(key);
    }

    public static String getConfigValue(String key, String defaultValue) {
        return getConfigurationMap().getOrDefault(key, defaultValue);
    }

    public static void refresh() {
        if (currentConfiguration instanceof CustomConfiguration) {
            ((CustomConfiguration) currentConfiguration).refresh();
        }
    }

    // Convenience methods for creating configurations
    public static CustomConfiguration createCustomConfiguration(Class<?>... configClasses) {
        return new CustomConfiguration(configClasses);
    }

    public static BasicConfiguration createBasicConfiguration(Map<String, String> initialMap) {
        return new BasicConfiguration(initialMap);
    }
}