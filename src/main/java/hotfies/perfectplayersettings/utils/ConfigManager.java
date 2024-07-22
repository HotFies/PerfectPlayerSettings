package hotfies.perfectplayersettings.utils;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final PerfectPlayerSettings plugin;

    public ConfigManager(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        createLangFile();
    }

    private void createLangFile() {
        File langFile = new File(plugin.getDataFolder(), "lang/Ru_ru.yml");
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            plugin.saveResource("lang/Ru_ru.yml", false);
        }
    }
}