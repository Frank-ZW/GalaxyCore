package org.craftgalaxy.galaxycore.compat.impl;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.craftgalaxy.galaxycore.compat.AbstractPacketHandler;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class PacketFullConnection extends DefinedPacket {

    private UUID uniqueId;
    private String name;

    public PacketFullConnection(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public void read(ByteBuf buf) {
        this.uniqueId = DefinedPacket.readUUID(buf);
        this.name = DefinedPacket.readString(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        DefinedPacket.writeUUID(buf, this.uniqueId);
        DefinedPacket.writeString(buf, this.name);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public String getName() {
        return this.name;
    }
}
