package hotfies.perfectplayersettings.placeholder;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import hotfies.perfectplayersettings.utils.DatabaseManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlaceholderIntegration extends PlaceholderExpansion {

    private final PerfectPlayerSettings plugin;
    private final DatabaseManager databaseManager;

    public PlaceholderIntegration(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
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
        return "perfectps";
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

        if (identifier.equals("tag")) {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT tag FROM player_settings WHERE player_uuid = ?")) {
                statement.setString(1, player.getUniqueId().toString());
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String tagKey = resultSet.getString("tag");
                    return plugin.getConfigManager().getTag(tagKey);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return plugin.getConfigManager().getDefaultTag();
        }

        return null;
    }
}