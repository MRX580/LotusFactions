package org.degree.factions.commands.faction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.degree.factions.commands.AbstractCommand;
import org.degree.factions.database.FactionDatabase;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FactionCreateCommand extends AbstractCommand {

    private final FactionDatabase factionDatabase = new FactionDatabase();

    public FactionCreateCommand() {}

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(localization.getMessage("messages.only_players_can_use"));
            return;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            localization.sendMessageToPlayer(player, "messages.usage_faction_create");
            return;
        }

        String factionName = args[0];
        String leaderUUID = player.getUniqueId().toString();
        String leaderName = player.getName();

        try {
            if (factionDatabase.factionExists(factionName)) {
                localization.sendMessageToPlayer(player, "messages.faction_already_exists");
                return;
            }

            factionDatabase.createFaction(factionName, leaderUUID, leaderName);
            localization.sendMessageToPlayer(
                    player,
                    "messages.faction_created_successfully",
                    Map.of("factionName", factionName)
            );

        } catch (SQLException e) {
            localization.sendMessageToPlayer(player, "messages.error_creating_faction");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
