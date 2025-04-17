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

    // Загрузка config.yml
    private void loadConfig() {
        config = plugin.getConfig();
    }

    // Перезагрузка config.yml
    public void reload() {
        plugin.reloadConfig();
        loadConfig();
    }

    // Методы для получения значений из config.yml
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    // Примеры получения настроек
    public int getMaxFactionMembers() {
        return getInt("faction-settings.max-members", 10);
    }

    public String getFactionPrefix() {
        return getString("faction-settings.prefix", "[Faction]");
    }

    // Добавьте другие методы для специфических настроек плагина
}
