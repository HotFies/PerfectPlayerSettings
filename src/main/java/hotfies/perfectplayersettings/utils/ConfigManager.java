package hotfies.perfectplayersettings.utils;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final PerfectPlayerSettings plugin;
    private FileConfiguration tagConfig;
    private final Map<String, String> tags = new HashMap<>();

    public ConfigManager(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        createLangFile();
        createTagFile();
        loadTagConfig();
    }

    private void createLangFile() {
        File langFile = new File(plugin.getDataFolder(), "lang/Ru_ru.yml");
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            plugin.saveResource("lang/Ru_ru.yml", false);
        }
    }

    private void createTagFile() {
        File tagFile = new File(plugin.getDataFolder(), "tags.yml");
        if (!tagFile.exists()) {
            plugin.saveResource("tags.yml", false);
        }
    }

    private void loadTagConfig() {
        File tagFile = new File(plugin.getDataFolder(), "tags.yml");
        tagConfig = YamlConfiguration.loadConfiguration(tagFile);
        tags.clear();
        for (String key : tagConfig.getConfigurationSection("Tags").getKeys(false)) {
            tags.put(key, tagConfig.getString("Tags." + key));
        }
    }

    public String getTag(String key) {
        return tags.getOrDefault(key, tagConfig.getString("Default"));
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getDefaultTag() {
        return tagConfig.getString("Default");
    }
}