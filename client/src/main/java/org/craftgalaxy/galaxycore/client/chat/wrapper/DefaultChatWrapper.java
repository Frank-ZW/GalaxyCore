package org.craftgalaxy.galaxycore.client.chat.wrapper;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class DefaultChatWrapper implements IChatWrapper<AsyncChatEvent> {

    private AsyncChatEvent event;

    public void cancel() {
        this.event.setCancelled(true);
    }

    public String format() {
        Player player = this.event.getPlayer();
        Component msg = this.event.renderer().render(player, player.displayName(), this.event.message(), player);
        return LegacyComponentSerializer.legacySection().serialize(msg);
    }

    public IChatWrapper<AsyncChatEvent> release() {
        this.event = null;
        return this;
    }

    public void accept(AsyncChatEvent object) {
        this.event = object;
    }

    public AsyncChatEvent get() {
        return this.event;
    }
}
