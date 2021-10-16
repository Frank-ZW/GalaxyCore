package org.craftgalaxy.galaxycore.client.connection.manager;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.craftgalaxy.galaxycore.client.CorePlugin;
import org.craftgalaxy.galaxycore.client.connection.PipelineBase;
import org.craftgalaxy.galaxycore.client.runnable.BootstrapRunnable;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;
import org.craftgalaxy.galaxycore.compat.impl.PacketHandshake;
import org.craftgalaxy.galaxycore.compat.impl.PacketServerDisconnect;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ConnectionManager {

    private final CorePlugin plugin;
    private final CountDownLatch shutdown = new CountDownLatch(1);
    private final EventLoopGroup group;
    private Channel channel;
    private static ConnectionManager instance;

    public ConnectionManager(CorePlugin plugin) {
        this.plugin = plugin;
        this.group = new NioEventLoopGroup();
        this.attemptConnection();
    }

    public static void enable(CorePlugin plugin) {
        instance = new ConnectionManager(plugin);
    }

    public static void disable() {
        if (instance != null) {
            try {
                if (instance.channel.isActive()) {
                    instance.channel.writeAndFlush(new PacketServerDisconnect(1));
                    if (!instance.shutdown.await(10L, TimeUnit.SECONDS)) {
                        Bukkit.getLogger().info(ChatColor.RED + "Did not receive server disconnection confirmation packet with an action ID of one.");
                    }
                } else {
                    instance.shutdown.countDown();
                }

                instance.group.shutdownGracefully().sync();
                instance.channel.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                instance = null;
            }

        }
    }

    public void attemptConnection() {
        ChannelFutureListener listener = (future) -> {
            if (future.isSuccess()) {
                future.channel().writeAndFlush(new PacketHandshake(this.plugin.getSettings().getName(), this.plugin.getSettings().getHost(), this.plugin.getSettings().getPort()));
            } else {
                Bukkit.getLogger().warning("Failed to connect with the Proxy... another attempt will be made in 10 seconds. Cause: " + future.cause().getClass().getSimpleName());
                new BootstrapRunnable().runTaskLater(this.plugin, 200L);
            }
        };

        this.channel = new Bootstrap()
                .group(this.group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(this.plugin.getSettings().getPluginHost(), this.plugin.getSettings().getPluginPort()))
                .handler(new PipelineBase(this.plugin))
                .connect()
                .addListener(listener)
                .channel();
    }

    public void write(DefinedPacket packet) {
        this.channel.writeAndFlush(packet);
    }

    public void countdown() {
        this.shutdown.countDown();
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public static ConnectionManager getInstance() {
        return instance;
    }
}
