package org.degree.factions.http;
import com.google.gson.Gson;
import org.bukkit.plugin.java.JavaPlugin;
import org.degree.factions.database.FactionDatabase;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class FactionApiClient {

    private final JavaPlugin plugin;
    private final String apiUrl;
    private final Gson gson = new Gson();
    private final FactionDatabase factionDatabase;

    public FactionApiClient(JavaPlugin plugin, String apiUrl, FactionDatabase factionDatabase) {
        this.plugin = plugin;
        this.apiUrl = apiUrl;
        this.factionDatabase = factionDatabase;
    }

    /**
     * Собирает данные фракции из базы и отправляет POST-запрос для её создания/обновления.
     *
     * @param factionName - имя (или uuid) фракции
     */
    public void postFactionFromDatabase(String factionName) {
        try {
            List<Map<String, String>> membersList = factionDatabase.getMemberNameUuidPairsOfFaction(factionName); // [{name, uuid}, ...]
            int membersCount = membersList.size();

            List<Map<String, Object>> members = new ArrayList<>();
            String mostActiveName = "-";
            long maxHours = -1;
            String mostKillsName = "-";
            int maxKills = -1;

            for (Map<String, String> member : membersList) {
                String name = member.get("name");
                String uuid = member.get("uuid");

                long hours = factionDatabase.getTotalHoursForPlayer(uuid);
                int kills = factionDatabase.getTotalKillsForPlayer(uuid);

                Map<String, Object> memberJson = new HashMap<>();
                memberJson.put("name", name);
                memberJson.put("hours", hours);
                memberJson.put("kills", kills);
                members.add(memberJson);

                if (hours > maxHours) {
                    maxHours = hours;
                    mostActiveName = name;
                }
                if (kills > maxKills) {
                    maxKills = kills;
                    mostKillsName = name;
                }
            }

            List<Map<String, Object>> activityByDate = factionDatabase.getActivityOverTime(factionName)
                    .stream()
                    .map(entry -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("date", entry.get("date"));
                        map.put("online", entry.get("count"));
                        return map;
                    })
                    .collect(Collectors.toList());

            List<Map<String, Object>> hoursByDay = factionDatabase.getDailyPlaytime(factionName)
                    .stream()
                    .map(day -> {
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("date", day.get("date"));
                        long seconds = ((Number) day.get("seconds")).longValue();
                        entry.put("hours", seconds / 3600);
                        return entry;
                    })
                    .collect(Collectors.toList());

            List<Map<String, Object>> resources = factionDatabase.getResourcesOfFaction(factionName);

            List<Map<String, Object>> blocks = factionDatabase.getBlocksOfFaction(factionName);

            Map<String, Object> json = new LinkedHashMap<>();
            json.put("slug", factionName); // Или другой уникальный идентификатор
            json.put("name", "Фракция «" + factionName + "»");
            json.put("membersCount", membersCount);
            json.put("members", members);
            json.put("activityByDate", activityByDate);
            json.put("hoursByDay", hoursByDay);
            json.put("mostKills", mostKillsName);
            json.put("mostActive", mostActiveName);
            json.put("resources", resources);
            json.put("blocks", blocks);

            String fullUrl = apiUrl + "/api/faction-stats/" + factionName + "/";
            URL url = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String jsonStr = gson.toJson(json);
            try (OutputStream stream = conn.getOutputStream()) {
                stream.write(jsonStr.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 201) {
                plugin.getLogger().info("[FactionApiClient] Фракция успешно отправлена: " + factionName);
            } else {
                plugin.getLogger().warning("[FactionApiClient] Ошибка отправки: HTTP " + responseCode);
            }
            conn.disconnect();

        } catch (SQLException e) {
            plugin.getLogger().severe("[FactionApiClient] SQL Error: " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().severe("[FactionApiClient] Ошибка при POST-запросе: " + e.getMessage());
        }
    }

}
