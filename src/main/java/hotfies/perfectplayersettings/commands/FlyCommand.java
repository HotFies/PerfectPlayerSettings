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

public class FlyCommand implements CommandExecutor {

    private final PerfectPlayerSettings plugin;
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;

    public FlyCommand(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.pffly", true)) {
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("perfectpf.fly")) {
            player.sendMessage(messageManager.getFormattedMessage(player, "Permissions", "%pf_prefix%", messageManager.getMessage(player, "Prefix")));
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT fly FROM player_settings WHERE player_uuid = ?")) {
                statement.setString(1, player.getUniqueId().toString());
                ResultSet resultSet = statement.executeQuery();

                boolean flyEnabled = false;
                if (resultSet.next()) {
                    flyEnabled = resultSet.getBoolean("fly");
                }

                flyEnabled = !flyEnabled;

                try (PreparedStatement updateStatement = connection.prepareStatement(
                        "INSERT INTO player_settings (player_uuid, fly) VALUES (?, ?) ON DUPLICATE KEY UPDATE fly = ?")) {
                    updateStatement.setString(1, player.getUniqueId().toString());
                    updateStatement.setBoolean(2, flyEnabled);
                    updateStatement.setBoolean(3, flyEnabled);
                    updateStatement.executeUpdate();
                }

                // Все действия с Bukkit API нужно делать синхронно
                boolean finalFlyEnabled = flyEnabled;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (finalFlyEnabled) {
                        player.setAllowFlight(true);
                        player.sendMessage(messageManager.getFormattedMessage(player, "FlyEnabled", "%pf_prefix%", messageManager.getMessage(player, "Prefix")));
                    } else {
                        player.setAllowFlight(false);
                        player.sendMessage(messageManager.getFormattedMessage(player, "FlyDisabled", "%pf_prefix%", messageManager.getMessage(player, "Prefix")));
                    }
                });

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return true;
    }
}