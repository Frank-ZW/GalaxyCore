package org.craftgalaxy.galaxycore.client.connection.handler;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.craftgalaxy.galaxycore.client.CorePlugin;
import org.craftgalaxy.galaxycore.client.connection.manager.ConnectionManager;
import org.craftgalaxy.galaxycore.client.data.ClientData;
import org.craftgalaxy.galaxycore.client.data.manager.PlayerManager;
import org.craftgalaxy.galaxycore.client.login.ConnectionTask;
import org.craftgalaxy.galaxycore.client.login.PlayerLoginTask;
import org.craftgalaxy.galaxycore.client.login.impl.AuthenticateTask;
import org.craftgalaxy.galaxycore.client.login.impl.PasswordSetTask;
import org.craftgalaxy.galaxycore.client.runnable.ShutdownRunnable;
import org.craftgalaxy.galaxycore.client.util.StringUtil;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;
import org.craftgalaxy.galaxycore.compat.PacketHandler;
import org.craftgalaxy.galaxycore.compat.Protocol;
import org.craftgalaxy.galaxycore.compat.impl.*;
import org.craftgalaxy.galaxycore.compat.wrapper.ChannelWrapper;
import org.craftgalaxy.galaxycore.compat.wrapper.PacketWrapper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

public class DownstreamBridge extends PacketHandler {

    private final CorePlugin plugin;
    private ChannelWrapper channelWrapper;
    private ShutdownRunnable shutdown;

    public DownstreamBridge(CorePlugin plugin) {
        this.plugin = plugin;
    }

    public void connected(ChannelWrapper channelWrapper) {
        this.channelWrapper = channelWrapper;
    }

    public void disconnected(ChannelWrapper channelWrapper) {
        this.channelWrapper.close();
    }

    public void exception(Throwable t) throws Exception {
        throw new Exception(t);
    }

    public void handle(PacketWrapper packetWrapper) {}

    public void handle(PacketHandshake packet) {
        if (packet.isConfirmed()) {
            this.channelWrapper.setProtocol(Protocol.PLAY);
            Bukkit.getLogger().info(ChatColor.GREEN + "Established TCP connection for " + packet.getName() + " on " + packet.getHost() + ":" + packet.getPort() + ". This server can now communicate with the Proxy.");
        } else {
            Bukkit.getLogger().warning("One or more of the settings entered in config.yml do not match up with the settings entered in the Proxy. Before restarting the plugin, double check to make sure the name, IP address, and port of this server match that of Bungee's.");
            this.channelWrapper.close();
        }
    }

    public void handle(PacketPlayerConnect packet) {
        ClientData clientData = PlayerManager.getInstance().getClientData(packet.getPlayer());
        if (clientData != null) {
            clientData.setReceivePings(packet.isReceivePings());
            for (int i : packet.getActions()) {
                PlayerLoginTask task = PlayerLoginTask.LoginTasks.VALUES[i].getTask();
                clientData.addHandler(task);
                if (task instanceof ConnectionTask) {
                    ((ConnectionTask) task).onConnect(clientData.getPlayer());
                }
            }

            if (packet.isFrozen()) {
                clientData.freeze();
            }

            if (!packet.isAddressLogged()) {
                List<String> addresses = packet.getAddresses();
                InetSocketAddress socketAddress = clientData.socketAddress();
                if (socketAddress == null) {
                    clientData.getPlayer().kick(Component.text(ChatColor.RED + "A kick occurred in your connection."));
                    return;
                }

                String host = socketAddress.getAddress().getHostAddress();
                if ("127.0.0.1".equals(host) || "localhost".equals(host)) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(packet.getPlayer());
                    this.channelWrapper.write(new PacketBroadcast(ChatColor.DARK_AQUA + offlinePlayer.getName() + ChatColor.GREEN + " joined the server with an IP address of " + host));
                    Bukkit.getScheduler().runTask(this.plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + offlinePlayer.getName()));
                    return;
                }

                if (!packet.isFrozen()) {
                    if (addresses.isEmpty()) {
                        addresses.add(host);
                    } else if (!addresses.contains(host)) {
                        if (packet.isAuthenticatePermission()) {
                            clientData.setFrozen(true);
                            this.channelWrapper.write(new PacketFreezeAction(clientData.getUniqueId(), 0));
                            PlayerLoginTask authenticateTask = PlayerLoginTask.LoginTasks.AUTHENTICATE.getTask();
                            clientData.addHandler(authenticateTask);
                            ((ConnectionTask)authenticateTask).onConnect(clientData.getPlayer());
                        } else {
                            addresses.add(host);
                        }
                    }

                    this.channelWrapper.write(new PacketUpdateAddresses(clientData.getUniqueId(), addresses, host));
                }
            }

        }
    }

    public void handle(PacketFreezeAction packet) {
        ClientData clientData = PlayerManager.getInstance().getClientData(packet.getPlayer());
        if (clientData != null) {
            if (packet.getFreeze() == 0) {
                clientData.freeze();
            } else {
                clientData.unfreeze();
            }
        }
    }

    public void handle(PacketPasswordSet packet) {
        this.handleLoginPacket(packet.getPlayer(), packet, PasswordSetTask.class);
    }

    public void handle(PacketAuthenticateConnection packet) {
        this.handleLoginPacket(packet.getPlayer(), packet, AuthenticateTask.class);
    }

    private void handleLoginPacket(UUID player, DefinedPacket packet, Class<? extends PlayerLoginTask> clazz) {
        ClientData clientData = PlayerManager.getInstance().getClientData(player);
        if (clientData != null) {
            clientData.firePipeline(clazz, packet);
        }

    }

    public void handle(PacketKeepAlive packet) {
        PacketKeepAlive copied = new PacketKeepAlive(packet.getId());
        this.channelWrapper.write(copied);
    }

    public void handle(PacketShutdown packet) {
        switch(packet.getAction()) {
            case 0:
                if (this.shutdown != null && !this.shutdown.isCancelled()) {
                    this.shutdown.cancel();
                    Bukkit.broadcast(Component.newline().append(StringUtil.SERVER_PREFIX).append(Component.text(ChatColor.YELLOW + "The server shutdown has been rescheduled to " + ChatColor.LIGHT_PURPLE + packet.getAttribute() + " second" + (packet.getAttribute() == 1 ? "" : "s") + ChatColor.YELLOW + ".")).append(Component.newline()));
                } else {
                    Bukkit.broadcast(Component.newline().append(StringUtil.SERVER_PREFIX).append(Component.text(ChatColor.YELLOW + "The server has been scheduled to shutdown in " + ChatColor.LIGHT_PURPLE + packet.getAttribute() + " second" + (packet.getAttribute() == 1 ? "" : "s") + ChatColor.YELLOW + ".")).append(Component.newline()));
                }

                this.shutdown = new ShutdownRunnable(this.plugin, packet.getAttribute());
                this.shutdown.runTaskTimerAsynchronously(this.plugin, 20L, 20L);
                break;
            case 1:
                if (this.shutdown != null && !this.shutdown.isCancelled()) {
                    this.shutdown.cancel();
                    Bukkit.broadcast(Component.newline().append(StringUtil.SERVER_PREFIX).append(Component.text(ChatColor.YELLOW + "The server shutdown has been cancelled by " + ChatColor.LIGHT_PURPLE + packet.getWho() + ChatColor.YELLOW + ".")).append(Component.newline()));
                }

                break;
            default:
                this.channelWrapper.write(new PacketShutdown(packet.getWho(), this.shutdown != null && !this.shutdown.isCancelled() ? this.shutdown.getCountdown() : -1, 2));
        }
    }

    public void handle(PacketServerDisconnect packet) {
        switch (packet.getAction()) {
            case 0 -> Bukkit.getScheduler().runTask(this.plugin, () -> Bukkit.getServer().shutdown());
            case 2 -> ConnectionManager.getInstance().countdown();
        }
    }
}