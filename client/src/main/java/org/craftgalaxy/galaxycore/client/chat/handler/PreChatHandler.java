package org.craftgalaxy.galaxycore.client.chat.handler;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.chat.manager.PooledResourceManager;
import org.craftgalaxy.galaxycore.client.chat.type.ChatPipelineEncoder;
import org.craftgalaxy.galaxycore.client.data.ChatData;
import org.craftgalaxy.galaxycore.client.data.ClientData;
import org.craftgalaxy.galaxycore.client.data.manager.PlayerManager;
import org.craftgalaxy.galaxycore.client.login.impl.BotCheckTask;
import org.craftgalaxy.galaxycore.client.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class PreChatHandler<T> extends ChatPipelineEncoder<T, String> {

    public PreChatHandler(PooledResourceManager<T> manager) {
        super(manager);
    }

    @Override
    public @NotNull ChatData encode(Player player, String input) {
        ClientData clientData = PlayerManager.getInstance().getClientData(player);
        ChatData chatData = this.manager.getChatData()
                .setPlayer(player)
                .setOriginalMsg(input)
                .setLowercaseMsg(StringUtils.lowerCase(input))
                .setClientData(clientData);
        if (clientData == null) {
            return chatData.setReason(StringUtil.NO_PLAYER_DATA).setCancelled(true);
        }

        if (PlayerManager.getInstance().isSilenced() && !player.hasPermission(StringUtil.SILENCED_BYPASS_PERMISSION)) {
            return chatData.setReason(ChatColor.GRAY + "The chat has been silenced.").setCancelled(true);
        }

        if (clientData.isFrozen()) {
            return chatData.setReason(ChatColor.RED + "You have been frozen").setCancelled(true);
        }

        if (clientData.handlerPresent(BotCheckTask.class)) {
            return chatData.setReason(ChatColor.RED + "You must first move before chatting!").setCancelled(true);
        }

        if (PlayerManager.getInstance().isSlowMode() && !player.hasPermission(StringUtil.SLOW_MODE_BYPASS_PERMISSION)) {
            long timestamp = clientData.getLastChatTimestamp();
            int secondsLeft = PlayerManager.getInstance().getChatCooldown(player) - (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - timestamp);
            if (secondsLeft > 0) {
                return chatData.setReason(ChatColor.GRAY + "You have " + ChatColor.WHITE + secondsLeft + " seconds" + ChatColor.GRAY + " left before you can chat again.").setCancelled(true);
            }
        }

        return chatData.setLevel(FilterLevel.PASS);
    }
}
