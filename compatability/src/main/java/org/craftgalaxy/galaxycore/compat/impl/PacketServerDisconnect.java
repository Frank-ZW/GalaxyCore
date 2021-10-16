package org.craftgalaxy.galaxycore.compat.impl;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import org.craftgalaxy.galaxycore.compat.AbstractPacketHandler;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;

@NoArgsConstructor
public class PacketServerDisconnect extends DefinedPacket {

    private byte action;

    public PacketServerDisconnect(int action) {
        this.action = (byte)action;
    }

    @Override
    public void read(ByteBuf buf) {
        this.action = buf.readByte();
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.action);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

    public byte getAction() {
        return this.action;
    }
}
