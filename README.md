# Configuration Manager

A multi-threaded Java configuration management library that pulls from annotations, system properties, and environment variables with runtime modification capabilities.

## Features

- **Multiple Config Sources**: Annotations, system properties, environment variables
- **Multi-threaded Operations**: All config alterations run asynchronously
- **Field Name Mapping**: Automatic camelCase → snake_case conversion
- **Runtime Modifications**: Change configurations on-the-fly
- **Thread-safe**: Built with ConcurrentHashMap and CompletableFuture

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
```

### 4. Modify Configurations (Multi-threaded)

```java
// Builder pattern (async)
ConfigManager.alterConfiguration()
    .setConfigValue("db_host", "production-db.com")
    .setConfigValue("db_port", "3306")
    .apply()
    .thenRun(() -> System.out.println("Config updated!"));

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

## Configuration Precedence

1. **Environment Variables** (highest priority)
2. **System Properties**
3. **Annotated Defaults** (lowest priority)

## Field Name Mapping

- `dbHost` → `db_host`
- `maxConnections` → `max_connections`
- Disable with `@ConfigValue(useFieldNameMapping = false)`

## Basic Configuration

For simple use cases without annotations:

```java
BasicConfiguration basicConfig = ConfigManager.createBasicConfiguration(
    Map.of("app.name", "MyApp", "app.version", "1.0")
);
ConfigManager.setConfiguration(basicConfig);
```

## Thread Safety

All operations are thread-safe using:
- `ConcurrentHashMap` for config storage
- `CompletableFuture` for async operations
- `CachedThreadPool` for optimal thread management

---

The concept of a configuration management tool was shown to me by a colleague at New pig. This isn't the same as his tool, but it takes inspiration - Thanks Vic. 
