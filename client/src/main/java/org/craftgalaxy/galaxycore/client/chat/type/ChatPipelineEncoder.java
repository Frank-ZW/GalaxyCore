package org.craftgalaxy.galaxycore.client.chat.type;

import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.chat.AbstractChatHandler;
import org.craftgalaxy.galaxycore.client.chat.manager.PooledResourceManager;
import org.craftgalaxy.galaxycore.client.chat.wrapper.IChatWrapper;
import org.craftgalaxy.galaxycore.client.data.ChatData;
import org.jetbrains.annotations.NotNull;

public abstract class ChatPipelineEncoder<T, U> extends AbstractChatHandler<T> {

    public ChatPipelineEncoder(PooledResourceManager<T> manager) {
        super(manager);
    }

    @SuppressWarnings("unchecked")
    public void channelRead(Player player, Object msg, IChatWrapper<T> wrapper) {
        this.context.fireChannelRead(player, this.encode(player, (U) msg), wrapper);
    }

    @NotNull
    public abstract ChatData encode(Player player, U msg);
}
