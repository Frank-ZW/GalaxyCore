package org.craftgalaxy.galaxycore.client.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public final class ClearChatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(StringUtil.CLEARCHAT_COMMAND_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return true;
        }

        switch (args.length) {
            case 0:
                this.clearChat(sender, true);
                break;
            case 1:
                if (args[0].equalsIgnoreCase("-s")) {
                    this.clearChat(sender, false);
                }

                break;
            default:
                sender.sendMessage(ChatColor.RED + "To clear the global chat, type /mutechat.");
        }

        return true;
    }

    private void clearChat(CommandSender sender, boolean display) {
        String message = ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "Craft" + ChatColor.BLUE + "Galaxy" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + "The chat has been cleared by " + ChatColor.LIGHT_PURPLE + sender.getName();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(StringUtil.CLEARCHAT_COMMAND_PERMISSION)) {
                player.sendMessage("");
                player.sendMessage("");
            } else {
                for(int i = 0; i < 100; i++) {
                    player.sendMessage("");
                }
            }

            if (display) {
                player.sendMessage(message);
            }
        }

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(message);
    }
}
