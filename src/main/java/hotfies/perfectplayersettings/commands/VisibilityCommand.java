package hotfies.perfectplayersettings.commands;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import hotfies.perfectplayersettings.utils.DatabaseManager;
import hotfies.perfectplayersettings.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VisibilityCommand implements CommandExecutor {

    private final PerfectPlayerSettings plugin;
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;

    public VisibilityCommand(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT visibility FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            boolean visibilityEnabled = true;
            if (resultSet.next()) {
                visibilityEnabled = resultSet.getBoolean("visibility");
            }

            visibilityEnabled = !visibilityEnabled;

            try (PreparedStatement updateStatement = connection.prepareStatement(
                    "INSERT INTO player_settings (player_uuid, visibility) VALUES (?, ?) ON DUPLICATE KEY UPDATE visibility = ?")) {
                updateStatement.setString(1, player.getUniqueId().toString());
                updateStatement.setBoolean(2, visibilityEnabled);
                updateStatement.setBoolean(3, visibilityEnabled);
                updateStatement.executeUpdate();
            }

            if (visibilityEnabled) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    player.showPlayer(plugin, p);
                }
                player.sendMessage(messageManager.getFormattedMessage(player, "VisibilityEnabled", "%ps_prefix%", messageManager.getMessage(player, "Prefix")));
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    player.hidePlayer(plugin, p);
                }
                player.sendMessage(messageManager.getFormattedMessage(player, "VisibilityDisabled", "%ps_prefix%", messageManager.getMessage(player, "Prefix")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }
}