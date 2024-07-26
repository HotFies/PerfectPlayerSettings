package hotfies.perfectplayersettings.commands;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import hotfies.perfectplayersettings.utils.DatabaseManager;
import hotfies.perfectplayersettings.utils.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LangCommand implements CommandExecutor {

    private final PerfectPlayerSettings plugin;
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;

    public LangCommand(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.pslang", true)) {
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            return true;
        }

        String selectedLang = args[0];
        Player player = (Player) sender;

        File langFile = new File(plugin.getDataFolder(), "lang/" + selectedLang + ".yml");
        if (!langFile.exists()) {
            player.sendMessage(messageManager.getFormattedMessage(player, "LangNotAvailable", "%ps_prefix%", messageManager.getMessage(player, "Prefix")));
            return true;
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT lang FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                try (PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE player_settings SET lang = ? WHERE player_uuid = ?")) {
                    updateStatement.setString(1, selectedLang);
                    updateStatement.setString(2, player.getUniqueId().toString());
                    updateStatement.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO player_settings (player_uuid, lang) VALUES (?, ?)")) {
                    insertStatement.setString(1, player.getUniqueId().toString());
                    insertStatement.setString(2, selectedLang);
                    insertStatement.executeUpdate();
                }
            }

            messageManager.loadMessages(player, selectedLang);
            player.sendMessage(messageManager.getFormattedMessage(player, "ChangeLangSucc", "%ps_prefix%", messageManager.getMessage(player, "Prefix"), "%ps_lang%", selectedLang));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }
}