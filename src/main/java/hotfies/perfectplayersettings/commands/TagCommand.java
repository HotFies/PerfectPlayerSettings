package hotfies.perfectplayersettings.commands;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import hotfies.perfectplayersettings.utils.DatabaseManager;
import hotfies.perfectplayersettings.utils.MessageManager;
import hotfies.perfectplayersettings.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TagCommand implements CommandExecutor {

    private final PerfectPlayerSettings plugin;
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;
    private final ConfigManager configManager;

    public TagCommand(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.messageManager = plugin.getMessageManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.pftag", true)) {
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            return true;
        }

        Player player = (Player) sender;
        String selectedTag = args[0].toLowerCase();

        if (!player.hasPermission("perfectpf.tag." + selectedTag)) {
            player.sendMessage(messageManager.getFormattedMessage(player, "Permissions", "%pf_prefix%", messageManager.getMessage(player, "Prefix")));
            return true;
        }

        String tagValue = configManager.getTag(selectedTag);
        if (tagValue == null) {
            player.sendMessage(messageManager.getFormattedMessage(player, "TagNotExist", "%pf_prefix%", messageManager.getMessage(player, "Prefix")));
            return true;
        }

        String formattedTag = ChatColor.translateAlternateColorCodes('&', tagValue);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            databaseManager.setTag(player.getUniqueId().toString(), selectedTag);
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(messageManager.getFormattedMessage(player, "TagSet", "%pf_prefix%", messageManager.getMessage(player, "Prefix"), "%pf_tag%", formattedTag));
            });
        });

        return true;
    }
}