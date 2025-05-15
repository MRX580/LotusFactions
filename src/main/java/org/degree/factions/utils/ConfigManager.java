package org.degree.factions.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.degree.factions.Factions;

public class ConfigManager {
    private final Factions plugin;
    private FileConfiguration config;

    public ConfigManager(Factions plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig(); // Сохранить config.yml, если его нет
        loadConfig();
    }

    private void loadConfig() {
        config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        loadConfig();
    }

    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public int getMaxFactionMembers() {
        return getInt("faction-settings.max-members", 10);
    }

    public int getInviteCooldownSeconds() { return getInt("faction-settings.invite-cooldown-seconds", 60); }
    public boolean isAlternativeIPEnabled() { return getBoolean("webserver.alternative-Ip.enabled", false); }
    public String getAlternativeIp() { return getString("webserver.alternative-Ip.address", "localhost"); }
    public int getWebPort() { return getInt("webserver.web-port", 8085); }

}
