package hotfies.perfectplayersettings.commands;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import hotfies.perfectplayersettings.utils.DatabaseManager;
import hotfies.perfectplayersettings.utils.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatCommand implements CommandExecutor {

    private final PerfectPlayerSettings plugin;
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;

    public ChatCommand(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.pschat", true)) {
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT chat FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            boolean chatEnabled = true;
            if (resultSet.next()) {
                chatEnabled = resultSet.getBoolean("chat");
            }

            chatEnabled = !chatEnabled;

            try (PreparedStatement updateStatement = connection.prepareStatement(
                    "INSERT INTO player_settings (player_uuid, chat) VALUES (?, ?) ON DUPLICATE KEY UPDATE chat = ?")) {
                updateStatement.setString(1, player.getUniqueId().toString());
                updateStatement.setBoolean(2, chatEnabled);
                updateStatement.setBoolean(3, chatEnabled);
                updateStatement.executeUpdate();
            }

            if (chatEnabled) {
                player.sendMessage(messageManager.getFormattedMessage(player, "ChatEnabled", "%ps_prefix%", messageManager.getMessage(player, "Prefix")));
            } else {
                player.sendMessage(messageManager.getFormattedMessage(player, "ChatDisabled", "%ps_prefix%", messageManager.getMessage(player, "Prefix")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }
}