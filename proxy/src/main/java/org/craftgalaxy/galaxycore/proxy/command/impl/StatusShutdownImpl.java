package org.craftgalaxy.galaxycore.proxy.command.impl;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import org.craftgalaxy.galaxycore.compat.exception.CallbackReplaceException;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.command.types.AbstractQueryShutdown;
import org.craftgalaxy.galaxycore.proxy.runnable.Shutdownable;
import org.craftgalaxy.galaxycore.proxy.server.ServerData;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

public class StatusShutdownImpl extends AbstractQueryShutdown {

    public StatusShutdownImpl(CoreProxyPlugin plugin) {
        super(plugin, StringUtil.SHUTDOWN_STATUS_PERMISSION);
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return;
        }

        if (args.length == 2) {
            if ("proxy".equalsIgnoreCase(args[1])) {
                this.shutdownStatus(sender, this.plugin, "network");
            } else {
                ServerData serverData = this.plugin.getServerManager().getServerData(args[1]);
                if (serverData == null) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "A server with the specified name does not exist."));
                    return;
                }

                this.shutdownStatus(sender, serverData, serverData.getServer().getName());
            }
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + "To view the shutdown status for the proxy, type /shutdown status proxy. To view the shutdown status for a server, type /shutdown status <server>"));
        }
    }

    private void shutdownStatus(CommandSender sender, Shutdownable shutdownable, String name) {
        shutdownable.shutdownStatus(sender.getName(), (countdown, error) -> {
            if (error instanceof CallbackReplaceException) {
                sender.sendMessage(new TextComponent(error.getMessage()));
            } else if (error != null) {
                sender.sendMessage(new TextComponent(ChatColor.RED + "An unknown error occurred while getting the shutdown scheduled on " + name + ". Contact the developer if this occurs."));
            } else if (countdown == -1) {
                sender.sendMessage(new TextComponent(StringUtil.SERVER_PREFIX + ChatColor.YELLOW + " There is no shutdown schedule on the " + ChatColor.LIGHT_PURPLE + name + ChatColor.YELLOW + "."));
            } else {
                sender.sendMessage(new TextComponent(StringUtil.SERVER_PREFIX + ChatColor.YELLOW + " The " + ChatColor.LIGHT_PURPLE + name + ChatColor.YELLOW + " will shutdown in " + ChatColor.LIGHT_PURPLE + countdown + " second" + (countdown == 1 ? "" : "s") + ChatColor.YELLOW + "."));
            }
        });
    }
}
