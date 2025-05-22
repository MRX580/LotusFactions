package org.degree.factions.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.degree.factions.database.FactionDatabase;
import org.degree.factions.utils.FactionCache; // наш кэш

public class FactionJoinListener implements Listener {
    private final FactionDatabase db;

    public FactionJoinListener(FactionDatabase db) {
        this.db = db;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        try {
            String faction = db.getFactionNameForPlayer(uuid);
            FactionCache.setFaction(uuid, faction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
