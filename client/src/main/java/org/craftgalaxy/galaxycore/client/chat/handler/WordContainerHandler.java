package org.craftgalaxy.galaxycore.client.chat.handler;

import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.chat.AbstractChatHandler;
import org.craftgalaxy.galaxycore.client.chat.manager.PooledResourceManager;
import org.craftgalaxy.galaxycore.client.chat.wrapper.IChatWrapper;
import org.craftgalaxy.galaxycore.client.data.ChatData;

import java.util.Optional;

public class WordContainerHandler<T> extends AbstractChatHandler<T> {

    public WordContainerHandler(PooledResourceManager<T> manager) {
        super(manager);
    }

    @Override
    public void channelRead(Player player, Object msg, IChatWrapper<T> wrapper) {
        ChatData chatData = (ChatData) msg;
        String checkMsg = chatData.getCheckMsg();
        Optional<String> optional = this.blacklisted.stream().map(s -> s.replaceAll("\\s+", "")).filter(checkMsg::contains).findFirst();
        if (optional.isPresent()) {
            chatData.setLevel(FilterLevel.SILENT).setCancelled(true);
        }

        this.context.fireChannelRead(player, chatData, wrapper);
    }
}