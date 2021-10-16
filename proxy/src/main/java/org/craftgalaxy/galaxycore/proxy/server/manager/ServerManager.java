package org.craftgalaxy.galaxycore.proxy.server.manager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.util.CaseInsensitiveMap;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;
import org.craftgalaxy.galaxycore.compat.Protocol;
import org.craftgalaxy.galaxycore.compat.impl.PacketServerDisconnect;
import org.craftgalaxy.galaxycore.compat.wrapper.ChannelWrapper;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.server.ServerData;
import org.craftgalaxy.galaxycore.proxy.util.PipelineBase;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

public class ServerManager {

    private final CoreProxyPlugin plugin;
    private final TaskScheduler scheduler;
    private final EventLoopGroup bossEventLoopGroup;
    private final EventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
    private final ChannelGroup channels;
    private final Map<String, ServerData> servers;
    private final Phaser phaser;
    private Protocol protocol;
    private Channel channel;

    public ServerManager(CoreProxyPlugin plugin) {
        this.channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        this.servers = new CaseInsensitiveMap<>();
        this.phaser = new Phaser(0);
        this.plugin = plugin;
        this.scheduler = plugin.getProxy().getScheduler();
        this.bossEventLoopGroup = new NioEventLoopGroup(1);
        this.protocol = Protocol.HANDSHAKE;
        this.scheduler.runAsync(plugin, () -> {
            try {
                this.channel = new ServerBootstrap()
                        .group(this.bossEventLoopGroup, this.workerEventLoopGroup)
                        .channel(NioServerSocketChannel.class)
                        .localAddress(new InetSocketAddress(9048))
                        .childHandler(new PipelineBase(plugin))
                        .bind()
                        .sync()
                        .channel();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void disable() {
        try {
            if (this.channels.isEmpty()) {
                this.plugin.getLogger().info(ChatColor.GREEN + "No servers still connected to the Proxy... jumping straight to Event Loop Group shutdown.");
                this.phaser.register();
                this.phaser.arriveAndDeregister();
            } else {
                DefinedPacket packet = new PacketServerDisconnect(0);
                this.servers.values().forEach((serverData) -> {
                    serverData.write(packet);
                });
            }

            this.phaser.arriveAndAwaitAdvance();
            this.bossEventLoopGroup.shutdownGracefully().sync();
            this.workerEventLoopGroup.shutdownGracefully().sync();
            this.channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.scheduler.cancel(this.plugin);
        }

    }

    public void unregisterAll() {
        Iterator<ServerData> iterator = this.servers.values().iterator();
        while(iterator.hasNext()) {
            ServerData serverData = iterator.next();
            this.channels.remove(serverData.getChannelWrapper().getChannel());
            serverData.disconnect();
            this.phaser.arriveAndDeregister();
            iterator.remove();
        }
    }

    public void unregister(Channel channel) {
        this.channels.remove(channel);
        Iterator<ServerData> iterator = this.servers.values().iterator();
        while(iterator.hasNext()) {
            ServerData serverData = iterator.next();
            if (serverData.getChannelWrapper().getChannel().equals(channel)) {
                serverData.disconnect();
                this.phaser.arriveAndDeregister();
                this.plugin.getProxy().getConsole().sendMessage(new TextComponent(StringUtil.SERVER_PREFIX + " " + ChatColor.LIGHT_PURPLE + serverData.getServer().getName() + ChatColor.YELLOW + " has been unregistered from " + ChatColor.LIGHT_PURPLE + "GalaxyCore" + ChatColor.YELLOW + "."));
                iterator.remove();
            }
        }

    }

    public ServerData register(ChannelWrapper channelWrapper, @NotNull ServerInfo serverInfo) {
        if (this.channels.add(channelWrapper.getChannel())) {
            ServerData serverData = new ServerData(this.plugin, serverInfo, channelWrapper);
            this.servers.put(serverInfo.getName(), serverData);
            this.phaser.register();
            this.plugin.getProxy().getConsole().sendMessage(new TextComponent(StringUtil.SERVER_PREFIX + " " + ChatColor.LIGHT_PURPLE + serverData.getServer().getName() + ChatColor.YELLOW + " has been registered to " + ChatColor.LIGHT_PURPLE + "GalaxyCore" + ChatColor.YELLOW + "."));
            return serverData;
        } else {
            return null;
        }
    }

    public boolean isConnected(String name) {
        ServerData serverData = this.getServerData(name);
        return serverData != null && serverData.getChannelWrapper().getChannel().isActive();
    }

    @Nullable
    public ServerData getServerData(String name) {
        return this.servers.get(name);
    }

    @Nullable
    public ServerData getServerData(@Nullable ServerInfo server) {
        return server == null ? null : this.getServerData(server.getName());
    }

    public void acceptToAll(Consumer<ServerData> consumer) {
        this.servers.values().forEach(consumer);
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public Channel getChannel() {
        return this.channel;
    }
}
