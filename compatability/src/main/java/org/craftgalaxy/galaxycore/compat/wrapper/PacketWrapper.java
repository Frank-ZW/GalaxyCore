package org.craftgalaxy.galaxycore.compat.wrapper;

import io.netty.buffer.ByteBuf;
import org.craftgalaxy.galaxycore.compat.DefinedPacket;

public class PacketWrapper {

    private final DefinedPacket packet;
    private final ByteBuf buf;
    private boolean released;

    public void trySingleRelease() {
        if (!this.released) {
            this.buf.release();
            this.released = true;
        }

    }

    public PacketWrapper(DefinedPacket packet, ByteBuf buf) {
        this.packet = packet;
        this.buf = buf;
    }

    public DefinedPacket getPacket() {
        return this.packet;
    }

    public ByteBuf getBuf() {
        return this.buf;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }
}
