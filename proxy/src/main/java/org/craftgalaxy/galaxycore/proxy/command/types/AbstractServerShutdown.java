package org.craftgalaxy.galaxycore.proxy.command.types;

import net.md_5.bungee.api.CommandSender;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.command.AbstractShutdownCommand;
import org.craftgalaxy.galaxycore.proxy.server.ServerData;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractServerShutdown extends AbstractShutdownCommand {

    public AbstractServerShutdown(CoreProxyPlugin plugin, String permission) {
        super(plugin, permission);
    }

    public abstract void accept(CommandSender sender, @NotNull ServerData serverData, String[] args);
}
