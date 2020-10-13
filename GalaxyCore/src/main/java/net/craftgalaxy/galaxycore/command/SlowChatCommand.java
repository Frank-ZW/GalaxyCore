package net.craftgalaxy.galaxycore.command;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.data.manager.PlayerManager;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class SlowChatCommand extends Command {

    private final CorePlugin plugin;

    public SlowChatCommand(CorePlugin plugin) {
        super("slowchat");
        this.plugin = plugin;
        this.setUsage("Usage: /slowchat");
        this.setDescription("Slow the general chat.");
    }

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(CorePermissions.SLOW_CHAT_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return true;
        }

        if (PlayerManager.getInstance().isChatSlowed()) {
            PlayerManager.getInstance().setChatSlowed(false);
            this.plugin.getServer().broadcastMessage(StringUtil.PREFIX + ChatColor.GREEN + " Slow mode has been disabled by " + (sender instanceof Player ? ((Player) sender).getDisplayName() : ChatColor.WHITE + "Console") + ChatColor.GREEN + ".");
        } else {
            PlayerManager.getInstance().setChatSlowed(true);
            this.plugin.getServer().broadcastMessage(StringUtil.PREFIX + ChatColor.RED + " Slow mode has been enabled by " + (sender instanceof Player ? ((Player) sender).getDisplayName() : ChatColor.WHITE + "Console") + ChatColor.RED + ".");
        }

        return true;
    }
}
