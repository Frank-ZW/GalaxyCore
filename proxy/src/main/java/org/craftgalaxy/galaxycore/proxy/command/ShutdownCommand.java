package org.craftgalaxy.galaxycore.proxy.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.command.impl.CancelShutdownImpl;
import org.craftgalaxy.galaxycore.proxy.command.impl.ProxyShutdownImpl;
import org.craftgalaxy.galaxycore.proxy.command.impl.ServerShutdownImpl;
import org.craftgalaxy.galaxycore.proxy.command.impl.StatusShutdownImpl;
import org.craftgalaxy.galaxycore.proxy.command.types.AbstractProxyShutdown;
import org.craftgalaxy.galaxycore.proxy.command.types.AbstractQueryShutdown;
import org.craftgalaxy.galaxycore.proxy.command.types.AbstractServerShutdown;
import org.craftgalaxy.galaxycore.proxy.server.ServerData;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

import java.util.Map;

public class ShutdownCommand extends Command {

    private final CoreProxyPlugin plugin;
    private final Map<String, AbstractShutdownCommand> subcommands;

    public ShutdownCommand(CoreProxyPlugin plugin) {
        super("shutdown");
        this.plugin = plugin;
        this.subcommands = Map.of("cancel", new CancelShutdownImpl(plugin), "server", new ServerShutdownImpl(plugin), "proxy", new ProxyShutdownImpl(plugin), "status", new StatusShutdownImpl(plugin));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "This command does not exist. To shutdown the proxy or server, type /shutdown [proxy | server] <countdown>"));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "cancel" -> ((AbstractQueryShutdown) this.subcommands.get("cancel")).accept(sender, args);
            case "server" -> {
                ServerData serverData = this.plugin.getServerManager().getServerData(args[1]);
                if (serverData == null) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "The specified server does not exist."));
                    return;
                }

                ((AbstractServerShutdown) this.subcommands.get("server")).accept(sender, serverData, args);
            }

            case "status" -> ((AbstractQueryShutdown) this.subcommands.get("status")).accept(sender, args);
            case "proxy" -> ((AbstractProxyShutdown) this.subcommands.get("proxy")).accept(sender, args);
            default -> sender.sendMessage(new TextComponent(ChatColor.RED + "This command entered has an invalid subcommand."));
        }
    }
}
