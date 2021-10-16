package org.craftgalaxy.galaxycore.compat.impl;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.craftgalaxy.galaxycore.compat.AbstractPacketHandler;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;

@NoArgsConstructor
@AllArgsConstructor
public class PacketBroadcast extends DefinedPacket {

    private String alert;

    @Override
    public void read(ByteBuf buf) {
        this.alert = DefinedPacket.readString(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        DefinedPacket.writeString(buf, this.alert);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

    public String getAlert() {
        return this.alert;
    }
}
