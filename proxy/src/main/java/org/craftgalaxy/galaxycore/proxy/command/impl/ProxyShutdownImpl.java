package org.craftgalaxy.galaxycore.proxy.command.impl;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.command.types.AbstractProxyShutdown;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

public class ProxyShutdownImpl extends AbstractProxyShutdown {

    public ProxyShutdownImpl(CoreProxyPlugin plugin) {
        super(plugin, StringUtil.SHUTDOWN_START_PERMISSION);
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return;
        }

        switch (args.length) {
            case 1 -> this.plugin.scheduleShutdown(300);
            case 2 -> {
                int countdown;
                try {
                    countdown = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "You must enter a valid whole number for the countdown."));
                    return;
                }
                if (countdown < 1) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "The countdown entered must be a positive, whole number."));
                    return;
                }

                this.plugin.scheduleShutdown(countdown);
            }
        }
    }
}
