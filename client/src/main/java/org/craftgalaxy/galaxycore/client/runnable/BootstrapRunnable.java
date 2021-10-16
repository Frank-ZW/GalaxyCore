package org.craftgalaxy.galaxycore.client.runnable;

import org.bukkit.scheduler.BukkitRunnable;
import org.craftgalaxy.galaxycore.client.connection.manager.ConnectionManager;

public class BootstrapRunnable extends BukkitRunnable {

    @Override
    public void run() {
        ConnectionManager.getInstance().attemptConnection();
    }
}
