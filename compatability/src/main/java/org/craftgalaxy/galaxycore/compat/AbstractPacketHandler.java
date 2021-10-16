package org.craftgalaxy.galaxycore.compat;

import org.craftgalaxy.galaxycore.compat.impl.*;

public abstract class AbstractPacketHandler {

    public void handle(PacketHandshake packet) throws Exception {}

    public void handle(PacketFullConnection packet) throws Exception {}

    public void handle(PacketPlayerConnect packet) throws Exception {
    }

    public void handle(PacketFreezeAction packet) throws Exception {}

    public void handle(PacketPasswordSet packet) throws Exception {
    }

    public void handle(PacketUpdateAddresses packet) throws Exception {
    }

    public void handle(PacketAuthenticateConnection packet) throws Exception {
    }

    public void handle(PacketKeepAlive packet) throws Exception {
    }

    public void handle(PacketBroadcast packet) throws Exception {}

    public void handle(PacketBotCheck packet) throws Exception {}

    public void handle(PacketShutdown packet) throws Exception {
    }

    public void handle(PacketServerDisconnect packet) throws Exception {
    }
}
