package org.craftgalaxy.galaxycore.proxy.punishment;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IPunishmentSystem {

    void kick(ProxiedPlayer player, @Nullable String reason);

    void ban(String playerName, UUID playerUuid, @Nullable String reason);
}
