package org.craftgalaxy.galaxycore.client.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.craftgalaxy.galaxycore.client.connection.manager.ConnectionManager;
import org.craftgalaxy.galaxycore.client.data.ClientData;
import org.craftgalaxy.galaxycore.client.data.manager.PlayerManager;
import org.craftgalaxy.galaxycore.client.login.impl.BotCheckTask;
import org.craftgalaxy.galaxycore.client.util.StringUtil;
import org.craftgalaxy.galaxycore.compat.impl.PacketFullConnection;

public final class PlayerListeners implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrePlayerJoin(PlayerLoginEvent e) {
        Player player = e.getPlayer();
        if (e.getResult() == PlayerLoginEvent.Result.KICK_FULL && player.hasPermission(StringUtil.PLAYER_COUNT_BYPASS_PERMISSION)) {
            e.allow();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        ConnectionManager.getInstance().write(new PacketFullConnection(player.getUniqueId(), player.getName()));
        PlayerManager.getInstance().addPlayer(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        PlayerManager.getInstance().removePlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        ClientData clientData = PlayerManager.getInstance().getClientData(e.getPlayer());
        if (clientData != null) {
            if (clientData.isFrozen() && !e.getFrom().getBlock().equals(e.getTo().getBlock())) {
                e.setCancelled(true);
                return;
            }

            clientData.firePipeline(BotCheckTask.class, e);
        }
    }
}
