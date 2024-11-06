package org.degree.faction.commands.faction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.degree.faction.commands.AbstractCommand;
import org.degree.faction.database.FactionDatabase;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class FactionCreateCommand extends AbstractCommand {

    private final FactionDatabase factionDatabase = new FactionDatabase();

    public FactionCreateCommand() {}

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("Usage: /faction create <name>");
            return;
        }

        String factionName = args[0];
        String leaderUUID = player.getUniqueId().toString();
        String leaderName = player.getName();

        try {
            if (factionDatabase.factionExists(factionName)) {
                player.sendMessage("A faction with this name already exists.");
                return;
            }

            factionDatabase.createFaction(factionName, leaderUUID, leaderName);
            player.sendMessage("Faction " + factionName + " has been created successfully!");

        } catch (SQLException e) {
            player.sendMessage("An error occurred while creating the faction.");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return Collections.emptyList(); // No tab completion for this command
    }
}
