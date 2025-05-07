package org.degree.factions.commands.faction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.degree.factions.commands.AbstractCommand;
import org.degree.factions.database.FactionDatabase;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class FactionDissolveCommand extends AbstractCommand {
    private final FactionDatabase db = new FactionDatabase();

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(localization.getMessage("messages.only_players_can_use"));
            return;
        }
        Player p = (Player) sender;
        String uuid = p.getUniqueId().toString();

        try {
            if (!db.isLeader(uuid)) {
                localization.sendMessageToPlayer(p, "messages.only_leader_can_dissolve");
                return;
            }
            String faction = db.getFactionNameByLeader(uuid);
            if (faction == null) {
                localization.sendMessageToPlayer(p, "messages.you_have_no_faction");
                return;
            }

            db.deleteFaction(faction);
            localization.sendMessageToPlayer(p, "messages.faction_dissolved", Map.of("factionName", faction));
        } catch (SQLException e) {
            localization.sendMessageToPlayer(p, "messages.error_deleting_faction");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
