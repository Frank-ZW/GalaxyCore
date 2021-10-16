package org.craftgalaxy.galaxycore.client.login.impl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.data.ClientData;
import org.craftgalaxy.galaxycore.client.login.ConnectionTask;
import org.craftgalaxy.galaxycore.compat.impl.PacketPasswordSet;
import org.jetbrains.annotations.NotNull;

public class PasswordSetTask extends ConnectionTask {

    public boolean apply(ClientData clientData, Object o) {
        if (o instanceof PacketPasswordSet) {
            Bukkit.getLogger().info(ChatColor.GREEN + "Removed " + this.getClass().getSimpleName() + " from " + clientData.getName() + "'s pipeline");
            return true;
        }

        return false;
    }

    @Override
    public void onConnect(@NotNull Player player) {
        player.sendMessage(ChatColor.YELLOW + "You have not entered a password for your account! A 2FA password will further protect your account from hackers gaining ownership of your account through exploits.");
        player.sendMessage(ChatColor.YELLOW + "Type /password <password> to set a 2FA password for yourself.");
    }
}
