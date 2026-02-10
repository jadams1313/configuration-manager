package com.github.configmanager.examples;

import com.github.configmanager.annotations.ConfigValue;

/**
 * Example cache configuration class.
 */
public class CacheConfig {

    @ConfigValue(defaultValue = "3600") // Becomes "cache_ttl"
    private String cacheTtl;

    @ConfigValue(defaultValue = "1000") // Becomes "cache_size"
    private String cacheSize;

    @ConfigValue(key = "cache.enabled", defaultValue = "true")
    private String enabled;

    @ConfigValue(defaultValue = "LRU") // Becomes "eviction_policy"
    private String evictionPolicy;
}