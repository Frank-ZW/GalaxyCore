package org.craftgalaxy.galaxycore.proxy.player.manager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.player.PlayerData;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final CoreProxyPlugin plugin;
    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();
    private final TaskScheduler scheduler;

    public PlayerManager(CoreProxyPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getProxy().getScheduler();
        plugin.getProxy().getPlayers().forEach(this::addPlayer);
    }

    public void disable() {
        this.scheduler.cancel(this.plugin);
        this.plugin.getProxy().getPlayers().forEach(this::removePlayer);
    }

    public void addPlayer(ProxiedPlayer player) {
        this.scheduler.runAsync(this.plugin, () -> {
            this.players.put(player.getUniqueId(), this.plugin.getDatabase().fetchPlayerData(player));
        });
    }

    public void removePlayer(ProxiedPlayer player) {
        this.removePlayer(player.getUniqueId());
    }

    public void removePlayer(UUID uniqueId) {
        this.scheduler.runAsync(this.plugin, () -> {
            this.players.computeIfPresent(uniqueId, (key, data) -> {
                this.plugin.getDatabase().writePlayerData(data);
                if (data.isFrozen()) {
                    BaseComponent alert = new TextComponent(ChatColor.DARK_AQUA + "[" + data.getServer().getName() + "] " + ChatColor.GREEN + data.getName() + ChatColor.DARK_AQUA + " logged out while frozen.");
                    this.plugin.getProxy().getPlayers().stream()
                            .filter(player -> player.hasPermission(StringUtil.FREEZE_PERMISSION))
                            .forEach(player -> player.sendMessage(alert));
                    this.plugin.getProxy().getConsole().sendMessage(alert);
                }

                return null;
            });
        });
    }

    @Nullable
    public PlayerData getPlayerData(ProxiedPlayer player) {
        return this.getPlayerData(player.getUniqueId());
    }

    @Nullable
    public PlayerData getPlayerData(UUID uniqueId) {
        return this.players.get(uniqueId);
    }
}
