package hotfies.perfectplayersettings.utils;

import hotfies.perfectplayersettings.PerfectPlayerSettings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final PerfectPlayerSettings plugin;
    private Connection connection;

    public DatabaseManager(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        try {
            String host = plugin.getConfig().getString("database.host");
            int port = plugin.getConfig().getInt("database.port");
            String database = plugin.getConfig().getString("database.name");
            String user = plugin.getConfig().getString("database.user");
            String password = plugin.getConfig().getString("database.password");

            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database, user, password);

            if (!isTableExists("player_settings")) {
                createPlayerSettingsTable();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isTableExists(String tableName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String query = "SHOW TABLES LIKE '" + tableName + "'";
            return statement.executeQuery(query).next();
        }
    }

    private void createPlayerSettingsTable() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String createTableSQL = "CREATE TABLE player_settings (" +
                    "player_uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                    "fly BOOLEAN DEFAULT FALSE," +
                    "visibility BOOLEAN DEFAULT TRUE," +
                    "chat BOOLEAN DEFAULT TRUE," +
                    "lang VARCHAR(10) DEFAULT 'Ru_ru'" +
                    ")";
            statement.executeUpdate(createTableSQL);
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}