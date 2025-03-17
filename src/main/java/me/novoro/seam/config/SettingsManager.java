package me.novoro.seam.config;

import me.novoro.seam.api.configuration.Configuration;
import me.novoro.seam.api.configuration.VersionedConfig;

import java.util.HashMap;
import java.util.Map;

public class SettingsManager extends VersionedConfig {
    private static final Map<String, String> SETTINGS = new HashMap<>();

    @Override
    protected void reload(Configuration settingsConfig) {
        super.reload(settingsConfig);
        SETTINGS.clear();
        for (String key : settingsConfig.getKeys()) {
            Object value = settingsConfig.get(key);
            SETTINGS.put(key, value == null ? "" : value.toString());
        }
    }


    /**
     * Retrieves the setting value as a string.
     * @param key The setting key.
     * @return The setting value, or null if not present.
     */
    public static String getSetting(String key) { return SETTINGS.get(key); }

    @Override
    public double getCurrentConfigVersion() {
        return 1.0;
    }

    @Override
    protected String getConfigFileName() {
        return "settings.yml";
    }
}