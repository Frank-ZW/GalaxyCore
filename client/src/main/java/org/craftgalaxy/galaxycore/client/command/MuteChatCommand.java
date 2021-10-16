package org.craftgalaxy.galaxycore.client.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.craftgalaxy.galaxycore.client.data.manager.PlayerManager;
import org.craftgalaxy.galaxycore.client.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public final class MuteChatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(StringUtil.MUTECHAT_COMMAND_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return true;
        }

        if (args.length == 0) {
            if (PlayerManager.getInstance().isSilenced()) {
                Bukkit.broadcast(StringUtil.SERVER_PREFIX.append(Component.text(ChatColor.GREEN + "The chat has been unmuted by " + ChatColor.WHITE + sender.getName() + ChatColor.GREEN + ".")));
            } else {
                Bukkit.broadcast(StringUtil.SERVER_PREFIX.append(Component.text(ChatColor.RED + "The chat has been muted by " + ChatColor.WHITE + sender.getName() + ChatColor.GREEN + ".")));
            }

            PlayerManager.getInstance().setSilenced(!PlayerManager.getInstance().isSilenced());
        } else {
            sender.sendMessage(ChatColor.RED + "To mute the global chat, type /mutechat.");
        }

        return true;
    }
}
