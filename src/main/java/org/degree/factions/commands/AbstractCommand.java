package org.degree.factions.commands;

import org.bukkit.command.*;
import org.degree.factions.Factions;
import org.degree.factions.utils.ConfigManager;
import org.degree.factions.utils.LocalizationManager;

import java.util.List;

public abstract class AbstractCommand implements CommandExecutor, TabCompleter {
    protected final LocalizationManager localization;
    protected final ConfigManager config;

    public AbstractCommand() {
        this.localization = Factions.getInstance().getLocalizationManager();
        this.config = Factions.getInstance().getConfigManager();
    }

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
