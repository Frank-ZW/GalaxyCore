package org.craftgalaxy.galaxycore.proxy.runnable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

import java.util.Set;

public class ShutdownRunnable implements Runnable {

    private final CoreProxyPlugin plugin;
    private final ProxyServer proxy;
    private int countdown;
    private static final Set<Integer> timestamps = Set.of(21600, 10800, 3600, 1800, 900, 600, 300, 180, 120, 60, 45, 30, 15, 10, 5, 4, 3, 2, 1);

    public ShutdownRunnable(CoreProxyPlugin plugin, int countdown) {
        this.plugin = plugin;
        this.proxy = plugin.getProxy();
        this.countdown = countdown;
    }

    @Override
    public void run() {
        if (timestamps.contains(this.countdown)) {
            this.proxy.broadcast(new TextComponent(StringUtil.SERVER_PREFIX + ChatColor.YELLOW + " The network will shutdown in " + ChatColor.LIGHT_PURPLE + this.countdown + " second" + (this.countdown == 1 ? "" : "s") + ChatColor.YELLOW + "."));
        }

        if (this.countdown-- <= 0) {
            this.plugin.getServerManager().unregisterAll();
            this.proxy.stop(ChatColor.RED + "The network has been shutdown!");
            this.plugin.cancelShutdownNow();
        }

    }

    public int getCountdown() {
        return this.countdown;
    }
}
