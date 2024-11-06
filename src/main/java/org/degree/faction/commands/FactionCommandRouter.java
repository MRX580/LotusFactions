package org.degree.faction.commands;

import org.bukkit.command.CommandSender;
import org.degree.faction.commands.faction.*;

import java.util.*;

public class FactionCommandRouter extends AbstractCommand {

    private final Map<String, AbstractCommand> subCommands = new HashMap<>();

    public FactionCommandRouter() {
        subCommands.put("invite", new FactionInviteCommand());
        subCommands.put("create", new FactionCreateCommand());
        subCommands.put("accept", new FactionAcceptCommand());
        subCommands.put("leave", new FactionLeaveCommand());
        subCommands.put("transfer", new FactionTransferCommand());
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Available subcommands: invite");
            return;
        }

        AbstractCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand != null) {
            subCommand.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
        } else {
            sender.sendMessage("Unknown subcommand. Available: invite");
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(subCommands.keySet());
        } else if (args.length > 1) {
            AbstractCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                return subCommand.complete(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return Collections.emptyList();
    }
}
