package org.craftgalaxy.galaxycore.compat;

import org.craftgalaxy.galaxycore.compat.impl.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public enum Protocol {

    HANDSHAKE
    {
        {
            this.TO_SERVER.registerPacket(PacketHandshake.class, PacketHandshake::new, 0x00);
            this.TO_CLIENT.registerPacket(PacketHandshake.class, PacketHandshake::new, 0x00);
        }
    },

    PLAY
    {
        {
            this.TO_SERVER.registerPacket(PacketFullConnection.class, PacketFullConnection::new, 0x00);
            this.TO_SERVER.registerPacket(PacketFreezeAction.class, PacketFreezeAction::new, 0x01);
            this.TO_SERVER.registerPacket(PacketUpdateAddresses.class, PacketUpdateAddresses::new, 0x02);
            this.TO_SERVER.registerPacket(PacketKeepAlive.class, PacketKeepAlive::new, 0x03);
            this.TO_SERVER.registerPacket(PacketBroadcast.class, PacketBroadcast::new, 0x04);
            this.TO_SERVER.registerPacket(PacketBotCheck.class, PacketBotCheck::new, 0x05);
            this.TO_SERVER.registerPacket(PacketShutdown.class, PacketShutdown::new, 0x06);
            this.TO_SERVER.registerPacket(PacketServerDisconnect.class, PacketServerDisconnect::new, 0x07);
            this.TO_CLIENT.registerPacket(PacketPlayerConnect.class, PacketPlayerConnect::new, 0x00);
            this.TO_CLIENT.registerPacket(PacketFreezeAction.class, PacketFreezeAction::new, 0x01);
            this.TO_CLIENT.registerPacket(PacketPasswordSet.class, PacketPasswordSet::new, 0x02);
            this.TO_CLIENT.registerPacket(PacketAuthenticateConnection.class, PacketAuthenticateConnection::new, 0x03);
            this.TO_CLIENT.registerPacket(PacketKeepAlive.class, PacketKeepAlive::new, 0x04);
            this.TO_CLIENT.registerPacket(PacketShutdown.class, PacketShutdown::new, 0x05);
            this.TO_CLIENT.registerPacket(PacketServerDisconnect.class, PacketServerDisconnect::new, 0x06);
        }
    };

    private static final int MAX_PACKET_ID = 255;
    public final Protocol.DirectionData TO_SERVER = new DirectionData(Direction.TO_SERVER);
    public final Protocol.DirectionData TO_CLIENT = new DirectionData(Direction.TO_CLIENT);

    public static final class DirectionData {

        private final Protocol.Direction direction;
        private final Protocol.ProtocolData protocolData;

        public DirectionData(Protocol.Direction direction) {
            this.direction = direction;
            this.protocolData = new Protocol.ProtocolData();
        }

        public int getId(Class<? extends DefinedPacket> clazz) {
            return this.protocolData.packetMap.get(clazz);
        }

        private void registerPacket(Class<? extends DefinedPacket> clazz, Supplier<? extends DefinedPacket> constructor, int packetId) {
            this.protocolData.packetMap.put(clazz, packetId);
            this.protocolData.constructors[packetId] = constructor;
        }

        @Nullable
        public DefinedPacket createPacket(int packetId) {
            Supplier<? extends DefinedPacket> constructor = this.protocolData.constructors[packetId];
            return constructor == null ? null : constructor.get();
        }

        public Protocol.Direction getDirection() {
            return this.direction;
        }
    }

    public enum Direction {
        TO_SERVER, TO_CLIENT
    }

    @SuppressWarnings("unchecked")
    private static class ProtocolData {

        private final Map<Class<? extends DefinedPacket>, Integer> packetMap = new HashMap<>(Protocol.MAX_PACKET_ID);
        private final Supplier<? extends DefinedPacket>[] constructors = new Supplier[Protocol.MAX_PACKET_ID];
    }
}
