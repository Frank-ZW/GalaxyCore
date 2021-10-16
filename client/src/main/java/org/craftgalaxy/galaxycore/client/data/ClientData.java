package org.craftgalaxy.galaxycore.client.data;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.login.PlayerLoginTask;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class ClientData {

    private final Map<Class<? extends PlayerLoginTask>, PlayerLoginTask> pipeline = new HashMap<>();
    private final Player player;
    private final String name;
    private final UUID uniqueId;
    private final Location start;
    private boolean frozen;
    private boolean receivePings;
    private long lastChatTimestamp;

    public ClientData(Player player) {
        this.player = player;
        this.name = player.getName();
        this.uniqueId = player.getUniqueId();
        this.start = player.getLocation();
        this.frozen = false;
        this.receivePings = false;
    }

    public void addHandler(PlayerLoginTask handler) {
        Bukkit.getLogger().info(ChatColor.GREEN + "Added " + handler.getClass().getSimpleName() + " to " + this.name + "'s pipeline");
        this.pipeline.put(handler.getClass(), handler);
    }

    public void firePipeline(Object object) {
        this.pipeline.values().removeIf((handler) -> handler.apply(this, object));
    }

    public void firePipeline(Class<? extends PlayerLoginTask> handler, Object object) {
        this.pipeline.computeIfPresent(handler, (k, v) -> v.apply(this, object) ? null : v);
    }

    public boolean handlerPresent(Class<? extends PlayerLoginTask> clazz) {
        return this.pipeline.containsKey(clazz);
    }

    public void freeze() {
        this.frozen = true;
        this.player.sendMessage(Component.newline().append(Component.newline()));
        this.player.sendMessage(ChatColor.YELLOW + "You have been frozen by a staff member.");
        this.player.sendMessage(ChatColor.LIGHT_PURPLE + "Please join our Discord: https://discord.gg/saNwTRuVwk");
        this.player.sendMessage(ChatColor.LIGHT_PURPLE + "You have 10 minutes. Logging out will result in a ban.");
        this.player.sendMessage(Component.newline().append(Component.newline()));
    }

    public void unfreeze() {
        this.frozen = false;
        this.player.sendMessage(ChatColor.YELLOW + "You are no longer frozen!");
    }

    public InetSocketAddress socketAddress() {
        return this.player.getAddress();
    }

    public Map<Class<? extends PlayerLoginTask>, PlayerLoginTask> getPipeline() {
        return this.pipeline;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getName() {
        return this.name;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public Location getStart() {
        return this.start;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public boolean isReceivePings() {
        return this.receivePings;
    }

    public long getLastChatTimestamp() {
        return this.lastChatTimestamp;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public void setReceivePings(boolean receivePings) {
        this.receivePings = receivePings;
    }

    public void setLastChatTimestamp(long lastChatTimestamp) {
        this.lastChatTimestamp = lastChatTimestamp;
    }
}
