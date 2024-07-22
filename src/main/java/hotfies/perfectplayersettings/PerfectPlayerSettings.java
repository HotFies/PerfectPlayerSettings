package hotfies.perfectplayersettings;

import hotfies.perfectplayersettings.commands.*;
import hotfies.perfectplayersettings.listeners.ChatListener;
import hotfies.perfectplayersettings.listeners.PlayerJoinListener;
import hotfies.perfectplayersettings.utils.DatabaseManager;
import hotfies.perfectplayersettings.utils.ConfigManager;
import hotfies.perfectplayersettings.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class PerfectPlayerSettings extends JavaPlugin {

    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("[PerfectPlayerSettings] Загрузка плагина.");

        saveDefaultConfig();
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);

        databaseManager = new DatabaseManager(this);
        if (!databaseManager.connect()) {
            Bukkit.getConsoleSender().sendMessage("§c[PerfectPlayerSettings] Не удалось установить соединение с MySQL.");
            Bukkit.getConsoleSender().sendMessage("[PerfectPlayerSettings] Плагин не загружен.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getConsoleSender().sendMessage("[PerfectPlayerSettings] Подключение с MySQL установлено.");

        registerCommands();
        registerListeners();

        Bukkit.getConsoleSender().sendMessage("[PerfectPlayerSettings] Плагин успешно загружен.");
    }

    @Override
    public void onDisable() {
        databaseManager.disconnect();
        Bukkit.getConsoleSender().sendMessage("[PerfectPlayerSettings] Плагин отключен.");
    }

    private void registerCommands() {
        getCommand("psfly").setExecutor(new FlyCommand(this));
        getCommand("psvisibility").setExecutor(new VisibilityCommand(this));
        getCommand("pschat").setExecutor(new ChatCommand(this));
        getCommand("pslang").setExecutor(new LangCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}