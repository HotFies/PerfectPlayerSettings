package hotfies.perfectplayersettings.utils;

import hotfies.perfectplayersettings.PerfectPlayerSettings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            } else {
                addColumnsIfNotExist();
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
                    "lang VARCHAR(10) DEFAULT 'Ru_ru'," +
                    "tag VARCHAR(20) DEFAULT 'Default'," +
                    "fake_nickname VARCHAR(10)," +
                    "real_nickname VARCHAR(16)," +
                    "color_nick VARCHAR(19) DEFAULT '&7'" +
                    ")";
            statement.executeUpdate(createTableSQL);
        }
    }

    private void addColumnsIfNotExist() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String query = "ALTER TABLE player_settings " +
                    "ADD COLUMN IF NOT EXISTS fake_nickname VARCHAR(10), " +
                    "ADD COLUMN IF NOT EXISTS real_nickname VARCHAR(16), " +
                    "ADD COLUMN IF NOT EXISTS color_nick VARCHAR(5) DEFAULT '&7'";
            statement.executeUpdate(query);
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

    public String getTag(String playerUUID) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT tag FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("tag");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Default";
    }

    public void setTag(String playerUUID, String tag) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE player_settings SET tag = ? WHERE player_uuid = ?")) {
            statement.setString(1, tag);
            statement.setString(2, playerUUID);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getFakeNickname(String playerUUID) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT fake_nickname FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("fake_nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setFakeNickname(String playerUUID, String fakeNickname) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE player_settings SET fake_nickname = ? WHERE player_uuid = ?")) {
            statement.setString(1, fakeNickname);
            statement.setString(2, playerUUID);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getRealNickname(String playerUUID) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT real_nickname FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("real_nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setRealNickname(String playerUUID, String realNickname) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE player_settings SET real_nickname = ? WHERE player_uuid = ?")) {
            statement.setString(1, realNickname);
            statement.setString(2, playerUUID);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getColorNick(String playerUUID) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT color_nick FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("color_nick");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "&7";
    }

    public void setColorNick(String playerUUID, String colorNick) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE player_settings SET color_nick = ? WHERE player_uuid = ?")) {
            statement.setString(1, colorNick);
            statement.setString(2, playerUUID);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}