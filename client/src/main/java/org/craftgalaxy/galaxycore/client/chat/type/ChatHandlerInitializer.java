package org.craftgalaxy.galaxycore.client.chat.type;

import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.chat.AbstractChatHandler;
import org.craftgalaxy.galaxycore.client.chat.ChannelPipeline;
import org.craftgalaxy.galaxycore.client.chat.manager.PooledResourceManager;
import org.craftgalaxy.galaxycore.client.chat.wrapper.IChatWrapper;

public abstract class ChatHandlerInitializer<T> extends AbstractChatHandler<T> {

    public ChatHandlerInitializer(PooledResourceManager<T> manager) {
        super(manager);
    }

    public void channelRead(Player player, Object msg, IChatWrapper<T> wrapper) {}

    public abstract void initPipeline(ChannelPipeline<T> var1);
}
