package org.degree.factions.web.api;

import org.degree.factions.database.FactionDatabase;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FactionsServlet extends HttpServlet {
    private final FactionDatabase db = new FactionDatabase();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String name = req.getParameter("factionName");
        JSONObject json = new JSONObject();

        if (name == null || name.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.put("error", "Missing parameter: factionName");
            resp.getWriter().write(json.toString());
            return;
        }

        try {
            Connection conn = db.getConnection();

            // 1) Faction info
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT leader_name, creation_date FROM factions WHERE name = ?"
            )) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        json.put("error", "Faction not found: " + name);
                        resp.getWriter().write(json.toString());
                        return;
                    }
                    json.put("factionName", name);
                    json.put("leaderName", rs.getString("leader_name"));
                    json.put("creationDate", rs.getString("creation_date"));
                }
            }

            List<String> members = db.getMemberNamesOfFaction(name);
            json.put("memberCount", members.size());
            json.put("members", new JSONArray(members));

            int joinsToday                        = db.getJoinsToday(name);
            List<Map<String,Object>> dailyPlaytime    = db.getDailyPlaytime(name);
            List<Map<String,Object>> lastOnline   = db.getLastOnline(name);
            Map<String,Object> mostActive         = db.getMostActiveMember(name);
            long avgOnlineSeconds                 = db.getAverageOnlineSeconds(name);

            if (mostActive == null || mostActive.isEmpty()) {
                mostActive = new HashMap<>();
                mostActive.put("name", "-");
                mostActive.put("seconds", 0L);
            }
            if (lastOnline == null || lastOnline.isEmpty()) {
                Map<String,Object> fb = new HashMap<>();
                fb.put("name", "-");
                fb.put("lastOnline", "-");
                lastOnline = Collections.singletonList(fb);
            }

            json.put("joinsToday", joinsToday);

            json.put("dailyPlaytime", new JSONArray(dailyPlaytime));
            json.put("lastOnline",   new JSONArray(lastOnline));
            json.put("mostActiveMember", new JSONObject(mostActive));
            json.put("averageOnlineTime", avgOnlineSeconds);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(json.toString());

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.put("error", "Database error: " + e.getMessage());
            resp.getWriter().write(json.toString());
        }
    }
}
