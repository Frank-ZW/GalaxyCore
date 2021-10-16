package org.craftgalaxy.galaxycore.proxy.command.types;

import net.md_5.bungee.api.CommandSender;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.command.AbstractShutdownCommand;

public abstract class AbstractQueryShutdown extends AbstractShutdownCommand {

    public AbstractQueryShutdown(CoreProxyPlugin plugin, String permission) {
        super(plugin, permission);
    }

    public abstract void accept(CommandSender sender, String[] args);
}