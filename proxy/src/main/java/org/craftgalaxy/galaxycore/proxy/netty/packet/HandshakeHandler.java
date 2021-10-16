package org.craftgalaxy.galaxycore.proxy.netty.packet;

import lombok.AllArgsConstructor;
import net.md_5.bungee.api.config.ServerInfo;
import org.craftgalaxy.galaxycore.compat.PacketHandler;
import org.craftgalaxy.galaxycore.compat.Protocol;
import org.craftgalaxy.galaxycore.compat.exception.QuietException;
import org.craftgalaxy.galaxycore.compat.handler.PacketHandlerBoss;
import org.craftgalaxy.galaxycore.compat.impl.PacketHandshake;
import org.craftgalaxy.galaxycore.compat.wrapper.ChannelWrapper;
import org.craftgalaxy.galaxycore.compat.wrapper.PacketWrapper;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.server.ServerData;

import java.net.InetSocketAddress;

@AllArgsConstructor
public class HandshakeHandler extends PacketHandler {

    private final CoreProxyPlugin plugin;
    private ChannelWrapper channelWrapper;

    public HandshakeHandler(CoreProxyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean shouldHandle(PacketWrapper packetWrapper) {
        return !this.channelWrapper.isClosing();
    }

    @Override
    public void connected(ChannelWrapper channelWrapper) {
        this.channelWrapper = channelWrapper;
    }

    @Override
    public void disconnected(ChannelWrapper channelWrapper) {
        this.channelWrapper.close();
    }

    @Override
    public void exception(Throwable t) throws Exception {
        throw new Exception(t);
    }

    @Override
    public void handle(PacketWrapper packetWrapper) {
        if (packetWrapper.getPacket() == null) {
            throw new QuietException("Unexpected packet received during initial handshake process");
        }
    }

    public void handle(PacketHandshake packet) {
        PacketHandshake copied = new PacketHandshake(packet.getName(), packet.getHost(), packet.getPort());
        ServerInfo server = this.plugin.getProxy().getServerInfo(copied.getName());
        if (server == null) {
            this.channelWrapper.write(copied);
            return;
        }

        ServerData serverData = null;
        InetSocketAddress remoteAddress = (InetSocketAddress) server.getSocketAddress();
        if (copied.getHost().equals(remoteAddress.getAddress().getHostAddress()) && copied.getPort() == remoteAddress.getPort()) {
            serverData = this.plugin.getServerManager().register(this.channelWrapper, server);
            if (serverData != null) {
                copied.setConfirmed(true);
            }
        }

        this.channelWrapper.write(copied);
        if (copied.isConfirmed()) {
            this.plugin.getServerManager().setProtocol(Protocol.PLAY);
            this.channelWrapper.setProtocol(Protocol.PLAY);
            this.channelWrapper.getChannel().pipeline().get(PacketHandlerBoss.class).setPacketHandler(new UpstreamBridge(this.plugin, server, serverData, this.channelWrapper));
        }
    }
}
