package org.degree.factions.database;

import org.degree.factions.Factions;

import java.io.File;
import java.sql.*;

public class Database {
    private Connection connection;

    public Database() {
        setupConnection();
    }

    private void setupConnection() {
        try {
            String databasePath = Factions.getInstance().getDataFolder() + "/factions.db";
            File pluginDir = Factions.getInstance().getDataFolder();
            if (!pluginDir.exists()) pluginDir.mkdirs();

            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS factions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL UNIQUE," +
                        "leader_uuid TEXT NOT NULL," +
                        "leader_name TEXT NOT NULL," +
                        "creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "color TEXT NOT NULL DEFAULT '#FFFFFF'" +
                        ");");


                stmt.execute("CREATE TABLE IF NOT EXISTS faction_members (" +
                        "faction_name TEXT NOT NULL," +
                        "member_uuid TEXT NOT NULL," +
                        "member_name TEXT NOT NULL," +
                        "role TEXT NOT NULL," +
                        "FOREIGN KEY(faction_name) REFERENCES factions(name) ON DELETE CASCADE" +
                        ");");

                stmt.execute("CREATE TABLE IF NOT EXISTS faction_invites (" +
                        "invitee_uuid TEXT NOT NULL," +
                        "inviter_uuid TEXT NOT NULL," +
                        "faction_name TEXT NOT NULL," +
                        "invite_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "expiry_date TIMESTAMP NOT NULL," +
                        "PRIMARY KEY(invitee_uuid, faction_name)" +
                        ");");

                stmt.execute("CREATE TABLE IF NOT EXISTS faction_sessions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "faction_name TEXT NOT NULL," +
                        "player_uuid TEXT NOT NULL," +
                        "login_time TIMESTAMP NOT NULL," +
                        "logout_time TIMESTAMP" +
                        ");");

                stmt.execute("CREATE TABLE IF NOT EXISTS faction_kill_stats (" +
                        "player_uuid TEXT NOT NULL," +
                        "faction_name TEXT NOT NULL," +
                        "kills INTEGER NOT NULL DEFAULT 0," +
                        "PRIMARY KEY (player_uuid)" +
                        ");");

                stmt.execute("CREATE TABLE IF NOT EXISTS faction_block_stats (" +
                        "player_uuid  TEXT NOT NULL," +
                        "faction_name TEXT NOT NULL," +
                        "block_type   TEXT NOT NULL," +
                        "placed       INTEGER NOT NULL DEFAULT 0," +
                        "broken       INTEGER NOT NULL DEFAULT 0," +
                        "PRIMARY KEY (player_uuid, block_type)" +
                        ");");

            }

            Factions.getInstance().getLogger().info("SQLite database setup completed.");
        } catch (SQLException e) {
            Factions.getInstance().getLogger().severe("Could not set up SQLite database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                setupConnection();
            }
        } catch (SQLException e) {
            Factions.getInstance().getLogger().severe("Error checking DB connection: " + e.getMessage());
        }
        return connection;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return getConnection().prepareStatement(sql);
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                Factions.getInstance().getLogger().info("Database connection closed.");
            } catch (SQLException e) {
                Factions.getInstance().getLogger().severe("Error while closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
