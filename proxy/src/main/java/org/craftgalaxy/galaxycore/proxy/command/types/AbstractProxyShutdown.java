package org.craftgalaxy.galaxycore.proxy.command.types;

import net.md_5.bungee.api.CommandSender;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.command.AbstractShutdownCommand;

public abstract class AbstractProxyShutdown extends AbstractShutdownCommand {

    public AbstractProxyShutdown(CoreProxyPlugin plugin, String permission) {
        super(plugin, permission);
    }

    public abstract void accept(CommandSender sender, String[] args);
}
