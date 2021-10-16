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
public class PacketPlayerConnect extends DefinedPacket {

    private UUID player;
    private List<Integer> actions;
    private List<String> addresses;
    private boolean addressLogged;
    private boolean authenticatePermission;
    private boolean receivePings;
    private boolean frozen;

    @Override
    public void read(ByteBuf buf) {
        this.player = DefinedPacket.readUUID(buf);
        this.actions = DefinedPacket.readVarIntList(buf);
        this.addresses = DefinedPacket.readStringArray(buf);
        this.addressLogged = buf.readBoolean();
        this.authenticatePermission = buf.readBoolean();
        this.receivePings = buf.readBoolean();
        this.frozen = buf.readBoolean();
    }

    @Override
    public void write(ByteBuf buf) {
        DefinedPacket.writeUUID(buf, this.player);
        DefinedPacket.writeVarIntArray(buf, this.actions);
        DefinedPacket.writeStringArray(buf, this.addresses);
        buf.writeBoolean(this.addressLogged);
        buf.writeBoolean(this.authenticatePermission);
        buf.writeBoolean(this.receivePings);
        buf.writeBoolean(this.frozen);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

    public UUID getPlayer() {
        return this.player;
    }

    public List<Integer> getActions() {
        return this.actions;
    }

    public List<String> getAddresses() {
        return this.addresses;
    }

    public boolean isAddressLogged() {
        return this.addressLogged;
    }

    public boolean isAuthenticatePermission() {
        return this.authenticatePermission;
    }

    public boolean isReceivePings() {
        return this.receivePings;
    }

    public boolean isFrozen() {
        return this.frozen;
    }
}
