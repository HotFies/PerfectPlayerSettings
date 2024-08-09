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

public class PartyCommand implements CommandExecutor {

    private final PerfectPlayerSettings plugin;
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;

    public PartyCommand(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.pfparty", true)) {
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Асинхронное выполнение
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT get_party_invites FROM player_settings WHERE player_uuid = ?")) {
                statement.setString(1, player.getUniqueId().toString());
                ResultSet resultSet = statement.executeQuery();

                boolean getPartyInvites = true;
                if (resultSet.next()) {
                    getPartyInvites = resultSet.getBoolean("get_party_invites");
                }

                // Переключение значения
                getPartyInvites = !getPartyInvites;

                try (PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE player_settings SET get_party_invites = ? WHERE player_uuid = ?")) {
                    updateStatement.setBoolean(1, getPartyInvites);
                    updateStatement.setString(2, player.getUniqueId().toString());
                    updateStatement.executeUpdate();
                }

                boolean finalGetPartyInvites = getPartyInvites;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (finalGetPartyInvites) {
                        player.sendMessage(messageManager.getFormattedMessage(player, "PartyInvitesEnabled", "%pf_prefix%", messageManager.getMessage(player, "Prefix")));
                    } else {
                        player.sendMessage(messageManager.getFormattedMessage(player, "PartyInvitesDisabled", "%pf_prefix%", messageManager.getMessage(player, "Prefix")));
                    }
                });

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return true;
    }
}