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

public class MuteChatCommand extends Command {

    /*
     * This command mutes the global chat and prevents
     * players that do not have permission to bypass unable
     * to chat. This is useful when the chat is out-of-control
     * and players are being toxic. We have so far not ever used
     * this since our community is pretty friendly, but is here
     * just in case.
     */

    private final CorePlugin plugin;

    public MuteChatCommand(CorePlugin plugin) {
        super("mutechat");
        this.plugin = plugin;
        this.setUsage("Usage: /mutechat");
        this.setDescription("Mute the global chat.");
    }

    /**
     * Mutes the general chat and cancels all chat messages sent by players. The
     * format of the command is /mutechat.
     *
     * @param sender    The entity or object that sent the command.
     * @param label     The name of the server.
     * @param args      Any specific command arguments.
     * @return          Always true since we do not want the server to send the command format.
     */
    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(CorePermissions.SILENCE_CHAT_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return true;
        }

        if (args.length != 0) {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To mute the global chat, type " + ChatColor.WHITE + "/mutechat" + ChatColor.RED + ".");
        } else {
            /*
             * Checks if the chat is already muted or silenced. If the value
             * is currently true, then the value is reset to false.
             */
            if (PlayerManager.getInstance().isChatSilenced()) {
                PlayerManager.getInstance().setChatSilenced(false);
                this.plugin.getServer().broadcastMessage(StringUtil.PREFIX + ChatColor.GREEN + " The chat has been unmuted by " + (sender instanceof Player ? ((Player) sender).getDisplayName() : ChatColor.WHITE + "Console") + ChatColor.RESET + ChatColor.GREEN + ".");
            } else {
                PlayerManager.getInstance().setChatSilenced(true);
                this.plugin.getServer().broadcastMessage(StringUtil.PREFIX + ChatColor.RED + " The chat has been muted by " + (sender instanceof Player ? ((Player) sender).getDisplayName() : ChatColor.WHITE + "Console") + ChatColor.RED + ".");
            }
        }

        return true;
    }
}
