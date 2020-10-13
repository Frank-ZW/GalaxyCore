package net.craftgalaxy.galaxycore.command;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.data.PlayerData;
import net.craftgalaxy.galaxycore.data.manager.PlayerManager;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Collections;

public class AdminChatCommand extends Command {

    /*
     * This command originally was an admin chat for the administrators of the server to
     * use to communicate game issues and handle disputes. Unfortunately, one of our other
     * plugins already has an admin chat which makes this command moot. :(
     */

    private final CorePlugin plugin;

    public AdminChatCommand(CorePlugin plugin) {
        super("adminchat");
        this.plugin = plugin;
        this.setUsage("Usage: /adminchat");
        this.setDescription("Send a message in admin chat.");
        this.setAliases(Collections.singletonList("achat"));
    }

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(CorePermissions.ADMIN_CHAT_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(StringUtil.PLAYER_ONLY);
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
        if (playerData == null) {
            player.sendMessage(StringUtil.ERROR_GETTING_DATA);
            return true;
        }

        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            if (args.length == 0) {
                if (playerData.isInAdminChat()) {
                    playerData.setInAdminChat(false);
                    player.sendMessage(StringUtil.PREFIX + ChatColor.GREEN + " You have been moved into the " + ChatColor.RED + "general chat" + ChatColor.GREEN + " channel.");
                } else {
                    playerData.setInAdminChat(true);
                    player.sendMessage(StringUtil.PREFIX + ChatColor.GREEN + " You have been moved into the " + ChatColor.RED + "Admin Chat" + ChatColor.GREEN + " channel.");
                }
            } else {
                player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To type a message in admin chat, type " + ChatColor.WHITE + "/adminchat" + ChatColor.RED + ".");
            }
        });

        return true;
    }
}
