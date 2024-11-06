package org.degree.faction.commands.faction;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.degree.faction.commands.AbstractCommand;
import org.degree.faction.database.FactionDatabase;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FactionTransferCommand extends AbstractCommand {

    private final FactionDatabase factionDatabase = new FactionDatabase();
    private final Map<UUID, String> pendingTransfers = new HashMap<>(); // Карта для хранения намерений о передаче лидерства

    public FactionTransferCommand() {}

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }

        Player currentLeader = (Player) sender;
        UUID leaderUUID = currentLeader.getUniqueId();

        if (args.length < 1) {
            currentLeader.sendMessage("Usage: /faction transfer <player>");
            return;
        }

        // Используем точное совпадение по имени
        Player newLeader = Bukkit.getPlayerExact(args[0]);
        if (newLeader == null) {
            currentLeader.sendMessage("Player " + args[0] + " is not online.");
            return;
        }

        try {
            // Проверяем, что текущий игрок — лидер
            String factionName = factionDatabase.getFactionNameForPlayer(leaderUUID.toString());
            if (factionName == null || !factionDatabase.isLeader(leaderUUID.toString())) {
                currentLeader.sendMessage("You are not the leader of any faction.");
                return;
            }

            // Проверяем, что новый лидер является членом той же фракции
            if (!factionDatabase.isMemberOfFaction(factionName, newLeader.getName())) { // Используем имя, а не UUID
                currentLeader.sendMessage("The specified player is not a member of your faction.");
                return;
            }

            // Проверка на двойное подтверждение
            if (pendingTransfers.containsKey(leaderUUID) && pendingTransfers.get(leaderUUID).equals(newLeader.getName())) {
                // Подтверждено: передача лидерства
                factionDatabase.transferLeadership(factionName, newLeader.getUniqueId().toString());
                currentLeader.sendMessage("You have successfully transferred leadership to " + newLeader.getName() + ".");
                newLeader.sendMessage("You are now the leader of the faction " + factionName + ".");
                pendingTransfers.remove(leaderUUID); // Удаляем запись из карты
            } else {
                // Первый запрос на подтверждение
                pendingTransfers.put(leaderUUID, newLeader.getName());
                currentLeader.sendMessage("Are you sure you want to transfer leadership to " + newLeader.getName() + "?");
                currentLeader.sendMessage("Type /faction transfer " + newLeader.getName() + " again to confirm.");
            }

        } catch (SQLException e) {
            currentLeader.sendMessage("An error occurred while transferring leadership.");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            Player player = (Player) sender;
            try {
                String factionName = factionDatabase.getFactionNameForPlayer(player.getUniqueId().toString());
                if (factionName != null) {
                    // Предлагаем имена членов фракции для автодополнения
                    List<String> members = factionDatabase.getMemberNamesOfFaction(factionName); // Обновленный метод для имен
                    members.remove(player.getName()); // Исключаем самого лидера
                    return members;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }
}
