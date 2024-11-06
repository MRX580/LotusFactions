package org.degree.faction.commands;

import org.bukkit.command.*;
import java.util.List;

public abstract class AbstractCommand implements CommandExecutor, TabCompleter {
    
    public AbstractCommand() {}

    public abstract void execute(CommandSender sender, String label, String[] args);

    public abstract List<String> complete(CommandSender sender, String[] args);

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        execute(commandSender, s, strings);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return complete(commandSender, strings);
    }
}
