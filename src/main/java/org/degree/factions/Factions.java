package org.degree.factions;

import org.bukkit.plugin.java.JavaPlugin;
import org.degree.factions.commands.FactionCommandRouter;
import org.degree.factions.database.Database;
import org.degree.factions.listeners.SessionListener;
import org.degree.factions.utils.ConfigManager;
import org.degree.factions.utils.LocalizationManager;
import org.degree.factions.web.JettyServerManager;
import org.degree.factions.web.WebResourceManager;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Factions extends JavaPlugin {
    private static Factions instance;
    private JettyServerManager serverManager;
    private WebResourceManager resourceManager;
    private ConfigManager configManager;
    private LocalizationManager localizationManager;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);
        String lang = configManager.getString("lang", "en");
        localizationManager = new LocalizationManager(this, lang);
        serverManager = new JettyServerManager(8085);
        resourceManager = new WebResourceManager();

        getCommand("faction").setExecutor(new FactionCommandRouter());

        getServer().getPluginManager().registerEvents(new SessionListener(), this);


        Path webDir = Paths.get(getDataFolder().getPath(), "web");
        try {
            resourceManager.copyResources("/web", webDir);
            serverManager.startServer(webDir);
            getLogger().info("Web-server has started on port 8085");
        } catch (IOException e) {
            getLogger().severe("Error during copying resources: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            getLogger().severe("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            serverManager.stopServer();
            getLogger().info("Web-server has stopped");
        } catch (Exception e) {
            getLogger().severe("Error during stopping server: " + e.getMessage());
            e.printStackTrace();
        }
        new Database().closeConnection();
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
}
