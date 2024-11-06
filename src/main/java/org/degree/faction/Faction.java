package org.degree.faction;

import org.bukkit.plugin.java.JavaPlugin;
import org.degree.faction.commands.FactionCommandRouter;
import org.degree.faction.database.Database;
import org.degree.faction.web.JettyServerManager;
import org.degree.faction.web.WebResourceManager;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Faction extends JavaPlugin {
    private static Faction instance;
    private JettyServerManager serverManager;
    private WebResourceManager resourceManager;

    @Override
    public void onEnable() {
        instance = this;
        Path dataFolderPath = getDataFolder().toPath();
        serverManager = new JettyServerManager(8085, dataFolderPath);
        resourceManager = new WebResourceManager(dataFolderPath);

        getCommand("faction").setExecutor(new FactionCommandRouter());

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

    public static Faction getInstance() {
        return instance;
    }
}
