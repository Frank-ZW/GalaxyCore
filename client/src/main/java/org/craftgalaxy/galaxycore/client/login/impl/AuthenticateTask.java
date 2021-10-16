package org.craftgalaxy.galaxycore.client.login.impl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.data.ClientData;
import org.craftgalaxy.galaxycore.client.login.ConnectionTask;
import org.craftgalaxy.galaxycore.client.util.StringUtil;
import org.craftgalaxy.galaxycore.compat.impl.PacketAuthenticateConnection;
import org.jetbrains.annotations.NotNull;

public class AuthenticateTask extends ConnectionTask {

    @Override
    public void onConnect(@NotNull Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.5F, 0.5F);
        player.sendMessage(StringUtil.AUTHENTICATE_MESSAGE);
    }

    public boolean apply(ClientData clientData, Object o) {
        if (o instanceof PacketAuthenticateConnection) {
            Bukkit.getLogger().info(ChatColor.GREEN + "Removed " + this.getClass().getSimpleName() + " from " + clientData.getName() + "'s pipeline");
            return true;
        }

        return false;
    }
}
