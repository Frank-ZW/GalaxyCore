package org.craftgalaxy.galaxycore.client.command;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.data.ClientData;
import org.craftgalaxy.galaxycore.client.data.manager.PlayerManager;
import org.craftgalaxy.galaxycore.client.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public final class PingsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(StringUtil.PLAYERS_ONLY);
            return true;
        }

        ClientData playerData = PlayerManager.getInstance().getClientData(player);
        if (playerData == null) {
            player.kick(Component.text(StringUtil.NO_PLAYER_DATA));
            return true;
        }

        if (args.length != 0) {
            player.sendMessage(ChatColor.RED + "To toggle your chat pings, type /pings.");
        } else {
            if (playerData.isReceivePings()) {
                player.sendMessage(StringUtil.SERVER_PREFIX.append(Component.text(ChatColor.GREEN + "You have " + ChatColor.WHITE + "unsubscribed from in-game chat pings from players.")));
            } else {
                player.sendMessage(StringUtil.SERVER_PREFIX.append(Component.text(ChatColor.GREEN + "You have " + ChatColor.WHITE + "subscribed" + ChatColor.GREEN + " to in-game chat pings from players.")));
            }

            playerData.setReceivePings(!playerData.isReceivePings());
        }

        return true;
    }
}
