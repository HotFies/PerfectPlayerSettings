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
import java.util.Random;

public class NickCommand implements CommandExecutor {

    private final PerfectPlayerSettings plugin;
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;
    private final Random random = new Random();

    public NickCommand(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.pfnick", true)) {
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("perfectpf.nick")) {
            player.sendMessage(messageManager.getFormattedMessage(player, "Permissions", "%pf_prefix%", messageManager.getMessage(player, "Prefix")));
            return true;
        }

        String uuid = player.getUniqueId().toString();
        String realNickname = player.getName();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String fakeNickname = generateRandomNickname();

            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement checkStmt = connection.prepareStatement("SELECT fake_nickname, real_nickname FROM player_settings WHERE player_uuid = ?");
                 PreparedStatement updateStmt = connection.prepareStatement("UPDATE player_settings SET fake_nickname = ?, real_nickname = ? WHERE player_uuid = ?")) {

                checkStmt.setString(1, uuid);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    String currentFakeNickname = rs.getString("fake_nickname");
                    String currentRealNickname = rs.getString("real_nickname");

                    if (!currentFakeNickname.equals(currentRealNickname)) {
                        fakeNickname = currentRealNickname;
                        String finalFakeNickname = fakeNickname;
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.sendMessage(messageManager.getFormattedMessage(player, "NicknameReset", "%pf_prefix%", messageManager.getMessage(player, "Prefix"), "%pf_nickname%", finalFakeNickname));
                        });
                    } else {
                        String finalFakeNickname = fakeNickname;
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.sendMessage(messageManager.getFormattedMessage(player, "NicknameChanged", "%pf_prefix%", messageManager.getMessage(player, "Prefix"), "%pf_nickname%", finalFakeNickname));
                        });
                    }
                } else {
                    fakeNickname = realNickname;
                    String finalFakeNickname = fakeNickname;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(messageManager.getFormattedMessage(player, "NicknameReset", "%pf_prefix%", messageManager.getMessage(player, "Prefix"), "%pf_nickname%", finalFakeNickname));
                    });
                }

                updateStmt.setString(1, fakeNickname);
                updateStmt.setString(2, realNickname);
                updateStmt.setString(3, uuid);
                updateStmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return true;
    }

    private String generateRandomNickname() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int length = random.nextInt(6) + 5; // длина от 5 до 10 символов
        StringBuilder nickname = new StringBuilder();
        for (int i = 0; i < length; i++) {
            nickname.append(chars.charAt(random.nextInt(chars.length())));
        }
        return nickname.toString();
    }
}