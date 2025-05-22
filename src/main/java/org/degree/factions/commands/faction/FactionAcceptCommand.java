package org.degree.factions.commands.faction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.degree.factions.commands.AbstractCommand;
import org.degree.factions.database.FactionDatabase;
import org.degree.factions.utils.FactionCache;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FactionAcceptCommand extends AbstractCommand {

    private final FactionDatabase factionDatabase = new FactionDatabase();

    public FactionAcceptCommand() {}

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(localization.getMessage("messages.only_players_can_use"));
            return;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();

        try {
            String factionName = factionDatabase.getFactionNameForInvite(playerUUID);
            if (factionName == null) {
                localization.sendMessageToPlayer(player, "messages.no_pending_invites");
                return;
            }

            factionDatabase.addMemberToFaction(factionName, playerUUID, player.getName(), "MEMBER");
            factionDatabase.removeInvite(factionName, playerUUID);

            FactionCache.setFaction(playerUUID, factionName);

            localization.sendMessageToPlayer(player,
                    "messages.faction_joined_successfully",
                    Map.of("factionName", factionName)
            );

        } catch (SQLException e) {
            localization.sendMessageToPlayer(player, "messages.error_accepting_invite");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
