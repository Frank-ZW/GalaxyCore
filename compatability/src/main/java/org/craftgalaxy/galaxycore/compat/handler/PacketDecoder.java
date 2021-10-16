package org.craftgalaxy.galaxycore.compat.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.AllArgsConstructor;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;
import org.craftgalaxy.galaxycore.compat.Protocol;
import org.craftgalaxy.galaxycore.compat.exception.BadPacketException;
import org.craftgalaxy.galaxycore.compat.wrapper.PacketWrapper;

import java.util.List;

@AllArgsConstructor
public class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {

    private Protocol protocol;
    private final boolean server;

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) {
        if (ctx.channel().isActive()) {
            Protocol.DirectionData directionData = this.server ? this.protocol.TO_SERVER : this.protocol.TO_CLIENT;
            ByteBuf copy = input.copy();
            try {
                if (input.readableBytes() != 0 || this.server) {
                    int packetId = DefinedPacket.readVarInt(input);
                    DefinedPacket packet = directionData.createPacket(packetId);
                    if (packet != null) {
                        packet.read(input, directionData.getDirection());
                        if (input.isReadable()) {
                            throw new BadPacketException("Did not read all bytes from packet " + packet.getClass().getSimpleName() + " with ID " + packetId);
                        }
                    } else {
                        input.skipBytes(input.readableBytes());
                    }

                    out.add(new PacketWrapper(packet, copy));
                    copy = null;
                }
            } catch (IndexOutOfBoundsException | BadPacketException e) {
                e.printStackTrace();
            } finally {
                if (copy != null) {
                    copy.release();
                }
            }
        }
    }
}
