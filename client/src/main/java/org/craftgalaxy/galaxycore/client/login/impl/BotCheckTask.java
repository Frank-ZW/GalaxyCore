package org.craftgalaxy.galaxycore.client.login.impl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerMoveEvent;
import org.craftgalaxy.galaxycore.client.connection.manager.ConnectionManager;
import org.craftgalaxy.galaxycore.client.data.ClientData;
import org.craftgalaxy.galaxycore.client.login.PlayerLoginTask;
import org.craftgalaxy.galaxycore.compat.impl.PacketBotCheck;

public class BotCheckTask extends PlayerLoginTask {

    public boolean apply(ClientData clientData, Object o) {
        if (o instanceof PlayerMoveEvent e) {
            if (!clientData.getStart().equals(e.getTo())) {
                ConnectionManager.getInstance().write(new PacketBotCheck(clientData.getUniqueId()));
                Bukkit.getLogger().info(ChatColor.GREEN + "Removed bot checked handler from " + clientData.getName() + "'s pipeline");
                return true;
            }
        }

        return false;
    }
}
