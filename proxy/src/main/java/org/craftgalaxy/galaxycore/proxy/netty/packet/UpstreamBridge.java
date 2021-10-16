package org.craftgalaxy.galaxycore.proxy.netty.packet;

import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.craftgalaxy.galaxycore.compat.PacketHandler;
import org.craftgalaxy.galaxycore.compat.exception.KeepAliveTimeoutException;
import org.craftgalaxy.galaxycore.compat.impl.*;
import org.craftgalaxy.galaxycore.compat.wrapper.ChannelWrapper;
import org.craftgalaxy.galaxycore.compat.wrapper.PacketWrapper;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.event.PlayerFullyConnectedEvent;
import org.craftgalaxy.galaxycore.proxy.player.PlayerData;
import org.craftgalaxy.galaxycore.proxy.server.ServerData;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;

@AllArgsConstructor
public class UpstreamBridge extends PacketHandler {

    private final CoreProxyPlugin plugin;
    private final ServerInfo server;
    private final ServerData serverData;
    private final ChannelWrapper wrapper;

    public void handle(PacketWrapper packetWrapper) {}

    public void handle(PacketFullConnection packet) {
        BaseComponent message = new TextComponent(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Attention" + ChatColor.DARK_GRAY + "] " + ChatColor.LIGHT_PURPLE + packet.getName() + ChatColor.YELLOW + " has joined the server through an unauthorized IP address of 'localhost'. This warning means a hacker is utilizing an exploit to gain administrative access to the network. If this is a false flag, unban " + ChatColor.LIGHT_PURPLE + packet.getName() + ChatColor.YELLOW + " through both the proxy and backend server.");
        ProxiedPlayer player = this.plugin.getProxy().getPlayer(packet.getUniqueId());
        if (player == null) {
            Collection<ProxiedPlayer> players = this.plugin.getProxy().getPlayers();
            for (ProxiedPlayer p : players) {
                if (p.hasPermission(StringUtil.AUTHENTICATE_PERMISSION)) {
                    p.sendMessage(StringUtil.EMPTY_STRING);
                    p.sendMessage(message);
                    p.sendMessage(StringUtil.EMPTY_STRING);
                }
            }

            CommandSender console = this.plugin.getProxy().getConsole();
            console.sendMessage(StringUtil.EMPTY_STRING);
            console.sendMessage(message);
            console.sendMessage(StringUtil.EMPTY_STRING);
            this.wrapper.write(new PacketPlayerConnect(packet.getUniqueId(), new ArrayList<>(), new ArrayList<>(), false, false, false, true));
            this.plugin.getPunishmentSystem().ban(packet.getName(), packet.getUniqueId(), null);
        } else {
            this.plugin.getProxy().getPluginManager().callEvent(new PlayerFullyConnectedEvent(player, this.server));
        }
    }

    public void handle(PacketFreezeAction packet) {
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(packet.getPlayer());
        if (playerData != null) {
            playerData.setFrozen(packet.getFreeze() == 0);
        }
    }

    public void handle(PacketUpdateAddresses packet) {
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(packet.getPlayer());
        if (playerData != null) {
            playerData.setAddresses(packet.getAddresses());
            playerData.setCurrentHost(packet.getHost());
            playerData.setAddressLogged(true);
        }
    }

    public void handle(PacketKeepAlive packet) throws Exception {
        if (packet.getId() != this.serverData.getId()) {
            throw new KeepAliveTimeoutException("Received wrong keep alive response from the client");
        } else {
            this.serverData.setId(-1L);
        }
    }

    public void handle(PacketBroadcast packet) {
        Collection<ProxiedPlayer> players = this.plugin.getProxy().getPlayers();
        BaseComponent alert = new TextComponent(ChatColor.DARK_AQUA + "[" + this.server.getName() + "] " + ChatColor.RESET + packet.getAlert());
        for (ProxiedPlayer player : players) {
            if (player.hasPermission(StringUtil.FILTER_NOTIFY_PERMISSION)) {
                player.sendMessage(alert);
            }
        }

        this.plugin.getProxy().getConsole().sendMessage(alert);
    }

    public void handle(PacketBotCheck packet) {
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(packet.getPlayer());
        if (playerData != null) {
            playerData.setBotChecked(true);
        }
    }

    public void handle(PacketShutdown packet) {
        if (packet.getAction() == 2) {
            this.serverData.completeShutdownStatus(packet.getWho(), packet.getAttribute());
        }
    }

    public void handle(PacketServerDisconnect packet) {
        if (packet.getAction() == 1) {
            ServerInfo serverInfo = this.plugin.getSettings().fallbackServer(this.server.getName());
            if (serverInfo != null && !this.server.getPlayers().isEmpty()) {
                this.serverData.setReceivedDisconnectionRequest(true);
                this.serverData.pendingDisconnections(this.server.getPlayers());
                for (ProxiedPlayer player : this.server.getPlayers()) {
                    if (!serverInfo.equals(player.getServer().getInfo())) {
                        player.connect(serverInfo);
                    }
                }
            } else {
                this.wrapper.close(new PacketServerDisconnect(2));
            }
        }
    }
}
