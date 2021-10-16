package org.craftgalaxy.galaxycore.proxy.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import lombok.AllArgsConstructor;
import org.craftgalaxy.galaxycore.compat.PipelineUtils;
import org.craftgalaxy.galaxycore.compat.Protocol;
import org.craftgalaxy.galaxycore.compat.handler.PacketDecoder;
import org.craftgalaxy.galaxycore.compat.handler.PacketEncoder;
import org.craftgalaxy.galaxycore.compat.handler.PacketHandlerBoss;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.netty.handler.ChannelHandlerBoss;
import org.craftgalaxy.galaxycore.proxy.netty.packet.HandshakeHandler;

@AllArgsConstructor
public class PipelineBase extends ChannelInitializer<Channel> {

    private final CoreProxyPlugin plugin;

    @Override
    protected void initChannel(Channel channel) {
        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
        PipelineUtils.BASE.initChannel(channel);
        channel.pipeline()
                .addBefore(PipelineUtils.TIMEOUT_HANDLER, PipelineUtils.PACKET_DECODER, new PacketDecoder(Protocol.HANDSHAKE, true))
                .addBefore(PipelineUtils.BOSS_HANDLER, PipelineUtils.PACKET_ENCODER, new PacketEncoder(Protocol.HANDSHAKE, true))
                .addFirst(PipelineUtils.CHANNEL_HANDLER, new ChannelHandlerBoss(this.plugin));
        channel.pipeline().get(PacketHandlerBoss.class).setPacketHandler(new HandshakeHandler(this.plugin));
    }
}
