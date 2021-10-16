package org.craftgalaxy.galaxycore.client.runnable;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.craftgalaxy.galaxycore.client.CorePlugin;
import org.craftgalaxy.galaxycore.client.util.StringUtil;

import java.util.Set;

public class ShutdownRunnable extends BukkitRunnable {

    private final CorePlugin plugin;
    private int countdown;
    private static final Set<Integer> timestamps = Set.of(21600, 10800, 3600, 1800, 900, 600, 300, 180, 120, 60, 45, 30, 15, 10, 5, 4, 3, 2, 1);

    public ShutdownRunnable(CorePlugin plugin, int countdown) {
        this.plugin = plugin;
        this.countdown = countdown;
    }

    @Override
    public void run() {
        if (timestamps.contains(this.countdown)) {
            Bukkit.broadcast(StringUtil.SERVER_PREFIX.append(Component.text(ChatColor.YELLOW + "The server will shutdown in " + ChatColor.LIGHT_PURPLE + this.countdown + " second" + (this.countdown == 1 ? "" : "s") + ChatColor.YELLOW + ".")));
        }

        if (this.countdown-- <= 0) {
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(ChatColor.RED + "The server has shutdown!"));
            Bukkit.getScheduler().runTask(this.plugin, () -> Bukkit.getServer().shutdown());
            this.cancel();
        }

    }

    public int getCountdown() {
        return this.countdown;
    }
}
