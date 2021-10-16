package org.craftgalaxy.galaxycore.compat.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;
import org.craftgalaxy.galaxycore.compat.Protocol;

public class PacketEncoder extends MessageToByteEncoder<DefinedPacket> {

    private Protocol protocol;
    private final boolean server;

    protected void encode(ChannelHandlerContext ctx, DefinedPacket packet, ByteBuf out) {
        Protocol.DirectionData directionData = this.server ? this.protocol.TO_CLIENT : this.protocol.TO_SERVER;
        DefinedPacket.writeVarInt(out, directionData.getId(packet.getClass()));
        packet.write(out, directionData.getDirection());
    }

    public PacketEncoder(Protocol protocol, boolean server) {
        this.protocol = protocol;
        this.server = server;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
}
