package org.craftgalaxy.galaxycore.client.listener.impl;

import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import org.bukkit.event.EventHandler;
import org.craftgalaxy.galaxycore.client.chat.wrapper.IChatWrapper;
import org.craftgalaxy.galaxycore.client.chat.wrapper.TownyChatWrapper;
import org.craftgalaxy.galaxycore.client.listener.AbstractChatListener;

public final class TownyChatListener extends AbstractChatListener<AsyncChatHookEvent> {

    @EventHandler
    public void onAsyncChatHook(AsyncChatHookEvent e) {
        IChatWrapper<AsyncChatHookEvent> wrapper = this.manager.getWrapper();
        if (wrapper == null) {
            wrapper = new TownyChatWrapper(e);
        } else {
            wrapper.accept(e);
        }

        this.bootstrap.getHeadHandler().channelRead(e.getPlayer(), e.getMessage(), wrapper);
    }
}
