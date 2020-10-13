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

public class PingsCommand extends Command {

    /*
     * This command toggles a player's in-game pings. Players
     * that have their pings disabled will not be notified
     * when their name appears in chat (and vice versa).
     */

    private final CorePlugin plugin;

    public PingsCommand(CorePlugin plugin) {
        super("pings");
        this.plugin = plugin;
        this.setUsage("Usage: /pings");
        this.setDescription("Enable or disable your in-game pings.");
    }

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(CorePermissions.PINGS_PERMISSION)) {
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

        if (args.length != 0) {
            player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To enable or disable your pings, type" + ChatColor.WHITE + " /pings" + ChatColor.RED + ".");
        } else {
            if (playerData.isReceivePings()) {
                player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " You have unsubscribed from in-game pings from players.");
                playerData.setReceivePings(false);
                return true;
            }

            player.sendMessage(StringUtil.PREFIX + ChatColor.GREEN + " You have subscribed to in-game pings from players.");
            playerData.setReceivePings(true);
        }

        return true;
    }
}
