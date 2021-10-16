package org.craftgalaxy.galaxycore.proxy.command.impl;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.command.types.AbstractQueryShutdown;
import org.craftgalaxy.galaxycore.proxy.server.ServerData;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

public class CancelShutdownImpl extends AbstractQueryShutdown {

    public CancelShutdownImpl(CoreProxyPlugin plugin) {
        super(plugin, StringUtil.SHUTDOWN_CANCEL_PERMISSION);
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return;
        }

        switch(args.length) {
            case 1:
                this.plugin.cancelShutdown(sender.getName());
                this.plugin.getServerManager().acceptToAll(serverData -> serverData.cancelShutdown(sender.getName()));
                sender.sendMessage(new TextComponent(ChatColor.GREEN + "All pending shutdown tasks have been cancelled!"));
                break;
            case 2:
                if ("proxy".equalsIgnoreCase(args[1])) {
                    sender.sendMessage(new TextComponent(ChatColor.GREEN + "If there was a shutdown timer scheduled for the Proxy, it has been cancelled."));
                } else {
                    ServerData serverData = this.plugin.getServerManager().getServerData(args[1]);
                    if (serverData == null) {
                        sender.sendMessage(new TextComponent(ChatColor.RED + "A server with the specified name does not exist."));
                        return;
                    }

                    serverData.cancelShutdown(sender.getName());
                    sender.sendMessage(new TextComponent(ChatColor.GREEN + "If there was a shutdown timer scheduled for " + serverData.getServer().getName() + ", it has been cancelled."));
                }
                break;
            default:
                sender.sendMessage(new TextComponent(ChatColor.RED + "To cancel a pre-existing shutdown task, type /shutdown cancel [server]"));
        }
    }
}
