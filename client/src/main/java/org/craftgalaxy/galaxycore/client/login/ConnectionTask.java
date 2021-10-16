package org.craftgalaxy.galaxycore.client.login;

import org.bukkit.entity.Player;

public abstract class ConnectionTask extends PlayerLoginTask {

    public abstract void onConnect(Player player);
}

