package hotfies.perfectplayersettings.placeholder;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import hotfies.perfectplayersettings.utils.DatabaseManager;
import hotfies.perfectplayersettings.utils.MessageManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlaceholderIntegration extends PlaceholderExpansion {

    private final PerfectPlayerSettings plugin;
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;

    public PlaceholderIntegration(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "pf";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        switch (identifier) {
            case "tag":
                return getTag(player);
            case "fakenick":
                return getFakeNickname(player);
            case "colornick":
                return getColorNick(player);
            case "status_pffly":
                return getStatusFly(player);
            case "status_pfvisibility":
                return getStatusVisibility(player);
            case "status_pfchat":
                return getStatusChat(player);
            case "pflang":
                return getLanguage(player);
            case "status_pfparty":
                return getStatusParty(player);
            case "realnick":
                return getRealNickname(player);
            default:
                return null;
        }
    }

    private String getStatusFly(Player player) {
        return getStatusFromDatabase(player, "fly");
    }

    private String getStatusVisibility(Player player) {
        return getStatusFromDatabase(player, "visibility");
    }

    private String getStatusChat(Player player) {
        return getStatusFromDatabase(player, "chat");
    }

    private String getStatusParty(Player player) {
        return getStatusFromDatabase(player, "get_party_invites");
    }

    private String getLanguage(Player player) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT lang FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("lang");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private String getRealNickname(Player player) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT real_nickname FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("real_nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return player.getName();
    }

    private String getStatusFromDatabase(Player player, String column) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT " + column + " FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                boolean status = resultSet.getBoolean(column);
                return status ? getEnabledStatus(player) : getDisabledStatus(player);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private String getEnabledStatus(Player player) {
        return messageManager.getMessage(player, "StatusEnabled");
    }

    private String getDisabledStatus(Player player) {
        return messageManager.getMessage(player, "StatusDisabled");
    }

    private String getTag(Player player) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT tag FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String tagKey = resultSet.getString("tag");
                String tag = plugin.getConfigManager().getTag(tagKey);
                return ChatColor.translateAlternateColorCodes('&', tag);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getDefaultTag());
    }

    private String getFakeNickname(Player player) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT fake_nickname FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return ChatColor.translateAlternateColorCodes('&', resultSet.getString("fake_nickname"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return player.getName();
    }

    private String getColorNick(Player player) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT color_nick FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return ChatColor.translateAlternateColorCodes('&', resultSet.getString("color_nick"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ChatColor.translateAlternateColorCodes('&', "&7");
    }
}