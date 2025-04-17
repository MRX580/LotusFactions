package org.degree.factions.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.degree.factions.Factions;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalizationManager {
    private final Factions plugin;
    private FileConfiguration localeConfig;
    private final Map<String, String> messages = new HashMap<>();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private String currentLanguage;

    public LocalizationManager(Factions plugin, String language) {
        this.plugin = plugin;
        this.currentLanguage = language;
        loadLocale(language);
    }

    private void loadLocale(String lang) {
        File localeFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");

        if (!localeFile.exists()) {
            plugin.saveResource("lang/" + lang + ".yml", false);
        }

        localeConfig = YamlConfiguration.loadConfiguration(localeFile);

        // Загрузка сообщений в Map
        for (String key : localeConfig.getKeys(true)) {
            if (localeConfig.isString(key)) {
                messages.put(key, localeConfig.getString(key));
            }
        }
    }

    public void reload(String lang) {
        messages.clear();
        this.currentLanguage = lang;
        loadLocale(lang);
    }

    public String getMessage(String key) {
        return formatColors(messages.getOrDefault(key, "Message not found: " + key));
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messages.getOrDefault(key, "Message not found: " + key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return formatColors(message);
    }

    public void sendMessageToPlayer(Player player, String messageKey, Map<String, String> placeholders) {
        String message = getMessage(messageKey, placeholders);
        player.sendMessage(message);
    }

    public void sendMessageToPlayer(Player player, String messageKey) {
        sendMessageToPlayer(player, messageKey, Collections.emptyMap());
    }

    private String formatColors(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hexColor).toString());
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
