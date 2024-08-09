package hotfies.perfectplayersettings.utils;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final PerfectPlayerSettings plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        try {
            // Инициализация HikariCP конфигурации
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + plugin.getConfig().getString("database.host") + ":" + plugin.getConfig().getInt("database.port") + "/" + plugin.getConfig().getString("database.name"));
            config.setUsername(plugin.getConfig().getString("database.user"));
            config.setPassword(plugin.getConfig().getString("database.password"));
            config.setMaximumPoolSize(10); // Настройка размера пула

            // Создание пула соединений
            dataSource = new HikariDataSource(config);

            // Проверка наличия таблицы и колонок
            try (Connection connection = getConnection()) {
                if (!isTableExists("player_settings", connection)) {
                    createPlayerSettingsTable(connection);
                } else {
                    addColumnsIfNotExist(connection);
                }
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isTableExists(String tableName, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SHOW TABLES LIKE ?")) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void createPlayerSettingsTable(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE TABLE player_settings (" +
                        "player_uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                        "fly BOOLEAN DEFAULT FALSE," +
                        "visibility BOOLEAN DEFAULT TRUE," +
                        "chat BOOLEAN DEFAULT TRUE," +
                        "lang VARCHAR(10) DEFAULT 'Ru_ru'," +
                        "tag VARCHAR(20) DEFAULT 'Default'," +
                        "fake_nickname VARCHAR(10)," +
                        "real_nickname VARCHAR(16)," +
                        "color_nick VARCHAR(19) DEFAULT '&7'," +
                        "get_party_invites BOOLEAN DEFAULT TRUE" + // Новый столбец
                        ")")) {
            statement.executeUpdate();
        }
    }

    private void addColumnsIfNotExist(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            if (!isColumnExists("player_settings", "fake_nickname", connection)) {
                statement.executeUpdate("ALTER TABLE player_settings ADD COLUMN fake_nickname VARCHAR(10)");
            }

            if (!isColumnExists("player_settings", "real_nickname", connection)) {
                statement.executeUpdate("ALTER TABLE player_settings ADD COLUMN real_nickname VARCHAR(16)");
            }

            if (!isColumnExists("player_settings", "color_nick", connection)) {
                statement.executeUpdate("ALTER TABLE player_settings ADD COLUMN color_nick VARCHAR(5) DEFAULT '&7'");
            }

            if (!isColumnExists("player_settings", "get_party_invites", connection)) {
                statement.executeUpdate("ALTER TABLE player_settings ADD COLUMN get_party_invites BOOLEAN DEFAULT TRUE");
            }
        }
    }

    private boolean isColumnExists(String tableName, String columnName, Connection connection) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
            return resultSet.next();
        }
    }

    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            connect();
        }
        return dataSource.getConnection();
    }

    // Методы для работы с настройками игроков

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

    public boolean getPartyInvites(String playerUUID) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT get_party_invites FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("get_party_invites");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true; // По умолчанию возвращаем true
    }

    public void setPartyInvites(String playerUUID, boolean getPartyInvites) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE player_settings SET get_party_invites = ? WHERE player_uuid = ?")) {
            statement.setBoolean(1, getPartyInvites);
            statement.setString(2, playerUUID);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}