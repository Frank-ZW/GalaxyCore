package org.craftgalaxy.galaxycore.client.chat;

import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.chat.wrapper.IChatWrapper;

@NoArgsConstructor
public class ChatPipelineNode<T, U> {

    private AbstractChatHandler<T> next;
    private AbstractChatHandler<T> previous;

    public void fireChannelRead(Player player, U msg, IChatWrapper<T> wrapper) {
        if (this.next != null) {
            this.next.channelRead(player, msg, wrapper);
        }
    }

    public void setNext(AbstractChatHandler<T> next) {
        this.next = next;
    }

    public AbstractChatHandler<T> getNext() {
        return this.next;
    }

    public void setPrevious(AbstractChatHandler<T> previous) {
        this.previous = previous;
    }

    public AbstractChatHandler<T> getPrevious() {
        return this.previous;
    }
}
