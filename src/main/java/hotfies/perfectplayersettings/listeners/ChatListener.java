package hotfies.perfectplayersettings.listeners;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import hotfies.perfectplayersettings.utils.DatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class ChatListener implements Listener {

    private final PerfectPlayerSettings plugin;
    private final DatabaseManager databaseManager;

    public ChatListener(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Iterator<Player> iterator = event.getRecipients().iterator();
        while (iterator.hasNext()) {
            Player recipient = iterator.next();
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT chat FROM player_settings WHERE player_uuid = ?")) {
                statement.setString(1, recipient.getUniqueId().toString());
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next() && !resultSet.getBoolean("chat")) {
                    iterator.remove();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}