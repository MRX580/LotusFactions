package org.degree.faction.database;

import org.degree.faction.Faction;

import java.io.File;
import java.sql.*;

public class Database {

    private Connection connection;

    public Database() {
        setupConnection();
    }

    private void setupConnection() {
        try {
            // Получаем путь к базе данных
            String databasePath = Faction.getInstance().getDataFolder() + "/factions.db";

            // Убедитесь, что директория плагина существует
            File pluginDir = Faction.getInstance().getDataFolder();
            if (!pluginDir.exists()) {
                pluginDir.mkdirs();  // Создаем директорию, если её нет
            }

            // Подключение к базе данных
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);

            // Создание таблиц, если их ещё нет
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS factions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL UNIQUE," +
                        "leader_uuid TEXT NOT NULL," +
                        "leader_name TEXT NOT NULL," +
                        "creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
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
            }

            Faction.getInstance().getLogger().info("SQLite database setup completed.");

        } catch (SQLException e) {
            Faction.getInstance().getLogger().severe("Could not set up SQLite database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (connection == null || connection.isClosed()) {
            setupConnection(); // Восстанавливаем подключение, если оно закрыто
        }
        return connection.prepareStatement(sql);
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                Faction.getInstance().getLogger().info("Database connection closed.");
            } catch (SQLException e) {
                Faction.getInstance().getLogger().severe("Error while closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
