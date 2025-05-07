package org.degree.factions.commands.faction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.degree.factions.commands.AbstractCommand;
import org.degree.factions.database.FactionDatabase;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.ChatColor; // Bungee-ChatColor

public class FactionStatsCommand extends AbstractCommand {
    private final FactionDatabase db = new FactionDatabase();

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(localization.getMessage("messages.only_players_can_use"));
            return;
        }
        Player player = (Player) sender;

        try {
            String factionName = db.getFactionNameForPlayer(player.getUniqueId().toString());
            if (factionName == null) {
                sender.sendMessage(localization.getMessage("messages.not_in_faction"));
                localization.sendMessageToPlayer(player, "messages.not_in_faction");
                return;
            }

            String encoded = URLEncoder.encode(factionName, StandardCharsets.UTF_8);
            String url = "http://localhost:8085/?factionName=" + encoded;

            TextComponent link = new TextComponent(url);
            link.setColor(ChatColor.BLUE);
            link.setUnderlined(true);
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

            player.spigot().sendMessage(link);

        } catch (SQLException e) {
            e.printStackTrace();
            localization.sendMessageToPlayer(player, "messages.error_fetching_faction");
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
