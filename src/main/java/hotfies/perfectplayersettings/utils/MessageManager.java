package hotfies.perfectplayersettings.utils;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final PerfectPlayerSettings plugin;
    private final Map<Player, FileConfiguration> playerMessages = new HashMap<>();

    public MessageManager(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        loadMessages("Ru_ru");
    }

    public void loadMessages(String lang) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            langFile = new File(plugin.getDataFolder(), "lang/Ru_ru.yml");
        }
        playerMessages.put(null, YamlConfiguration.loadConfiguration(langFile));
    }

    public void loadMessages(Player player, String lang) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            langFile = new File(plugin.getDataFolder(), "lang/Ru_ru.yml");
        }
        playerMessages.put(player, YamlConfiguration.loadConfiguration(langFile));
    }

    public String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', playerMessages.getOrDefault(null, YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "lang/Ru_ru.yml"))).getString(key, ""));
    }

    public String getMessage(Player player, String key) {
        return ChatColor.translateAlternateColorCodes('&', playerMessages.getOrDefault(player, YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "lang/Ru_ru.yml"))).getString(key, ""));
    }

    public String getFormattedMessage(String key, String... placeholders) {
        String message = getMessage(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        return message;
    }

    public String getFormattedMessage(Player player, String key, String... placeholders) {
        String message = getMessage(player, key);
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        return message;
    }
}