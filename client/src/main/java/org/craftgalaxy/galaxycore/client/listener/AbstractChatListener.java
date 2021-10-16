package org.craftgalaxy.galaxycore.client.listener;

import org.bukkit.event.Listener;
import org.craftgalaxy.galaxycore.client.chat.bootstrap.ChatBootstrap;
import org.craftgalaxy.galaxycore.client.chat.handler.DefaultChatHandlerInitializer;
import org.craftgalaxy.galaxycore.client.chat.manager.PooledResourceManager;

public abstract class AbstractChatListener<T> implements Listener {

    protected final PooledResourceManager<T> manager = new PooledResourceManager<>();
    protected final ChatBootstrap<T> bootstrap;

    public AbstractChatListener() {
        this.bootstrap = new ChatBootstrap<T>().handlers(new DefaultChatHandlerInitializer<>(this.manager));
    }
}
