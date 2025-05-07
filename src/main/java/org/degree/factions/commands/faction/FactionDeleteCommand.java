package org.degree.factions.commands.faction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.degree.factions.commands.AbstractCommand;
import org.degree.factions.database.FactionDatabase;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class FactionDeleteCommand extends AbstractCommand {
    private final FactionDatabase db = new FactionDatabase();

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("faction.admin.delete")) {
            sender.sendMessage(localization.getMessage("messages.no_permission"));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(localization.getMessage("messages.usage_faction_delete"));
            return;
        }

        String faction = args[0];
        try {
            if (!db.factionExists(faction)) {
                sender.sendMessage(localization.getMessage("messages.faction_not_found"));
                return;
            }
            db.deleteFaction(faction);
            sender.sendMessage(localization.getMessage(
                    "messages.faction_deleted_by_admin",
                    Map.of("factionName", faction)
            ));
        } catch (SQLException e) {
            sender.sendMessage(localization.getMessage("messages.error_deleting_faction"));
            e.printStackTrace();
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            try {
                List<String> all = db.getAllFactionNames();
                String prefix = args[0].toLowerCase();
                return all.stream()
                        .filter(name -> name.toLowerCase().startsWith(prefix))
                        .toList();
            } catch (SQLException e) {
                return List.of();
            }
        }
        return List.of();
    }
}
