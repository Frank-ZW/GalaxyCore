package org.craftgalaxy.galaxycore.proxy.listener;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.craftgalaxy.galaxycore.compat.Protocol;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.event.PlayerFullyConnectedEvent;
import org.craftgalaxy.galaxycore.proxy.player.PlayerData;
import org.craftgalaxy.galaxycore.proxy.server.ServerData;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

public record PlayerListeners(CoreProxyPlugin plugin) implements Listener {

    @EventHandler
    public void onPlayerPreConnect(PreLoginEvent e) {
        if (this.plugin.getServerManager().getProtocol() == Protocol.HANDSHAKE) {
            e.setCancelled(true);
            e.setCancelReason(StringUtil.EARLY_LOGIN);
        }
    }

    @EventHandler
    public void onPlayerConnect(PostLoginEvent e) {
        this.plugin.getPlayerManager().addPlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent e) {
        this.plugin.getPlayerManager().removePlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerFullyConnected(PlayerFullyConnectedEvent e) {
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(e.getPlayer());
        if (playerData != null) {
            playerData.sendPlayerLogin(e.getTo());
        }
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent e) {
        ServerData serverData = this.plugin.getServerManager().getServerData(e.getFrom());
        if (serverData != null) {
            serverData.handlePendingDisconnection(e.getPlayer());
        }
    }
}
