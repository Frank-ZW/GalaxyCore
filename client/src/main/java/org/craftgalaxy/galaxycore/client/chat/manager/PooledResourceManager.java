package org.craftgalaxy.galaxycore.client.chat.manager;

import lombok.NoArgsConstructor;
import org.craftgalaxy.galaxycore.client.chat.wrapper.IChatWrapper;
import org.craftgalaxy.galaxycore.client.data.ChatData;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@NoArgsConstructor
public class PooledResourceManager<T> {

    private final Queue<ChatData> chatDataQueue = new ConcurrentLinkedQueue<>();
    private final Queue<IChatWrapper<T>> wrapperQueue = new ConcurrentLinkedQueue<>();

    public void release(ChatData chatData) {
        this.chatDataQueue.add(chatData.release());
    }

    public void release(IChatWrapper<T> wrapper) {
        this.wrapperQueue.add(wrapper.release());
    }

    @Nullable
    public IChatWrapper<T> getWrapper() {
        return this.wrapperQueue.poll();
    }

    public ChatData getChatData() {
        ChatData chatData = this.chatDataQueue.poll();
        return chatData == null ? new ChatData() : chatData;
    }
}
