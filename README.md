# Configuration Manager

A robust, multi-threaded Java configuration management library that pulls from annotations, system properties, and environment variables with runtime modification capabilities and reactive listeners.

[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## Features

- ✅ **Multiple Config Sources**: Annotations, system properties, environment variables
- ✅ **True Multi-threaded Operations**: All config alterations run asynchronously with CompletableFuture
- ✅ **Field Name Mapping**: Automatic camelCase → snake_case conversion
- ✅ **Runtime Modifications**: Change configurations on-the-fly
- ✅ **Thread-safe**: Built with ConcurrentHashMap and proper synchronization
- ✅ **Type Safety**: Built-in type conversion for primitives and common types
- ✅ **Change Listeners**: React to configuration changes in real-time
- ✅ **Immutable Snapshots**: Get point-in-time configuration views
- ✅ **Comprehensive Error Handling**: Proper validation and exception handling
- ✅ **Graceful Shutdown**: Proper resource cleanup

## Quick Start

### 1. Define Configuration Classes

```java
public class DatabaseConfig {
    @ConfigValue(key = "db.host", defaultValue = "localhost")
    private String dbHost;
    
    @ConfigValue(defaultValue = "5432") // Becomes "db_port"
    private String dbPort;
    
    @ConfigValue(useFieldNameMapping = false, defaultValue = "admin")
    private String username; // Stays "username"
}

public class CacheConfig {
    @ConfigValue(defaultValue = "3600") // Becomes "cache_ttl"
    private String cacheTtl;
}
```

### 2. Initialize Configuration

```java
// Create custom configuration with your classes
CustomConfiguration config = ConfigManager.createCustomConfiguration(
    DatabaseConfig.class, 
    CacheConfig.class
);
ConfigManager.setConfiguration(config);
```

### 3. Get Configuration Values

```java
// Get all configurations
Map<String, String> allConfigs = ConfigManager.getConfigurationMap();

// Get specific values
String dbHost = ConfigManager.getConfigValue("db_host");
String timeout = ConfigManager.getConfigValue("request_timeout", "30000");

// Type-safe access
Integer port = ConfigManager.getConfigValue("db_port", Integer.class, 5432);
Boolean enabled = ConfigManager.getConfigValue("cache.enabled", Boolean.class, true);
```

### 4. Modify Configurations (Async)

```java
// Builder pattern (returns CompletableFuture)
ConfigManager.alterConfiguration()
    .setConfigValue("db_host", "production-db.com")
    .setConfigValue("db_port", "3306")
    .apply()
    .thenRun(() -> System.out.println("Config updated!"))
    .exceptionally(e -> {
        System.err.println("Update failed: " + e.getMessage());
        return null;
    });

// Quick single value update
ConfigManager.alterConfigurationAsync("cache_ttl", "7200")
    .thenRun(() -> System.out.println("Cache TTL updated!"));

// Batch updates
Map<String, String> changes = Map.of(
    "db_host", "new-host.com",
    "db_port", "5432"
);
ConfigManager.alterConfigurationAsync(changes);
```

### 5. Listen to Configuration Changes

```java
// Add a listener
ConfigurationChangeListener listener = changes -> {
    System.out.println("Configuration changed:");
    changes.forEach((key, value) -> 
        System.out.println("  " + key + " = " + value)
    );
};

ConfigManager.addConfigurationChangeListener(listener);

// Make changes - listener will be notified
ConfigManager.alterConfigurationAsync("db_host", "new-host");

// Remove listener when done
ConfigManager.removeConfigurationChangeListener(listener);
```

## Configuration Precedence

Configuration values are resolved in the following order (highest to lowest priority):

1. **Environment Variables** (highest priority)
2. **System Properties** (`-Dkey=value`)
3. **Annotated Defaults** (lowest priority)

## Field Name Mapping

The library automatically converts camelCase field names to snake_case:

- `dbHost` → `db_host`
- `maxConnections` → `max_connections`
- `cacheTtl` → `cache_ttl`

Disable with `@ConfigValue(useFieldNameMapping = false)`

## Basic Configuration

For simple use cases without annotations:

```java
BasicConfiguration basicConfig = ConfigManager.createBasicConfiguration(
    Map.of("app.name", "MyApp", "app.version", "1.0")
);
ConfigManager.setConfiguration(basicConfig);
```

## Advanced Features

### Type-Safe Configuration Access

```java
// Supported types: String, Integer, Long, Double, Float, Boolean
Integer port = ConfigManager.getConfigValue("server.port", Integer.class, 8080);
Boolean debug = ConfigManager.getConfigValue("app.debug", Boolean.class, false);
Long timeout = ConfigManager.getConfigValue("timeout", Long.class, 30000L);
```

### Configuration Snapshots

```java
// Get an immutable snapshot of current configuration
Map<String, String> snapshot = ConfigManager.getConfigurationSnapshot();

// Snapshot is immutable - modifications will throw UnsupportedOperationException
```

### Chaining Async Operations

```java
ConfigManager.alterConfigurationAsync("step", "1")
    .thenCompose(v -> ConfigManager.alterConfigurationAsync("step", "2"))
    .thenCompose(v -> ConfigManager.alterConfigurationAsync("step", "3"))
    .thenRun(() -> System.out.println("All steps complete!"));
```

### Configuration Refresh

```java
// Refresh configuration (re-scans annotations)
ConfigManager.refresh(); // Only works with CustomConfiguration
```

### Checking Configuration State

```java
// Check if a key exists
boolean hasKey = ConfigManager.hasConfigValue("db.host");

// Get configuration size
int size = ConfigManager.getConfigurationSize();

// Get number of registered listeners
int listenerCount = ConfigManager.getListenerCount();
```

## Thread Safety

All operations are thread-safe using:
- `ConcurrentHashMap` for config storage
- `CompletableFuture` for async operations
- `CachedThreadPool` for optimal thread management
- `CopyOnWriteArrayList` for listener management

## Graceful Shutdown

The library includes automatic shutdown handling:

```java
// Automatic shutdown on JVM exit (registered via shutdown hook)

// Manual shutdown
ConfigManager.shutdown();

// Check shutdown state
boolean isShutdown = ConfigManager.isShutdown();
```

## Error Handling

The library includes comprehensive error handling:

```java
// Async operations return CompletableFuture
ConfigManager.alterConfigurationAsync("key", "value")
    .handle((result, error) -> {
        if (error != null) {
            System.err.println("Error: " + error.getMessage());
            // Handle error
            return null;
        }
        return result;
    });

// Type conversion with automatic fallback
Integer port = ConfigManager.getConfigValue("invalid", Integer.class, 8080);
// Returns 8080 if "invalid" is not a valid integer
```

## Maven Dependency

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github</groupId>
    <artifactId>config-manager</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/config-manager.git
cd config-manager

# Build with Maven
mvn clean install

# Run tests
mvn test

# Run examples
mvn exec:java -Dexec.mainClass="com.github.configmanager.examples.UsageExamples"
```

## Examples

See the [UsageExamples.java](src/main/java/com/github/configmanager/examples/UsageExamples.java) file for comprehensive examples including:

1. Basic Configuration Setup
2. Custom Configuration with Annotations
3. Async Configuration Changes
4. Builder Pattern for Multiple Changes
5. Type-Safe Configuration Access
6. Configuration Change Listeners
7. Working with Snapshots
8. Error Handling
9. Configuration Refresh
10. Chaining Multiple Async Operations

## API Documentation

### ConfigManager

Main entry point for all configuration operations.

**Key Methods:**
- `getConfigValue(String key)` - Get configuration value
- `getConfigValue(String key, Class<T> type, T defaultValue)` - Type-safe get
- `alterConfiguration()` - Start building configuration changes
- `alterConfigurationAsync(String key, String value)` - Quick async update
- `addConfigurationChangeListener(listener)` - Register change listener
- `shutdown()` - Gracefully shutdown the manager

### @ConfigValue Annotation

Marks fields for configuration scanning.

**Parameters:**
- `key` - Override the configuration key (optional)
- `defaultValue` - Default value if not found in env/properties
- `useFieldNameMapping` - Enable/disable camelCase → snake_case (default: true)

## Performance Considerations

- Configuration reads (`getConfigValue`) are fast - backed by ConcurrentHashMap
- Configuration writes are async - won't block your application
- Listeners are executed asynchronously on configuration changes
- Thread pool automatically scales based on workload
- Immutable snapshots prevent accidental modifications

## Best Practices

1. **Initialize early**: Set up configuration at application startup
2. **Use type-safe methods**: Leverage `getConfigValue(key, type, default)` for type safety
3. **Listen selectively**: Only register listeners for config changes you care about
4. **Handle async errors**: Always add error handling to CompletableFuture operations
5. **Shutdown cleanly**: Call `ConfigManager.shutdown()` or rely on the shutdown hook
6. **Use snapshots for consistency**: When you need a consistent view across multiple reads

## Troubleshooting

### Configuration not updating
- Ensure you're calling `.apply()` on configuration alterations
- Check that async operations have completed (use `.join()` or `.thenRun()`)
- Verify the configuration type supports alterations

### Type conversion failures
- The library logs warnings when type conversion fails
- Always provide sensible default values for type-safe methods
- Check that the configuration value is in the expected format

### Memory leaks
- Remove listeners when no longer needed
- Call `ConfigManager.shutdown()` when application exits
- Don't hold references to configuration snapshots indefinitely

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass (`mvn test`)
5. Submit a pull request

## License

MIT License - feel free to use in your projects!

## Acknowledgments

The concept of a configuration management tool was shown to me by a colleague at New Pig. This isn't the same as his tool, but it takes inspiration - Thanks Vic
