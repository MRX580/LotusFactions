package org.degree.faction.commands.faction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.degree.faction.commands.AbstractCommand;
import org.degree.faction.database.FactionDatabase;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class FactionAcceptCommand extends AbstractCommand {

    private final FactionDatabase factionDatabase = new FactionDatabase();

    public FactionAcceptCommand() {}

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();
        String memberName = player.getName();

        try {
            String factionName = factionDatabase.getFactionNameForInvite(playerUUID);
            if (factionName == null) {
                player.sendMessage("You don't have any pending invites or the invite has expired.");
                return;
            }

            // Добавляем игрока в фракцию как обычного участника
            factionDatabase.addMemberToFaction(factionName, playerUUID, memberName, "MEMBER");
            factionDatabase.removeInvite(factionName, playerUUID);

            player.sendMessage("You have successfully joined the faction " + factionName + "!");
        } catch (SQLException e) {
            player.sendMessage("An error occurred while accepting the invite.");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return Collections.emptyList(); // Нет автодополнения для этой команды
    }
}
