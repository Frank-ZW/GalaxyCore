package org.craftgalaxy.galaxycore.compat.impl;

import io.netty.buffer.ByteBuf;
import org.craftgalaxy.galaxycore.compat.AbstractPacketHandler;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;

public class PacketKeepAlive extends DefinedPacket {

    private long id;

    public PacketKeepAlive() {
        this.id = System.currentTimeMillis();
    }

    public PacketKeepAlive(long id) {
        this.id = id;
    }

    @Override
    public void read(ByteBuf buf) {
        this.id = buf.readLong();
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeLong(this.id);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

    public long getId() {
        return this.id;
    }
}
