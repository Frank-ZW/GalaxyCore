package org.craftgalaxy.galaxycore.proxy.command.impl;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.command.types.AbstractServerShutdown;
import org.craftgalaxy.galaxycore.proxy.server.ServerData;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class ServerShutdownImpl extends AbstractServerShutdown {

    public ServerShutdownImpl(CoreProxyPlugin plugin) {
        super(plugin, StringUtil.SHUTDOWN_START_PERMISSION);
    }

    @Override
    public void accept(CommandSender sender, @NotNull ServerData serverData, String[] args) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return;
        }

        switch (args.length) {
            case 2 -> {
                serverData.scheduleShutdown(300);
                sender.sendMessage(new TextComponent(ChatColor.GREEN + "Started a new shutdown scheduler for " + serverData.getServer().getName() + ". If there was a pre-existing task, it has been over-written."));
            }

            case 3 -> {
                int countdown;
                try {
                    countdown = Integer.parseInt(args[2]);
                } catch (NumberFormatException var6) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "You must enter a valid whole number for the countdown."));
                    return;
                }

                if (countdown < 1) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "The countdown entered must be a positive, whole number."));
                    return;
                }

                serverData.scheduleShutdown(countdown);
                sender.sendMessage(new TextComponent(ChatColor.GREEN + "Started a new shutdown scheduler for " + serverData.getServer().getName() + ". If there was a pre-existing task, it has been over-written."));
            }

            default -> sender.sendMessage(new TextComponent(ChatColor.RED + "This command does not exist."));
        }
    }
}
