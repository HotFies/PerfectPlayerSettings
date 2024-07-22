package hotfies.perfectplayersettings.listeners;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import hotfies.perfectplayersettings.utils.DatabaseManager;
import hotfies.perfectplayersettings.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerJoinListener implements Listener {

    private final PerfectPlayerSettings plugin;
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;

    public PlayerJoinListener(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_settings WHERE player_uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                boolean fly = resultSet.getBoolean("fly");
                boolean visibility = resultSet.getBoolean("visibility");
                boolean chat = resultSet.getBoolean("chat");
                String lang = resultSet.getString("lang");
                String tag = resultSet.getString("tag");

                player.setAllowFlight(fly);
                if (!visibility) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        player.hidePlayer(plugin, p);
                    }
                } else {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        player.showPlayer(plugin, p);
                    }
                }

                messageManager.loadMessages(player, lang);
                // Загружаем тег игрока
                // Дополнительные действия, если необходимо

            } else {
                try (PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO player_settings (player_uuid, fly, visibility, chat, lang, tag) VALUES (?, false, true, true, 'Ru_ru', 'Default')")) {
                    insertStatement.setString(1, player.getUniqueId().toString());
                    insertStatement.executeUpdate();
                }

                messageManager.loadMessages(player, "Ru_ru");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}