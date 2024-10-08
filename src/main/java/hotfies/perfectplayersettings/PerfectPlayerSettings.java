package hotfies.perfectplayersettings;

import hotfies.perfectplayersettings.commands.*;
import hotfies.perfectplayersettings.listeners.ChatListener;
import hotfies.perfectplayersettings.listeners.PlayerJoinListener;
import hotfies.perfectplayersettings.placeholder.PlaceholderIntegration;
import hotfies.perfectplayersettings.utils.DatabaseManager;
import hotfies.perfectplayersettings.utils.MessageManager;
import hotfies.perfectplayersettings.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PerfectPlayerSettings extends JavaPlugin {

    private DatabaseManager databaseManager;
    private MessageManager messageManager;
    private ConfigManager configManager;

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

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderIntegration(this).register();
        }

        Bukkit.getConsoleSender().sendMessage("[PerfectPlayerSettings] Плагин успешно загружен.");
    }

    @Override
    public void onDisable() {
        databaseManager.disconnect();
        Bukkit.getConsoleSender().sendMessage("[PerfectPlayerSettings] Плагин отключен.");
    }

    private void registerCommands() {
        registerCommand("pffly", new FlyCommand(this));
        registerCommand("pfvisibility", new VisibilityCommand(this));
        registerCommand("pfchat", new ChatCommand(this));
        registerCommand("pflang", new LangCommand(this));
        registerCommand("pftag", new TagCommand(this));
        registerCommand("pfnick", new NickCommand(this));
        registerCommand("pfcolornick", new ColorNickCommand(this));
        registerCommand("pfparty", new PartyCommand(this));
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}