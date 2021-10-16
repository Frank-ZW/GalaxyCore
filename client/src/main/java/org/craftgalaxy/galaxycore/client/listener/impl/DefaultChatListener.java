package org.craftgalaxy.galaxycore.client.listener.impl;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.craftgalaxy.galaxycore.client.chat.wrapper.DefaultChatWrapper;
import org.craftgalaxy.galaxycore.client.chat.wrapper.IChatWrapper;
import org.craftgalaxy.galaxycore.client.listener.AbstractChatListener;

public class DefaultChatListener extends AbstractChatListener<AsyncChatEvent> {

    @EventHandler
    public void onAsyncChat(AsyncChatEvent e) {
        IChatWrapper<AsyncChatEvent> wrapper = this.manager.getWrapper();
        if (wrapper == null) {
            wrapper = new DefaultChatWrapper(e);
        } else {
            wrapper.accept(e);
        }

        this.bootstrap.getHeadHandler().channelRead(e.getPlayer(), PlainTextComponentSerializer.plainText().serialize(e.message()), wrapper);
    }
}
