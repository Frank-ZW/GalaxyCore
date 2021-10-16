package org.craftgalaxy.galaxycore.compat.impl;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import org.craftgalaxy.galaxycore.compat.AbstractPacketHandler;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;

import java.util.UUID;

@NoArgsConstructor
public class PacketPasswordSet extends DefinedPacket {

    private UUID player;

    public PacketPasswordSet(UUID player) {
        this.player = player;
    }

    @Override
    public void read(ByteBuf buf) {
        this.player = DefinedPacket.readUUID(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        DefinedPacket.writeUUID(buf, this.player);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

    public UUID getPlayer() {
        return this.player;
    }
}
