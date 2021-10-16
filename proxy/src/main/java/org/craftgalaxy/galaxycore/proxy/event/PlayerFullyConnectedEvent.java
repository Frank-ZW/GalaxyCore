package org.craftgalaxy.galaxycore.proxy.event;

import lombok.AllArgsConstructor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

@AllArgsConstructor
public class PlayerFullyConnectedEvent extends Event {

    private final ProxiedPlayer player;
    private final ServerInfo to;

    public ProxiedPlayer getPlayer() {
        return this.player;
    }

    public ServerInfo getTo() {
        return this.to;
    }
}
