package org.craftgalaxy.galaxycore.client.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.AllArgsConstructor;
import org.craftgalaxy.galaxycore.client.CorePlugin;
import org.craftgalaxy.galaxycore.client.connection.handler.DownstreamBridge;
import org.craftgalaxy.galaxycore.compat.PipelineUtils;
import org.craftgalaxy.galaxycore.compat.Protocol;
import org.craftgalaxy.galaxycore.compat.handler.PacketDecoder;
import org.craftgalaxy.galaxycore.compat.handler.PacketEncoder;
import org.craftgalaxy.galaxycore.compat.handler.PacketHandlerBoss;

@AllArgsConstructor
public class PipelineBase extends ChannelInitializer<Channel> {

    private final CorePlugin plugin;

    protected void initChannel(Channel channel) {
        PipelineUtils.BASE.initChannel(channel);
        channel.pipeline()
                .addBefore(PipelineUtils.TIMEOUT_HANDLER, PipelineUtils.PACKET_DECODER, new PacketDecoder(Protocol.HANDSHAKE, false))
                .addBefore(PipelineUtils.BOSS_HANDLER, PipelineUtils.PACKET_ENCODER, new PacketEncoder(Protocol.HANDSHAKE, false));
        channel.pipeline().get(PacketHandlerBoss.class).setPacketHandler(new DownstreamBridge(this.plugin));
    }
}
