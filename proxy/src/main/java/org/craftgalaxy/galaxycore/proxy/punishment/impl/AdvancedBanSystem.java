package org.craftgalaxy.galaxycore.proxy.punishment.impl;

import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.punishment.IPunishmentSystem;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record AdvancedBanSystem(CoreProxyPlugin plugin) implements IPunishmentSystem {

    @Override
    public void kick(ProxiedPlayer player, @Nullable String reason) {
        Punishment.create(player.getName(), player.getUniqueId().toString(), reason, null, PunishmentType.KICK, Long.MAX_VALUE, null, false);
    }

    @Override
    public void ban(String playerName, UUID playerUuid, @Nullable String reason) {
        Punishment.create(playerName, playerUuid.toString(), reason, null, PunishmentType.BAN, Long.MAX_VALUE, null, false);
    }
}
