package org.craftgalaxy.galaxycore.proxy.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

import java.util.Map;
import java.util.Set;

public final class ServerStatusCommand extends Command {

    private final CoreProxyPlugin plugin;

    public ServerStatusCommand(CoreProxyPlugin plugin) {
        super("serverstatus", StringUtil.SERVER_STATUS_PERMISSION);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return;
        }

        if (args.length != 0) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "To view the status of every single server in the network, type /serverstatus."));
            return;
        }

        int index = 0;
        StringBuilder builder = new StringBuilder();
        Set<Map.Entry<String, ServerInfo>> entries = this.plugin.getProxy().getServersCopy().entrySet();
        for (Map.Entry<String, ServerInfo> entry : entries) {
            if (entry.getValue() == null) {
                builder.append(entry.getKey()).append(ChatColor.RED).append(" ●");
                continue;
            }

            builder.append(entry.getKey()).append(this.plugin.getServerManager().isConnected(entry.getKey()) ? ChatColor.GREEN : ChatColor.RED).append(" ●");
            if (index++ != entries.size() - 1) {
                builder.append(ChatColor.YELLOW).append(", ");
            }
        }

        sender.sendMessage(new TextComponent(StringUtil.SERVER_PREFIX + ChatColor.YELLOW + " " + builder));
    }
}

