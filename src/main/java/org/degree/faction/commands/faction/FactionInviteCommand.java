package org.degree.faction.commands.faction;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.degree.faction.commands.AbstractCommand;
import org.degree.faction.database.FactionDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactionInviteCommand extends AbstractCommand {

    private final FactionDatabase factionDatabase = new FactionDatabase();

    public FactionInviteCommand() {}

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }

        Player inviter = (Player) sender;

        // Получаем имя фракции игрока
        String factionName;
        try {
            factionName = factionDatabase.getFactionNameForPlayer(inviter.getUniqueId().toString());
            if (factionName == null) {
                inviter.sendMessage("You are not part of any faction.");
                return;
            }
        } catch (SQLException e) {
            inviter.sendMessage("An error occurred while fetching your faction.");
            e.printStackTrace();
            return;
        }

        if (args.length < 1) {
            inviter.sendMessage("Usage: /faction invite <player>");
            return;
        }

        Player invitee = Bukkit.getPlayer(args[0]);
        if (invitee == null || !invitee.isOnline()) {
            inviter.sendMessage("Player " + args[0] + " is not online.");
            return;
        }

        try {
            // Добавляем приглашение, автоматически удаляя старые приглашения для этого игрока
            factionDatabase.addInvite(factionName, inviter.getUniqueId().toString(), invitee.getUniqueId().toString());
            invitee.sendMessage("You have been invited to join " + inviter.getName() + "'s faction. Use /faction accept to join.");
            inviter.sendMessage("Invitation sent to " + invitee.getName() + ".");

        } catch (SQLException e) {
            inviter.sendMessage("An error occurred while sending the invite.");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.equals(sender)) {
                    suggestions.add(player.getName());
                }
            }
            return suggestions;
        }
        return Collections.emptyList();
    }
}
