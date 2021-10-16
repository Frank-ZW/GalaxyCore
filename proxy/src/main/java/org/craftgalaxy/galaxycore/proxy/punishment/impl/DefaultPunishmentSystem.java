package org.craftgalaxy.galaxycore.proxy.punishment.impl;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.punishment.IPunishmentSystem;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record DefaultPunishmentSystem(CoreProxyPlugin plugin) implements IPunishmentSystem {

    @Override
    public void kick(ProxiedPlayer player, @Nullable String reason) {
        player.disconnect(new TextComponent(reason));
    }

    @Override
    public void ban(String name, UUID uniqueId, @Nullable String reason) {
        ProxiedPlayer player = this.plugin.getProxy().getPlayer(uniqueId);
        if (player != null) {
            player.disconnect(new TextComponent(reason));
        }
    }
}
