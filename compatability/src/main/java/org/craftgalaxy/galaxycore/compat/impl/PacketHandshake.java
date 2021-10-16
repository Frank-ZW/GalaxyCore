package org.craftgalaxy.galaxycore.compat.impl;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import org.craftgalaxy.galaxycore.compat.AbstractPacketHandler;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;

@NoArgsConstructor
public class PacketHandshake extends DefinedPacket {

    private String name;
    private String host;
    private int port;
    private boolean confirmed;

    public PacketHandshake(String name, String host, int port) {
        this.name = name;
        this.host = host.replace("localhost", "127.0.0.1");
        this.port = port;
        this.confirmed = false;
    }

    @Override
    public void read(ByteBuf buf) {
        this.name = DefinedPacket.readString(buf);
        this.host = DefinedPacket.readString(buf, 255);
        this.port = DefinedPacket.readVarInt(buf);
        this.confirmed = buf.readBoolean();
    }

    @Override
    public void write(ByteBuf buf) {
        DefinedPacket.writeString(buf, this.name);
        DefinedPacket.writeString(buf, this.host);
        DefinedPacket.writeVarInt(buf, this.port);
        buf.writeBoolean(this.confirmed);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

    public String getName() {
        return this.name;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isConfirmed() {
        return this.confirmed;
    }

    public String toString() {
        return "[HandlerPacket: name=" + this.name + ", host=" + this.host + ", port=" + this.port + ", confirmed=" + this.confirmed + "]";
    }
}
