package com.github.configmanager.listeners;

import java.util.Map;

/**
 * Listener interface for configuration change events.
 */
@FunctionalInterface
public interface ConfigurationChangeListener {

    /**
     * Called when configuration values are changed.
     *
     * @param changes Map of changed keys and their new values
     */
    void onConfigurationChanged(Map<String, String> changes);
}