package org.craftgalaxy.galaxycore.compat.impl;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.craftgalaxy.galaxycore.compat.AbstractPacketHandler;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class PacketUpdateAddresses extends DefinedPacket {

    private UUID player;
    private List<String> addresses;
    private String host;

    @Override
    public void read(ByteBuf buf) {
        this.player = DefinedPacket.readUUID(buf);
        this.addresses = DefinedPacket.readStringArray(buf);
        this.host = DefinedPacket.readString(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        DefinedPacket.writeUUID(buf, this.player);
        DefinedPacket.writeStringArray(buf, this.addresses);
        DefinedPacket.writeString(buf, this.host);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

    public UUID getPlayer() {
        return this.player;
    }

    public List<String> getAddresses() {
        return this.addresses;
    }

    public String getHost() {
        return this.host;
    }
}
