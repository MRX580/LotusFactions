package org.degree.faction.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FactionDatabase {

    private final Database database = new Database();

    public void addInvite(String factionName, String inviterUUID, String inviteeUUID) throws SQLException {
        // Удаляем старые приглашения для этого игрока
        removeAllInvitesForPlayer(inviteeUUID);

        String sql = "INSERT INTO faction_invites (faction_name, inviter_uuid, invitee_uuid, invite_date, expiry_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, factionName);
            pstmt.setString(2, inviterUUID);
            pstmt.setString(3, inviteeUUID);
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis() + 86400000)); // 24 часа
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

    public String getFactionNameForInvite(String inviteeUUID) throws SQLException {
        String sql = "SELECT faction_name, expiry_date FROM faction_invites WHERE invitee_uuid = ?";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, inviteeUUID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Timestamp expiryDate = rs.getTimestamp("expiry_date");
                if (expiryDate != null && expiryDate.before(new Timestamp(System.currentTimeMillis()))) {
                    // Удаляем истекшее приглашение
                    removeInvite(rs.getString("faction_name"), inviteeUUID);
                    return null;
                }
                return rs.getString("faction_name");
            }
            return null;
        }
    }

    public String getFactionNameForPlayer(String playerUUID) throws SQLException {
        String sql = "SELECT faction_name FROM faction_members WHERE member_uuid = ?";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("faction_name");
            }
        }
        return null;
    }

    public void addMemberToFaction(String factionName, String memberUUID, String memberName, String role) throws SQLException {
        String sql = "INSERT INTO faction_members (faction_name, member_uuid, member_name, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, factionName);
            pstmt.setString(2, memberUUID);
            pstmt.setString(3, memberName); // Сохраняем никнейм игрока
            pstmt.setString(4, role);
            pstmt.executeUpdate();
        }
    }

    public boolean factionExists(String name) throws SQLException {
        String sql = "SELECT id FROM factions WHERE name = ?";
        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    public void createFaction(String name, String leaderUUID, String leaderName) throws SQLException {
        String sql = "INSERT INTO factions (name, leader_uuid, leader_name) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, leaderUUID);
            pstmt.setString(3, leaderName);
            pstmt.executeUpdate();
        }

        addMemberToFaction(name, leaderUUID, leaderName, "LEADER");
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
            return pstmt.executeQuery().next();
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
            pstmt.setString(2, playerName); // Проверка по имени игрока
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    public List<String> getMemberNamesOfFaction(String factionName) throws SQLException {
        List<String> memberNames = new ArrayList<>();
        String sql = "SELECT member_name FROM faction_members WHERE faction_name = ?";

        try (PreparedStatement pstmt = database.prepareStatement(sql)) {
            pstmt.setString(1, factionName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                memberNames.add(rs.getString("member_name"));
            }
        }

        return memberNames;
    }
}
