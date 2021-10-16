package org.craftgalaxy.galaxycore.client.data.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.CorePlugin;
import org.craftgalaxy.galaxycore.client.data.ClientData;
import org.craftgalaxy.galaxycore.client.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final Map<UUID, ClientData> players = new HashMap<>();
    private final int donatorCooldown;
    private final int defaultCooldown;
    private int customCooldown;
    private boolean silenced;
    private boolean slowMode;
    private boolean usingDefault;
    private static PlayerManager instance;

    public PlayerManager(CorePlugin plugin) {
        this.donatorCooldown = plugin.getSettings().getChatFilterInfo().donatorCooldown();
        this.defaultCooldown = plugin.getSettings().getChatFilterInfo().defaultCooldown();
        this.silenced = false;
        this.slowMode = false;
        Bukkit.getOnlinePlayers().forEach(this::addPlayer);
    }

    public static void enable(CorePlugin plugin) {
        instance = new PlayerManager(plugin);
    }

    public static void disable() {
        if (instance == null) {
            return;
        }

        Bukkit.getOnlinePlayers().forEach(instance::removePlayer);
        instance = null;
    }

    public void addPlayer(Player player) {
        this.players.put(player.getUniqueId(), new ClientData(player));
    }

    public void removePlayer(Player player) {
        this.removePlayer(player.getUniqueId());
    }

    public void removePlayer(UUID uniqueId) {
        this.players.remove(uniqueId);
    }

    public ClientData getClientData(@Nullable Player player) {
        return player == null ? null : this.getClientData(player.getUniqueId());
    }

    public ClientData getClientData(UUID uniqueId) {
        return (ClientData)this.players.get(uniqueId);
    }

    public int getChatCooldown(Player player) {
        return player.hasPermission(StringUtil.DONATOR_CHAT_COOLDOWN) ? this.donatorCooldown : (this.usingDefault ? this.defaultCooldown : this.customCooldown);
    }

    public void setCustomCooldown(int customCooldown) {
        this.customCooldown = customCooldown;
    }

    public void setSilenced(boolean silenced) {
        this.silenced = silenced;
    }

    public boolean isSilenced() {
        return this.silenced;
    }

    public void setSlowMode(boolean slowMode) {
        this.slowMode = slowMode;
    }

    public boolean isSlowMode() {
        return this.slowMode;
    }

    public void setUsingDefault(boolean usingDefault) {
        this.usingDefault = usingDefault;
    }

    public boolean isUsingDefault() {
        return this.usingDefault;
    }

    public static PlayerManager getInstance() {
        return instance;
    }
}
