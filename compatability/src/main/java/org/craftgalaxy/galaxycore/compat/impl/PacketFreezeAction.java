package org.craftgalaxy.galaxycore.compat.impl;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import org.craftgalaxy.galaxycore.compat.AbstractPacketHandler;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;

import java.util.UUID;

@NoArgsConstructor
public class PacketFreezeAction extends DefinedPacket {

    private UUID player;
    private byte freeze;

    public PacketFreezeAction(UUID player, int freeze) {
        this.player = player;
        this.freeze = (byte)freeze;
    }

    @Override
    public void read(ByteBuf buf) {
        this.player = DefinedPacket.readUUID(buf);
        this.freeze = buf.readByte();
    }

    @Override
    public void write(ByteBuf buf) {
        DefinedPacket.writeUUID(buf, this.player);
        buf.writeByte(this.freeze);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

    public UUID getPlayer() {
        return this.player;
    }

    public byte getFreeze() {
        return this.freeze;
    }
}
