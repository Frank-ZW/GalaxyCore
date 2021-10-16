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

public final class SlowChatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission(StringUtil.SLOWCHAT_COMMAND_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return true;
        }

        switch (args.length) {
            case 0 -> this.toggleSlowMode(sender, -1, true);
            case 1 -> {
                int cooldown;
                try {
                    cooldown = Integer.parseInt(args[0]);
                } catch (NumberFormatException var7) {
                    sender.sendMessage(ChatColor.RED + "The chat cooldown must be a whole number.");
                    return true;
                }

                if (cooldown < 1) {
                    sender.sendMessage(ChatColor.RED + "The chat cooldown must be a positive, whole number.");
                    return true;
                }

                this.toggleSlowMode(sender, cooldown, false);
            }
            default -> sender.sendMessage(ChatColor.RED + "To enable slow mode, type /slowmode [cooldown].");
        }

        return true;
    }

    private void toggleSlowMode(CommandSender sender, int cooldown, boolean defaultCooldown) {
        PlayerManager manager = PlayerManager.getInstance();
        if (manager.isSlowMode()) {
            Bukkit.broadcast(StringUtil.SERVER_PREFIX.append(Component.text(ChatColor.GREEN + "Slow mode has been disabled by " + ChatColor.WHITE + sender.getName() + ChatColor.GREEN + ".")));
            manager.setUsingDefault(true);
        } else {
            Bukkit.broadcast(StringUtil.SERVER_PREFIX.append(Component.text(ChatColor.GREEN + "Slow mode has been enabled by " + ChatColor.WHITE + sender.getName() + ChatColor.GREEN + ".")));
            manager.setUsingDefault(defaultCooldown);
            if (!defaultCooldown) {
                manager.setCustomCooldown(cooldown);
            }
        }

        manager.setSlowMode(!manager.isSlowMode());
    }
}
