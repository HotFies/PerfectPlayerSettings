package hotfies.perfectplayersettings.commands;

import hotfies.perfectplayersettings.PerfectPlayerSettings;
import hotfies.perfectplayersettings.utils.DatabaseManager;
import hotfies.perfectplayersettings.utils.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ColorNickCommand implements CommandExecutor {

    private final PerfectPlayerSettings plugin;
    private final DatabaseManager databaseManager;
    private final MessageManager messageManager;

    public ColorNickCommand(PerfectPlayerSettings plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.pscolornick", true)) {
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("perfectps.colornick")) {
            player.sendMessage(messageManager.getFormattedMessage(player, "Permissions", "%ps_prefix%", messageManager.getMessage(player, "Prefix")));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(messageManager.getFormattedMessage(player, "InvalidColorFormat", "%ps_prefix%", messageManager.getMessage(player, "Prefix")));
            return true;
        }

        String color = args[0];
        List<String> allowedColors = plugin.getConfigManager().getColorNickList();

        if (!allowedColors.contains(color)) {
            player.sendMessage(messageManager.getFormattedMessage(player, "ColorNotExist", "%ps_prefix%", messageManager.getMessage(player, "Prefix")));
            return true;
        }

        databaseManager.setColorNick(player.getUniqueId().toString(), color);
        player.sendMessage(messageManager.getFormattedMessage(player, "ColorNickChanged", "%ps_prefix%", messageManager.getMessage(player, "Prefix"), "%ps_color%", color));

        return true;
    }
}