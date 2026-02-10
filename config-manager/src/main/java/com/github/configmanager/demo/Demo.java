package com.github.configmanager.demo;

import com.github.configmanager.ConfigManager;
import com.github.configmanager.config.BasicConfiguration;
import com.github.configmanager.config.CustomConfiguration;
import com.github.configmanager.listeners.ConfigurationChangeListener;

import java.util.Map;

/**
 * Examples demonstrating how to use the Configuration Manager.
 */
public class Demo {

    /**
     * Example 1: Basic Configuration Setup
     */
    public static void basicConfigurationExample() {
        System.out.println("=== Example 1: Basic Configuration ===");

        // Create a basic configuration with initial values
        BasicConfiguration basicConfig = ConfigManager.createBasicConfiguration(
                Map.of(
                        "app.name", "MyApp",
                        "app.version", "1.0.0",
                        "app.environment", "development"
                )
        );

        ConfigManager.setConfiguration(basicConfig);

        // Get configuration values
        String appName = ConfigManager.getConfigValue("app.name");
        System.out.println("App Name: " + appName);

        // Get with default value
        String timeout = ConfigManager.getConfigValue("request.timeout", "30000");
        System.out.println("Request Timeout: " + timeout);
    }

    /**
     * Example 2: Custom Configuration with Annotations
     */
    public static void customConfigurationExample() {
        System.out.println("\n=== Example 2: Custom Configuration ===");

        // Create custom configuration with annotation scanning
        CustomConfiguration config = ConfigManager.createCustomConfiguration(
                CacheConfig.class
        );

        ConfigManager.setConfiguration(config);

        // Get all configurations
        Map<String, String> allConfigs = ConfigManager.getConfigurationMap();
        System.out.println("Total configurations: " + allConfigs.size());

        // Get specific values (field names are converted to snake_case)
        String dbPort = ConfigManager.getConfigValue("db_port");
        String cacheTtl = ConfigManager.getConfigValue("cache_ttl");
        System.out.println("DB Port: " + dbPort);
        System.out.println("Cache TTL: " + cacheTtl);
    }

    /**
     * Example 3: Async Configuration Changes
     */
    public static void asyncConfigurationExample() {
        System.out.println("\n=== Example 3: Async Configuration Changes ===");

        // Single value update
        ConfigManager.alterConfigurationAsync("db.host", "production-db.com")
                .thenRun(() -> System.out.println("DB host updated!"))
                .exceptionally(e -> {
                    System.err.println("Failed to update: " + e.getMessage());
                    return null;
                });

        // Batch updates
        Map<String, String> changes = Map.of(
                "db.host", "new-host.com",
                "db.port", "5432",
                "cache_ttl", "7200"
        );

        ConfigManager.alterConfigurationAsync(changes)
                .thenRun(() -> System.out.println("Batch update completed!"));
    }

    /**
     * Example 4: Builder Pattern for Multiple Changes
     */
    public static void builderPatternExample() {
        System.out.println("\n=== Example 4: Builder Pattern ===");

        ConfigManager.alterConfiguration()
                .setConfigValue("db.host", "production-db.com")
                .setConfigValue("db.port", 3306)
                .setConfigValue("max_connections", 50)
                .apply()
                .thenRun(() -> System.out.println("All changes applied!"));
    }

    /**
     * Example 5: Type-Safe Configuration Access
     */
    public static void typeSafeExample() {
        System.out.println("\n=== Example 5: Type-Safe Access ===");

        // Get typed values with automatic conversion
        Integer port = ConfigManager.getConfigValue("db.port", Integer.class, 5432);
        Boolean cacheEnabled = ConfigManager.getConfigValue("cache.enabled", Boolean.class, true);
        Long timeout = ConfigManager.getConfigValue("db.timeout", Long.class, 30000L);

        System.out.println("Port (Integer): " + port);
        System.out.println("Cache Enabled (Boolean): " + cacheEnabled);
        System.out.println("Timeout (Long): " + timeout);
    }

    /**
     * Example 6: Configuration Change Listeners
     */
    public static void listenersExample() {
        System.out.println("\n=== Example 6: Configuration Listeners ===");

        // Add a listener to react to configuration changes
        ConfigurationChangeListener listener = changes -> {
            System.out.println("Configuration changed! Updates:");
            changes.forEach((key, value) ->
                    System.out.println("  " + key + " = " + value)
            );
        };

        ConfigManager.addConfigurationChangeListener(listener);

        // Make changes - listener will be notified
        ConfigManager.alterConfiguration()
                .setConfigValue("db.host", "new-host")
                .setConfigValue("db.port", "3306")
                .apply()
                .join(); // Wait for completion

        // Remove listener when done
        ConfigManager.removeConfigurationChangeListener(listener);
    }

    /**
     * Example 7: Working with Snapshots
     */
    public static void snapshotExample() {
        System.out.println("\n=== Example 7: Configuration Snapshots ===");

        // Get an immutable snapshot
        Map<String, String> snapshot = ConfigManager.getConfigurationSnapshot();

        System.out.println("Current configuration snapshot:");
        snapshot.forEach((key, value) ->
                System.out.println("  " + key + " = " + value)
        );

        // Snapshot is immutable - this would throw UnsupportedOperationException
        try {
            snapshot.put("new.key", "new.value");
        } catch (UnsupportedOperationException e) {
            System.out.println("Snapshot is immutable (as expected)");
        }
    }

    /**
     * Example 8: Error Handling
     */
    public static void errorHandlingExample() {
        System.out.println("\n=== Example 8: Error Handling ===");

        // Handle failed async operations
        ConfigManager.alterConfigurationAsync(null, "value")
                .handle((result, error) -> {
                    if (error != null) {
                        System.err.println("Error: " + error.getMessage());
                        return null;
                    }
                    System.out.println("Success!");
                    return result;
                });

        // Type conversion with fallback
        Integer invalidPort = ConfigManager.getConfigValue("invalid.port", Integer.class, 8080);
        System.out.println("Port with fallback: " + invalidPort);
    }

    /**
     * Example 9: Configuration Refresh
     */
    public static void refreshExample() {
        System.out.println("\n=== Example 9: Configuration Refresh ===");

        CustomConfiguration config = ConfigManager.createCustomConfiguration(
                CacheConfig.class
        );
        ConfigManager.setConfiguration(config);

        // Manually refresh to re-scan annotations
        ConfigManager.refresh();
        System.out.println("Configuration refreshed");
    }

    /**
     * Example 10: Chaining Multiple Async Operations
     */
    public static void chainingExample() {
        System.out.println("\n=== Example 10: Chaining Operations ===");

        ConfigManager.alterConfigurationAsync("step", "1")
                .thenCompose(v -> {
                    System.out.println("Step 1 complete");
                    return ConfigManager.alterConfigurationAsync("step", "2");
                })
                .thenCompose(v -> {
                    System.out.println("Step 2 complete");
                    return ConfigManager.alterConfigurationAsync("step", "3");
                })
                .thenRun(() -> System.out.println("All steps complete!"))
                .exceptionally(e -> {
                    System.err.println("Chain failed: " + e.getMessage());
                    return null;
                });
    }

    /**
     * Run all examples
     */
    public static void main(String[] args) throws InterruptedException {
        basicConfigurationExample();
        customConfigurationExample();
        asyncConfigurationExample();
        builderPatternExample();
        typeSafeExample();
        listenersExample();
        snapshotExample();
        errorHandlingExample();
        refreshExample();
        chainingExample();

        // Wait for async operations to complete
        Thread.sleep(2000);

        // Shutdown cleanly
        ConfigManager.shutdown();
        System.out.println("\n=== All examples completed ===");
    }
}