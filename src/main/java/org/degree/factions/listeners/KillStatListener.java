package org.degree.factions.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.degree.factions.database.FactionDatabase;

public class KillStatListener implements Listener {
    private final FactionDatabase db;

    public KillStatListener(FactionDatabase db) {
        this.db = db;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        String killerUuid = killer.getUniqueId().toString();
        String faction = null;
        try {
            faction = db.getFactionNameForPlayer(killerUuid);
        } catch (Exception ignored) {}
        if (faction == null) return;

        db.incrementKill(killerUuid, faction);
    }
}
