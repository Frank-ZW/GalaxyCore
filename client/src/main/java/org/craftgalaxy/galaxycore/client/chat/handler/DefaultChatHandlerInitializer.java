package org.craftgalaxy.galaxycore.client.chat.handler;

import org.craftgalaxy.galaxycore.client.chat.ChannelPipeline;
import org.craftgalaxy.galaxycore.client.chat.manager.PooledResourceManager;
import org.craftgalaxy.galaxycore.client.chat.type.ChatHandlerInitializer;
import org.jetbrains.annotations.NotNull;

public class DefaultChatHandlerInitializer<T> extends ChatHandlerInitializer<T> {

    public DefaultChatHandlerInitializer(PooledResourceManager<T> manager) {
        super(manager);
    }

    @Override
    public void initPipeline(@NotNull ChannelPipeline<T> pipeline) {
        pipeline.addLast(new PreChatHandler<>(this.manager))
                .addLast(new WordMatcherHandler<>(this.manager))
                .addLast(new RegexHandler<>(this.manager))
                .addLast(new WordContainerHandler<>(this.manager))
                .addLast(new ChatHandlerBoss<>(this.manager))
                .addLast(new PlayerPingHandler<>(this.manager));
    }
}
