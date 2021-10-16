package org.craftgalaxy.galaxycore.compat;

import org.craftgalaxy.galaxycore.compat.wrapper.ChannelWrapper;
import org.craftgalaxy.galaxycore.compat.wrapper.PacketWrapper;

public abstract class PacketHandler extends AbstractPacketHandler {

    public boolean shouldHandle(PacketWrapper wrapper) {
        return true;
    }

    public void connected(ChannelWrapper wrapper) throws Exception {}

    public void disconnected(ChannelWrapper wrapper) throws Exception {}

    public void exception(Throwable t) throws Exception {
        throw new Exception(t);
    }

    public abstract void handle(PacketWrapper wrapper) throws Exception;
}
