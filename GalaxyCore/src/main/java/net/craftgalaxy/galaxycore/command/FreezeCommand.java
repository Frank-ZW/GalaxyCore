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

public class FreezeCommand extends Command {

    /*
     * Freezes the specified player. Players that are frozen will
     * not be able to move around, place or break blocks, chat in game,
     * teleport around, and log off. If a player logs off, they are
     * instantly banned from the server.
     * Freezing a player is useful for catching hackers since it freezes
     * their client and prevents them from deleting any hacks or in-game
     * modifications.
     */

    private final CorePlugin plugin;

    public FreezeCommand(CorePlugin plugin) {
        super("freeze");
        this.plugin = plugin;
        this.setUsage("Usage: /freeze <player>");
        this.setDescription("Freeze a player.");
        this.setAliases(Collections.singletonList("ss"));
    }

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(CorePermissions.FREEZE_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To freeze a player, type " + ChatColor.WHITE + "/freeze <player>" + ChatColor.RED + ".");
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

            targetData.freezePlayer(sender);
        }

        return true;
    }
}
