package org.degree.factions.commands.faction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.degree.factions.Factions;
import org.degree.factions.commands.AbstractCommand;
import org.degree.factions.database.FactionDatabase;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.Bukkit.*;

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
                localization.sendMessageToPlayer(player, "messages.not_in_faction");
                return;
            }

            // Отправка POST-запроса с реальными данными фракции
            apiClient.postFactionFromDatabase(factionName);

            // Далее всё как было (отправка игроку ссылки на статистику)
            String encoded = URLEncoder.encode(factionName, StandardCharsets.UTF_8);
            TextComponent link = getLink(encoded);
            player.spigot().sendMessage(link);

        } catch (SQLException e) {
            e.printStackTrace();
            localization.sendMessageToPlayer(player, "messages.error_fetching_faction");
        }
    }

    private @NotNull TextComponent getLink(String encoded) {
        String url = "https://lotuscraft.fun/faction/" + encoded;

        TextComponent link = new TextComponent(url);
        link.setColor(ChatColor.BLUE);
        link.setUnderlined(true);
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        return link;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }

}
