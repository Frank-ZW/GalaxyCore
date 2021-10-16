package org.craftgalaxy.galaxycore.proxy.player;

import lombok.Data;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.craftgalaxy.galaxycore.compat.impl.PacketFreezeAction;
import org.craftgalaxy.galaxycore.compat.impl.PacketPasswordSet;
import org.craftgalaxy.galaxycore.compat.impl.PacketPlayerConnect;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.server.ServerData;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class PlayerData {

    private final CoreProxyPlugin plugin;
    private final ProxiedPlayer player;
    private final String name;
    private final UUID uniqueId;
    private List<String> addresses;
    private String currentHost;
    private String lastHost;
    private String password;
    private boolean receivePings;
    private boolean shouldAuthenticate;
    private boolean authenticated;
    private boolean botChecked;
    private boolean frozen;
    private boolean addressLogged;

    public PlayerData(ProxiedPlayer player) {
        this(player, new ArrayList<>());
    }

    public PlayerData(ProxiedPlayer player, List<String> addresses) {
        this.plugin = CoreProxyPlugin.getInstance();
        this.player = player;
        this.name = player.getName();
        this.uniqueId = player.getUniqueId();
        this.addresses = addresses;
        this.botChecked = player.hasPermission(StringUtil.BYPASS_BOT_CHECK_PERMISSION);
        this.receivePings = player.hasPermission(StringUtil.TOGGLE_PINGS_PERMISSION);
        this.addressLogged = false;
    }

    public ServerInfo getServer() {
        return this.player.getServer().getInfo();
    }

    public void sendPlayerLogin(ServerInfo server) {
        ServerData serverData = this.plugin.getServerManager().getServerData(server.getName());
        if (serverData == null) {
            this.player.disconnect(StringUtil.KICK_IN_CONNECTION);
        } else {
            List<Integer> actions = new ArrayList<>();
            if (!this.botChecked) {
                actions.add(0);
            }

            if (this.password == null && this.player.hasPermission(StringUtil.AUTHENTICATE_PERMISSION)) {
                actions.add(1);
            }

            serverData.getChannelWrapper().write(new PacketPlayerConnect(this.uniqueId, actions, this.addresses, this.addressLogged, this.player.hasPermission("galaxycore.command.authenticate"), this.receivePings, this.frozen));
        }
    }

    public void setPassword(@NotNull String password) {
        this.setPassword(password, true);
    }

    public void setPassword(@NotNull String password, boolean packet) {
        if (this.password == null && packet) {
            ServerData serverData = this.getServerData();
            if (serverData != null) {
                serverData.write(new PacketPasswordSet(this.uniqueId));
            }
        }

        this.password = password;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public ServerData getServerData() {
        return this.plugin.getServerManager().getServerData(this.player.getServer().getInfo());
    }

    public boolean freeze() {
        if (this.frozen) {
            return false;
        } else {
            ServerData serverData = this.getServerData();
            if (serverData == null) {
                return false;
            } else {
                this.frozen = true;
                serverData.write(new PacketFreezeAction(this.uniqueId, 0));
                return true;
            }
        }
    }

    public boolean unfreeze() {
        if (!this.frozen) {
            return false;
        } else {
            ServerData serverData = this.getServerData();
            if (serverData == null) {
                return false;
            } else {
                this.frozen = false;
                serverData.write(new PacketFreezeAction(this.uniqueId, 1));
                return true;
            }
        }
    }

    public CoreProxyPlugin getPlugin() {
        return this.plugin;
    }

    public ProxiedPlayer getPlayer() {
        return this.player;
    }

    public String getName() {
        return this.name;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public List<String> getAddresses() {
        return this.addresses;
    }

    public String getCurrentHost() {
        return this.currentHost;
    }

    public String getLastHost() {
        return this.lastHost;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean isReceivePings() {
        return this.receivePings;
    }

    public boolean isShouldAuthenticate() {
        return this.shouldAuthenticate;
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public boolean isBotChecked() {
        return this.botChecked;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public boolean isAddressLogged() {
        return this.addressLogged;
    }

    public void setCurrentHost(String currentHost) {
        this.currentHost = currentHost;
    }

    public void setLastHost(String lastHost) {
        this.lastHost = lastHost;
    }

    public void setReceivePings(boolean receivePings) {
        this.receivePings = receivePings;
    }

    public void setShouldAuthenticate(boolean shouldAuthenticate) {
        this.shouldAuthenticate = shouldAuthenticate;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void setBotChecked(boolean botChecked) {
        this.botChecked = botChecked;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public void setAddressLogged(boolean addressLogged) {
        this.addressLogged = addressLogged;
    }
}
