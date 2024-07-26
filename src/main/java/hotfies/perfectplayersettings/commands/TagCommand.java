package hotfies.perfectplayersettings.commands;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import hotfies.perfectplayersettings.utils.DatabaseManager;
import hotfies.perfectplayersettings.utils.MessageManager;
import hotfies.perfectplayersettings.utils.ConfigManager;
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
        if (!plugin.getConfig().getBoolean("commands.pstag", true)) {
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

        if (!player.hasPermission("perfectps.tag." + selectedTag)) {
            player.sendMessage(messageManager.getFormattedMessage(player, "Permissions", "%ps_prefix%", messageManager.getMessage(player, "Prefix")));
            return true;
        }

        String tagValue = configManager.getTag(selectedTag);
        if (tagValue == null) {
            player.sendMessage(messageManager.getFormattedMessage(player, "TagNotExist", "%ps_prefix%", messageManager.getMessage(player, "Prefix")));
            return true;
        }

        // Форматируем тег для отображения в чате
        String formattedTag = ChatColor.translateAlternateColorCodes('&', tagValue);
        databaseManager.setTag(player.getUniqueId().toString(), selectedTag);
        player.sendMessage(messageManager.getFormattedMessage(player, "TagSet", "%ps_prefix%", messageManager.getMessage(player, "Prefix"), "%ps_tag%", formattedTag));

        return true;
    }
}