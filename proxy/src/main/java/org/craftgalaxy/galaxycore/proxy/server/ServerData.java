package org.craftgalaxy.galaxycore.proxy.server;

import lombok.Data;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.craftgalaxy.galaxycore.compat.Callback;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;
import org.craftgalaxy.galaxycore.compat.exception.CallbackReplaceException;
import org.craftgalaxy.galaxycore.compat.impl.PacketKeepAlive;
import org.craftgalaxy.galaxycore.compat.impl.PacketServerDisconnect;
import org.craftgalaxy.galaxycore.compat.impl.PacketShutdown;
import org.craftgalaxy.galaxycore.compat.wrapper.ChannelWrapper;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.runnable.Shutdownable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Data
public class ServerData implements Shutdownable {

    private ServerInfo server;
    private ChannelWrapper channelWrapper;
    private long id;
    private final ScheduledTask keepAliveTask;
    private final Map<String, Callback<Integer>> shutdownCalls = new ConcurrentHashMap<>();
    private final Set<ProxiedPlayer> pendingDisconnections = new HashSet<>();
    private boolean receivedDisconnectionRequest;

    public ServerData(CoreProxyPlugin plugin, ServerInfo server, ChannelWrapper channelWrapper) {
        this.server = server;
        this.channelWrapper = channelWrapper;
        this.id = -1L;
        this.keepAliveTask = plugin.getProxy().getScheduler().schedule(plugin, this::sendKeepAlive, 25L, 25L, TimeUnit.SECONDS);
    }

    public void disconnect() {
        this.keepAliveTask.cancel();
        this.channelWrapper.close();
    }

    public void write(DefinedPacket packet) {
        this.channelWrapper.write(packet);
    }

    public void sendKeepAlive() {
        this.id = System.currentTimeMillis();
        this.channelWrapper.write(new PacketKeepAlive(this.id));
    }

    public void cancelShutdown(String who) {
        this.write(new PacketShutdown(who, -1, 1));
    }

    public void scheduleShutdown(int duration) {
        this.write(new PacketShutdown("", duration, 0));
    }

    public void shutdownStatus(String name, Callback<Integer> callback) {
        this.channelWrapper.write(new PacketShutdown(name, -1, 2));
        Callback<Integer> original = this.shutdownCalls.remove(name);
        if (original != null) {
            callback.done(null, new CallbackReplaceException("Callback already queued for " + name));
        }

        this.shutdownCalls.put(name, callback);
    }

    public void completeShutdownStatus(String name, int countdown) {
        this.shutdownCalls.computeIfPresent(name, (k, v) -> {
            v.done(countdown, null);
            return null;
        });
    }

    public void handlePendingDisconnection(ProxiedPlayer player) {
        if (this.receivedDisconnectionRequest && this.pendingDisconnections.remove(player) && this.pendingDisconnections.isEmpty()) {
            this.channelWrapper.close(new PacketServerDisconnect(2));
        }
    }

    public void pendingDisconnections(Collection<ProxiedPlayer> players) {
        this.pendingDisconnections.addAll(players);
    }

    public ServerInfo getServer() {
        return this.server;
    }

    public ChannelWrapper getChannelWrapper() {
        return this.channelWrapper;
    }

    public long getId() {
        return this.id;
    }

    public ScheduledTask getKeepAliveTask() {
        return this.keepAliveTask;
    }

    public Map<String, Callback<Integer>> getShutdownCalls() {
        return this.shutdownCalls;
    }

    public Set<ProxiedPlayer> getPendingDisconnections() {
        return this.pendingDisconnections;
    }

    public boolean isReceivedDisconnectionRequest() {
        return this.receivedDisconnectionRequest;
    }

    public void setServer(ServerInfo server) {
        this.server = server;
    }

    public void setChannelWrapper(ChannelWrapper channelWrapper) {
        this.channelWrapper = channelWrapper;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setReceivedDisconnectionRequest(boolean receivedDisconnectionRequest) {
        this.receivedDisconnectionRequest = receivedDisconnectionRequest;
    }
}
