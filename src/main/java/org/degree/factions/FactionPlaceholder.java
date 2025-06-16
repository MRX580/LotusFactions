package org.degree.factions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.degree.factions.database.FactionDatabase;
import org.degree.factions.models.Faction;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class FactionPlaceholder extends PlaceholderExpansion {
    FactionDatabase factionDatabase = new FactionDatabase();

    private final Factions plugin;

    public FactionPlaceholder(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "faction";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        try {
            String factionName = factionDatabase.getFactionNameForPlayer(player.getUniqueId().toString());
            if (factionName == null) return "";

            Faction faction = factionDatabase.loadFaction(factionName);
            if (faction == null) return "";

            switch (identifier) {
                case "prefix":
                    return ChatColor.of(faction.getColorHex())
                            + faction.getName() + " "
                            + ChatColor.RESET;

                default:
                    return "";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    }


}
