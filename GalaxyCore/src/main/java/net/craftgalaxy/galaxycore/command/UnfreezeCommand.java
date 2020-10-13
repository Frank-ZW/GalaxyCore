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

public class UnfreezeCommand extends Command {

    /*
     * Unfreezes the player specified regardless if they are a staff member or default player. This command
     * can only be sent through console.
     */

    private final CorePlugin plugin;

    public UnfreezeCommand(CorePlugin plugin) {
        super("unfreeze");
        this.plugin = plugin;
        this.setUsage("Usage: /unfreeze <player>");
        this.setDescription("Unfreeze a player.");
    }

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(CorePermissions.FREEZE_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To unfreeze a player, type " + ChatColor.WHITE + "/unfreeze <player>" + ChatColor.RED + ".");
        } else {
            Player target = this.plugin.getServer().getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(StringUtil.PLAYER_OFFLINE);
                return true;
            }

            PlayerData targetData = PlayerManager.getInstance().getPlayerData(target);
            if (targetData == null) {
                sender.sendMessage(StringUtil.ERROR_GETTING_DATA);
                return true;
            }

            targetData.unfreezePlayer(sender);
        }

        return true;
    }
}
