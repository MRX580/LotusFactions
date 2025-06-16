package org.degree.factions.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.degree.factions.database.FactionDatabase;
import org.degree.factions.utils.BlockStatCache;
import org.degree.factions.utils.FactionCache;

public class BlockStatListener implements Listener {
    private final FactionDatabase db;

    public BlockStatListener(FactionDatabase db) {
        this.db = db;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String blockType = event.getBlock().getType().name();
        String faction = FactionCache.getFaction(uuid);
        if (faction != null) {
            BlockStatCache.incrementPlaced(uuid, faction, blockType);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String blockType = event.getBlock().getType().name();
        String faction = FactionCache.getFaction(uuid);
        if (faction != null) {
            BlockStatCache.incrementBroken(uuid, faction, blockType);
        }
    }
}
