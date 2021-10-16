package org.craftgalaxy.galaxycore.client.chat.bootstrap;

import org.craftgalaxy.galaxycore.client.chat.AbstractChatHandler;
import org.craftgalaxy.galaxycore.client.chat.ChannelPipeline;
import org.craftgalaxy.galaxycore.client.chat.type.ChatHandlerInitializer;

public class ChatBootstrap<T> {

    private final ChannelPipeline<T> pipeline = new ChannelPipeline<>();

    public ChatBootstrap<T> handlers(ChatHandlerInitializer<T> handler) {
        handler.initPipeline(this.pipeline);
        return this;
    }

    public AbstractChatHandler<T> getHeadHandler() {
        return this.pipeline.getHead();
    }
}
