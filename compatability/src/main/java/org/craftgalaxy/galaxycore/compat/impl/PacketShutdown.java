package org.craftgalaxy.galaxycore.compat.impl;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import org.craftgalaxy.galaxycore.compat.AbstractPacketHandler;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;

@NoArgsConstructor
public class PacketShutdown extends DefinedPacket {

    private String who;
    private int attribute;
    private byte action;

    public PacketShutdown(String who, int attribute, int action) {
        this.who = who;
        this.attribute = attribute;
        this.action = (byte)action;
    }

    @Override
    public void read(ByteBuf buf) {
        this.who = DefinedPacket.readString(buf);
        this.attribute = DefinedPacket.readVarInt(buf);
        this.action = buf.readByte();
    }

    @Override
    public void write(ByteBuf buf) {
        DefinedPacket.writeString(buf, this.who);
        DefinedPacket.writeVarInt(buf, this.attribute);
        buf.writeByte(this.action);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

    public String getWho() {
        return this.who;
    }

    public int getAttribute() {
        return this.attribute;
    }

    public byte getAction() {
        return this.action;
    }
}
