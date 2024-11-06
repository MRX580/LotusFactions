package org.degree.faction.commands.faction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.degree.faction.commands.AbstractCommand;
import org.degree.faction.database.FactionDatabase;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class FactionLeaveCommand extends AbstractCommand {

    private final FactionDatabase factionDatabase = new FactionDatabase();

    public FactionLeaveCommand() {}

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();

        try {
            // Проверяем, является ли игрок лидером
            if (factionDatabase.isLeader(playerUUID)) {
                player.sendMessage("You cannot leave the faction because you are the leader. Transfer leadership or disband the faction first.");
                return;
            }

            // Удаляем игрока из фракции
            factionDatabase.removeMemberFromFaction(playerUUID);
            player.sendMessage("You have successfully left the faction.");

        } catch (SQLException e) {
            player.sendMessage("An error occurred while leaving the faction.");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return Collections.emptyList(); // Нет автодополнения для этой команды
    }
}
