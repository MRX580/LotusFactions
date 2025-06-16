package org.degree.factions;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.degree.factions.commands.FactionCommandRouter;
import org.degree.factions.database.Database;
import org.degree.factions.database.FactionDatabase;
import org.degree.factions.http.FactionApiClient;
import org.degree.factions.listeners.*;
import org.degree.factions.utils.BlockStatCache;
import org.degree.factions.utils.ConfigManager;
import org.degree.factions.utils.LocalizationManager;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public final class Factions extends JavaPlugin {
    private static Factions instance;
    private ConfigManager configManager;
    private LocalizationManager localizationManager;
    private FactionApiClient apiClient;

    @Override
    public void onEnable() {
        // bstats
        int pluginId = 25785;
        new Metrics(this, pluginId);

        instance = this;
        FactionDatabase factionDatabase = new FactionDatabase();
        configManager = new ConfigManager(this);
        String lang = configManager.getString("lang", "en");
        localizationManager = new LocalizationManager(this, lang);

        apiClient = new FactionApiClient(this, "https://lotuscraft.fun", factionDatabase);

        getCommand("faction").setExecutor(new FactionCommandRouter());

        getServer().getPluginManager().registerEvents(new SessionListener(), this);
        getServer().getPluginManager().registerEvents(new BlockStatListener(factionDatabase), this);
        getServer().getPluginManager().registerEvents(new BlockStatQuitListener(factionDatabase), this);
        getServer().getPluginManager().registerEvents(new FactionJoinListener(factionDatabase), this);
        getServer().getPluginManager().registerEvents(new KillStatListener(factionDatabase), this);

        // Регистрация раннера для сохранения статистики блоков
        new BlockStatSaverTask(factionDatabase).runTaskTimer(this, 20 * 60, 20 * 60); // раз в 1 минуту


        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new FactionPlaceholder(this).register();
            getLogger().info("Registered FactionPlaceholder for PAPI");
        } else {
            getLogger().warning("PlaceholderAPI not found; FactionPlaceholder not registered");
        }

    }

    @Override
    public void onDisable() {
        try {
            getLogger().info("Web-server has stopped");
        } catch (Exception e) {
            getLogger().severe("Error during stopping server: " + e.getMessage());
            e.printStackTrace();
        }
        new Database().closeConnection();
    }

    public class BlockStatSaverTask extends BukkitRunnable {
        private final FactionDatabase db;

        public BlockStatSaverTask(FactionDatabase db) {
            this.db = db;
        }

        @Override
        public void run() {
            Map<String, Map<String, BlockStatCache.BlockStat>> allStats = BlockStatCache.getAndClearStats();
            for (String uuid : allStats.keySet()) {
                for (Map.Entry<String, BlockStatCache.BlockStat> entry : allStats.get(uuid).entrySet()) {
                    String blockType = entry.getKey();
                    BlockStatCache.BlockStat stat = entry.getValue();
                    db.saveOrUpdateBlockStat(uuid, stat.factionName, blockType, stat.placed, stat.broken);
                }
            }
        }
    }


    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public static Factions getInstance() {
        return instance;
    }

    public FactionApiClient getApiClient() { return apiClient; }
}
