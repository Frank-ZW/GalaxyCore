package org.craftgalaxy.galaxycore.client.chat.wrapper;

import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@AllArgsConstructor
public class TownyChatWrapper implements IChatWrapper<AsyncChatHookEvent> {

    private AsyncChatHookEvent event;

    public void cancel() {
        this.event.setCancelled(true);
    }

    public String format() {
        return String.format(this.event.getFormat(), LegacyComponentSerializer.legacySection().serialize(this.event.getPlayer().displayName()), this.event.getMessage());
    }

    public IChatWrapper<AsyncChatHookEvent> release() {
        this.event = null;
        return this;
    }

    public void accept(AsyncChatHookEvent object) {
        this.event = object;
    }

    public AsyncChatHookEvent get() {
        return this.event;
    }
}
