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

public class AlertsCommand extends Command {

    /*
     * Command to toggle a player's ability to receive anticheat alerts.
     * Since our server does not (at the moment) have an anticheat, this
     * command has no use.
     */

    private final CorePlugin plugin;

    public AlertsCommand(CorePlugin plugin) {
        super("alerts");
        this.plugin = plugin;
        this.setDescription("Usage: /alerts");
        this.setUsage("Enable or disable anticheat alerts.");
    }

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(CorePermissions.ALERTS_PERMISSION)) {
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

        if (playerData.isReceiveAlerts()) {
            player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " You have unsubscribed from anticheat alerts.");
            playerData.setReceiveAlerts(false);
            return true;
        }

        player.sendMessage(StringUtil.PREFIX + ChatColor.GREEN + " You have subscribed to anticheat alerts.");
        playerData.setReceiveAlerts(true);
        return true;
    }
}
