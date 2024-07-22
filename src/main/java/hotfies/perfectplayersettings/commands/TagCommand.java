package hotfies.perfectplayersettings.commands;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import hotfies.perfectplayersettings.utils.DatabaseManager;
import hotfies.perfectplayersettings.utils.MessageManager;
import hotfies.perfectplayersettings.utils.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TagCommand implements CommandExecutor {

    private final PerfectPlayerSettings plugin;
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;
    private final ConfigManager configManager;

    public TagCommand(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.messageManager = plugin.getMessageManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Please specify a tag.");
            return true;
        }

        Player player = (Player) sender;
        String selectedTag = args[0];

        if (!player.hasPermission("perfectps.tag." + selectedTag)) {
            player.sendMessage(messageManager.getFormattedMessage(player, "Permissions", "%ps_prefix%", messageManager.getMessage(player, "Prefix")));
            return true;
        }

        String tagValue = configManager.getTag(selectedTag);
        if (tagValue == null) {
            player.sendMessage(messageManager.getFormattedMessage(player, "TagNotExist", "%ps_prefix%", messageManager.getMessage(player, "Prefix")));
            return true;
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT tag FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                try (PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE player_settings SET tag = ? WHERE player_uuid = ?")) {
                    updateStatement.setString(1, selectedTag);
                    updateStatement.setString(2, player.getUniqueId().toString());
                    updateStatement.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO player_settings (player_uuid, tag) VALUES (?, ?)")) {
                    insertStatement.setString(1, player.getUniqueId().toString());
                    insertStatement.setString(2, selectedTag);
                    insertStatement.executeUpdate();
                }
            }

            player.sendMessage(messageManager.getFormattedMessage(player, "TagSet", "%ps_prefix%", messageManager.getMessage(player, "Prefix"), "%ps_tag%", tagValue));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }
}