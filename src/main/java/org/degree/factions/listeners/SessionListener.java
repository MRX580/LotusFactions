package org.degree.factions.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.degree.factions.Factions;
import org.degree.factions.database.FactionDatabase;

import java.sql.SQLException;

public class SessionListener implements Listener {
    private final FactionDatabase db = new FactionDatabase();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        String uuid = e.getPlayer().getUniqueId().toString();
        try {
            String faction = db.getFactionNameForPlayer(uuid);
            if (faction != null) db.logSessionStart(faction, uuid);
        } catch (SQLException ex) {
            Factions.getInstance().getLogger()
                    .severe("Error fetching faction for join: " + ex.getMessage());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        String uuid = e.getPlayer().getUniqueId().toString();
        db.logSessionEnd(uuid);
    }
}
