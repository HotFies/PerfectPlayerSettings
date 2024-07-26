package hotfies.perfectplayersettings.listeners;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import hotfies.perfectplayersettings.utils.DatabaseManager;
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

    public PlayerJoinListener(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String realNickname = player.getName();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_settings WHERE player_uuid = ?");
             PreparedStatement insertStatement = connection.prepareStatement(
                     "INSERT INTO player_settings (player_uuid, fly, visibility, chat, lang, tag, fake_nickname, real_nickname, color_nick) " +
                             "VALUES (?, false, true, true, 'Ru_ru', 'Default', ?, ?, '&7') " +
                             "ON DUPLICATE KEY UPDATE fake_nickname = IFNULL(fake_nickname, VALUES(fake_nickname)), real_nickname = IFNULL(real_nickname, VALUES(real_nickname))")) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                boolean fly = resultSet.getBoolean("fly");
                boolean visibility = resultSet.getBoolean("visibility");
                boolean chat = resultSet.getBoolean("chat");
                String lang = resultSet.getString("lang");
                String tag = resultSet.getString("tag");
                String fakeNickname = resultSet.getString("fake_nickname");
                String currentRealNickname = resultSet.getString("real_nickname");

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


                if (fakeNickname == null || currentRealNickname == null) {
                    insertStatement.setString(1, uuid);
                    insertStatement.setString(2, realNickname);
                    insertStatement.setString(3, realNickname);
                    insertStatement.executeUpdate();
                }

                plugin.getMessageManager().loadMessages(player, lang);
                // Загружаем тег игрока
                // Дополнительные действия, если необходимо

            } else {
                try (PreparedStatement insertNewPlayerStatement = connection.prepareStatement(
                        "INSERT INTO player_settings (player_uuid, fly, visibility, chat, lang, tag, fake_nickname, real_nickname, color_nick) VALUES (?, false, true, true, 'Ru_ru', 'Default', ?, ?, '&7')")) {
                    insertNewPlayerStatement.setString(1, uuid);
                    insertNewPlayerStatement.setString(2, realNickname);
                    insertNewPlayerStatement.setString(3, realNickname);
                    insertNewPlayerStatement.executeUpdate();
                }

                plugin.getMessageManager().loadMessages(player, "Ru_ru");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}