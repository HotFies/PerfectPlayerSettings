package hotfies.perfectplayersettings.utils;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final PerfectPlayerSettings plugin;
    private FileConfiguration tagConfig;
    private FileConfiguration colorConfig;
    private final Map<String, String> tags = new HashMap<>();
    private List<String> colorNickList;

    public ConfigManager(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        createLangFile();
        createTagFile();
        createColorFile();
        loadTagConfig();
        loadColorConfig();
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

    private void createColorFile() {
        File colorFile = new File(plugin.getDataFolder(), "color.yml");
        if (!colorFile.exists()) {
            plugin.saveResource("color.yml", false);
        }
    }

    private void loadTagConfig() {
        File tagFile = new File(plugin.getDataFolder(), "tags.yml");
        tagConfig = YamlConfiguration.loadConfiguration(tagFile);
        tags.clear();
        for (String key : tagConfig.getConfigurationSection("Tags").getKeys(false)) {
            tags.put(key.toLowerCase(), tagConfig.getString("Tags." + key));
        }
    }

    private void loadColorConfig() {
        File colorFile = new File(plugin.getDataFolder(), "color.yml");
        colorConfig = YamlConfiguration.loadConfiguration(colorFile);
        colorNickList = colorConfig.getStringList("Color_nick");
    }

    public String getTag(String key) {
        return tags.getOrDefault(key.toLowerCase(), null);
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getDefaultTag() {
        return tagConfig.getString("Default");
    }

    public List<String> getColorNickList() {
        return colorNickList;
    }
}