package org.degree.factions.database;

import org.degree.factions.models.Faction;

import java.sql.*;
import java.util.*;

public class FactionDatabase {

    private final Database database = new Database();
    private static final long MIN_SESSION_MS = 5 * 60_000;
    private static final long MERGE_THRESHOLD_MS = 2 * 60_000;

    public Connection getConnection() {
        return database.getConnection();
    }

    public void addInvite(String factionName, String inviterUUID, String inviteeUUID) throws SQLException {
        removeAllInvitesForPlayer(inviteeUUID);
        String sql = "INSERT INTO faction_invites (faction_name, inviter_uuid, invitee_uuid, invite_date, expiry_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, factionName);
            pstmt.setString(2, inviterUUID);
            pstmt.setString(3, inviteeUUID);
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis() + 86400000));
            pstmt.executeUpdate();
        }
    }

    public void removeAllInvitesForPlayer(String inviteeUUID) throws SQLException {
        String sql = "DELETE FROM faction_invites WHERE invitee_uuid = ?";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, inviteeUUID);
            pstmt.executeUpdate();
        }
    }

    public void removeInvite(String factionName, String inviteeUUID) throws SQLException {
        String sql = "DELETE FROM faction_invites WHERE faction_name = ? AND invitee_uuid = ?";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, factionName);
            pstmt.setString(2, inviteeUUID);
            pstmt.executeUpdate();
        }
    }

    public String getFactionNameByLeader(String leaderUUID) throws SQLException {
        String sql = "SELECT name FROM factions WHERE leader_uuid = ?";
        try (PreparedStatement ps = database.prepareStatement(sql)) {
            ps.setString(1, leaderUUID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("name") : null;
            }
        }
    }

    public void deleteFaction(String factionName) throws SQLException {
        try (PreparedStatement ps = database.prepareStatement(
                "DELETE FROM faction_members WHERE faction_name = ?")) {
            ps.setString(1, factionName);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = database.prepareStatement(
                "DELETE FROM faction_invites WHERE faction_name = ?")) {
            ps.setString(1, factionName);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = database.prepareStatement(
                "DELETE FROM faction_sessions WHERE faction_name = ?")) {
            ps.setString(1, factionName);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = database.prepareStatement(
                "DELETE FROM factions WHERE name = ?")) {
            ps.setString(1, factionName);
            ps.executeUpdate();
        }
    }

    public String getFactionNameForInvite(String inviteeUUID) throws SQLException {
        String sql = "SELECT faction_name, expiry_date FROM faction_invites WHERE invitee_uuid = ?";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, inviteeUUID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp expiry = rs.getTimestamp("expiry_date");
                    if (expiry != null && expiry.before(new Timestamp(System.currentTimeMillis()))) {
                        removeInvite(rs.getString("faction_name"), inviteeUUID);
                        return null;
                    }
                    return rs.getString("faction_name");
                }
            }
        }
        return null;
    }

    public String getFactionNameForPlayer(String playerUUID) throws SQLException {
        String sql = "SELECT faction_name FROM faction_members WHERE member_uuid = ?";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("faction_name");
            }
        }
        return null;
    }

    public void addMemberToFaction(String factionName, String memberUUID, String memberName, String role) throws SQLException {
        String sql = "INSERT INTO faction_members (faction_name, member_uuid, member_name, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, factionName);
            pstmt.setString(2, memberUUID);
            pstmt.setString(3, memberName);
            pstmt.setString(4, role);
            pstmt.executeUpdate();
        }
    }

    public boolean factionExists(String name) throws SQLException {
        String sql = "SELECT id FROM factions WHERE name = ?";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void createFaction(String name, String leaderUUID, String leaderName, String colorHex) throws SQLException {
        String sql = "INSERT INTO factions (name, leader_uuid, leader_name, color) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, leaderUUID);
            pstmt.setString(3, leaderName);
            pstmt.setString(4, colorHex);
            pstmt.executeUpdate();
        }
    }


    public void removeMemberFromFaction(String playerUUID) throws SQLException {
        String sql = "DELETE FROM faction_members WHERE member_uuid = ?";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            pstmt.executeUpdate();
        }
    }

    public boolean isLeader(String playerUUID) throws SQLException {
        String sql = "SELECT id FROM factions WHERE leader_uuid = ?";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void transferLeadership(String factionName, String newLeaderUUID) throws SQLException {
        String sql = "UPDATE factions SET leader_uuid = ? WHERE name = ?";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, newLeaderUUID);
            pstmt.setString(2, factionName);
            pstmt.executeUpdate();
        }
    }

    public boolean isMemberOfFaction(String factionName, String playerName) throws SQLException {
        String sql = "SELECT 1 FROM faction_members WHERE faction_name = ? AND member_name = ?";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, factionName);
            pstmt.setString(2, playerName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<String> getMemberNamesOfFaction(String factionName) throws SQLException {
        List<String> memberNames = new ArrayList<>();
        String sql = "SELECT member_name FROM faction_members WHERE faction_name = ?";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, factionName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) memberNames.add(rs.getString("member_name"));
            }
        }
        return memberNames;
    }

    public int getJoinsToday(String factionName) throws SQLException {
        String sql =
                "SELECT COUNT(DISTINCT player_uuid) " +
                        "  FROM faction_sessions " +
                        " WHERE faction_name = ? " +
                        "   AND login_time >= (strftime('%s','now','localtime','start of day') * 1000)";
        try (PreparedStatement ps = database.prepareStatement(sql)) {
            ps.setString(1, factionName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<Map<String,Object>> getActivityOverTime(String factionName) throws SQLException {
        String sql =
                "SELECT " +
                        "  date(login_time/1000, 'unixepoch','localtime') AS date, " +
                        "  COUNT(*) AS count " +
                        "FROM faction_sessions " +
                        "WHERE faction_name = ? " +
                        "GROUP BY date " +
                        "ORDER BY date";
        List<Map<String,Object>> list = new ArrayList<>();
        try (PreparedStatement ps = database.prepareStatement(sql)) {
            ps.setString(1, factionName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> row = new HashMap<>();
                    row.put("date",  rs.getString("date"));
                    row.put("count", rs.getInt   ("count"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    public List<Map<String,Object>> getDailyPlaytime(String factionName) throws SQLException {
        String sql =
                "SELECT " +
                        "  date(login_time/1000, 'unixepoch','localtime') AS date, " +
                        "  SUM((logout_time - login_time) / 1000)               AS seconds " +
                        "FROM faction_sessions " +
                        "WHERE faction_name = ? " +
                        "  AND logout_time IS NOT NULL " +
                        "GROUP BY date " +
                        "ORDER BY date";

        List<Map<String,Object>> list = new ArrayList<>();
        try (PreparedStatement ps = database.prepareStatement(sql)) {
            ps.setString(1, factionName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> row = new HashMap<>();
                    row.put("date",    rs.getString("date"));
                    row.put("seconds", rs.getLong("seconds"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    public List<Map<String,Object>> getLastOnline(String factionName) throws SQLException {
        String sql =
                "SELECT " +
                        "  m.member_name AS name, " +
                        "  datetime(" +
                        "    MAX(COALESCE(s.logout_time, s.login_time)) / 1000," +
                        "    'unixepoch','localtime'" +
                        "  ) AS lastOnline " +
                        "FROM faction_sessions s " +
                        "  JOIN faction_members m ON s.player_uuid = m.member_uuid " +
                        "WHERE s.faction_name = ? " +
                        "GROUP BY s.player_uuid " +
                        "ORDER BY MAX(COALESCE(s.logout_time, s.login_time)) DESC";

        List<Map<String,Object>> list = new ArrayList<>();
        try (PreparedStatement ps = database.prepareStatement(sql)) {
            ps.setString(1, factionName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> row = new HashMap<>();
                    row.put("name",       rs.getString("name"));
                    row.put("lastOnline", rs.getString("lastOnline"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    public Map<String,Object> getMostActiveMember(String factionName) throws SQLException {
        String sql =
                "SELECT player_uuid, " +
                        "       SUM((logout_time - login_time) / 1000) AS seconds " +
                        "  FROM faction_sessions " +
                        " WHERE faction_name = ? " +
                        "   AND logout_time IS NOT NULL " +
                        " GROUP BY player_uuid " +
                        " ORDER BY seconds DESC " +
                        " LIMIT 1";
        try (PreparedStatement ps = database.prepareStatement(sql)) {
            ps.setString(1, factionName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String uuid = rs.getString("player_uuid");
                    long seconds = rs.getLong("seconds");

                    // Берём имя по UUID
                    String name = "-";
                    String q = "SELECT member_name FROM faction_members WHERE member_uuid = ?";
                    try (PreparedStatement ps2 = database.prepareStatement(q)) {
                        ps2.setString(1, uuid);
                        try (ResultSet r2 = ps2.executeQuery()) {
                            if (r2.next()) name = r2.getString("member_name");
                        }
                    }

                    Map<String,Object> result = new HashMap<>();
                    result.put("name",    name);
                    result.put("seconds", seconds);
                    return result;
                }
            }
        }
        // Дефолт, если нет ни одной законченной сессии
        Map<String,Object> fallback = new HashMap<>();
        fallback.put("name",    "-");
        fallback.put("seconds", 0L);
        return fallback;
    }

    public long getAverageOnlineSeconds(String factionName) throws SQLException {
        String sql =
                "SELECT ROUND(AVG(user_avg)) AS avgsec FROM (" +
                        "  SELECT player_uuid, AVG((logout_time - login_time) / 1000.0) AS user_avg " +
                        "    FROM faction_sessions " +
                        "   WHERE faction_name = ? " +
                        "     AND logout_time IS NOT NULL " +
                        "     AND (logout_time - login_time) >= ? " +      // <-- добавляем фильтр
                        "   GROUP BY player_uuid" +
                        ")";
        try (PreparedStatement ps = database.prepareStatement(sql)) {
            ps.setString(1, factionName);
            ps.setLong   (2, MIN_SESSION_MS);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("avgsec") : 0;
            }
        }
    }

    public void logSessionStart(String factionName, String playerUuid) {
        long now = System.currentTimeMillis();
        Connection conn = database.getConnection();

        try (
                PreparedStatement psLast = conn.prepareStatement(
                        "SELECT id, logout_time, login_time FROM faction_sessions " +
                                " WHERE player_uuid = ? " +
                                " ORDER BY login_time DESC LIMIT 1"
                )
        ) {
            psLast.setString(1, playerUuid);
            try (ResultSet rs = psLast.executeQuery()) {
                if (rs.next()) {
                    Timestamp tl = rs.getTimestamp("logout_time");
                    long   lid = rs.getLong("id");
                    if (tl != null && now - tl.getTime() <= MERGE_THRESHOLD_MS) {
                        try (PreparedStatement psUpd = conn.prepareStatement(
                                "UPDATE faction_sessions SET logout_time = NULL WHERE id = ?"
                        )) {
                            psUpd.setLong(1, lid);
                            psUpd.executeUpdate();
                            return;
                        }
                    }
                }
            }
        } catch (SQLException ignored) {}

        try (PreparedStatement ps = database.prepareStatement(
                "INSERT INTO faction_sessions (faction_name, player_uuid, login_time) VALUES (?,?,?)"
        )) {
            ps.setString(1, factionName);
            ps.setString(2, playerUuid);
            ps.setTimestamp(3, new Timestamp(now));
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void logSessionEnd(String playerUuid) {
        String sql = "UPDATE faction_sessions SET logout_time = ? WHERE player_uuid = ? AND logout_time IS NULL";
        try (PreparedStatement ps = database.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setString(2, playerUuid);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public Faction loadFaction(String name) throws SQLException {
        String sql = "SELECT id, name, leader_uuid, leader_name, creation_date, color FROM factions WHERE name = ?";
        try (PreparedStatement ps = database.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Faction f = new Faction();
                f.setId(rs.getInt("id"));
                f.setName(rs.getString("name"));
                f.setLeaderUuid(rs.getString("leader_uuid"));
                f.setLeaderName(rs.getString("leader_name"));
                f.setCreationDate(rs.getTimestamp("creation_date"));
                f.setColorHex(rs.getString("color"));
                return f;
            }
        }
    }

    public List<String> getAllFactionNames() throws SQLException {
        List<String> names = new ArrayList<>();
        String sql = "SELECT name FROM factions";
        try (PreparedStatement ps = database.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        }
        return names;
    }

    public void saveOrUpdateBlockStat(String playerUuid, String factionName, String blockType, int placed, int broken) {
        try (PreparedStatement ps = database.prepareStatement(
                "INSERT INTO faction_block_stats (player_uuid, faction_name, block_type, placed, broken) " +
                        "VALUES (?, ?, ?, ?, ?) " +
                        "ON CONFLICT(player_uuid, block_type) DO UPDATE SET " +
                        "placed = placed + EXCLUDED.placed, " +
                        "broken = broken + EXCLUDED.broken, " +
                        "faction_name = EXCLUDED.faction_name"
        )) {
            ps.setString(1, playerUuid);
            ps.setString(2, factionName);
            ps.setString(3, blockType);
            ps.setInt(4, placed);
            ps.setInt(5, broken);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
