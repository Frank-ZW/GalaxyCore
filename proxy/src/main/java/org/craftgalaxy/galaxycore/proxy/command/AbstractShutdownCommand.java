package org.craftgalaxy.galaxycore.proxy.command;

import net.md_5.bungee.api.CommandSender;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;

public abstract class AbstractShutdownCommand {
    protected final CoreProxyPlugin plugin;
    protected final String permission;

    protected boolean hasPermission(CommandSender sender) {
        return this.permission == null || this.permission.isEmpty() || sender.hasPermission(this.permission);
    }

    public AbstractShutdownCommand(CoreProxyPlugin plugin, String permission) {
        this.plugin = plugin;
        this.permission = permission;
    }
}
