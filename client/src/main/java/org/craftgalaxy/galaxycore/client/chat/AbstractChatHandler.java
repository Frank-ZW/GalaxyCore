package org.craftgalaxy.galaxycore.client.chat;

import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.CorePlugin;
import org.craftgalaxy.galaxycore.client.chat.manager.PooledResourceManager;
import org.craftgalaxy.galaxycore.client.chat.wrapper.IChatWrapper;
import org.craftgalaxy.galaxycore.client.data.ChatData;

import java.util.List;

public abstract class AbstractChatHandler<T> {

    protected final CorePlugin plugin = CorePlugin.getInstance();
    protected final PooledResourceManager<T> manager;
    protected final List<String> blacklisted;
    protected final List<String> foreignWords;
    protected final ChatPipelineNode<T, ChatData> context;

    public AbstractChatHandler(PooledResourceManager<T> manager) {
        this.blacklisted = this.plugin.getSettings().getChatFilterInfo().blacklisted();
        this.foreignWords = this.plugin.getSettings().getChatFilterInfo().foreignWords();
        this.manager = manager;
        this.context = new ChatPipelineNode<>();
    }

    public abstract void channelRead(Player player, Object msg, IChatWrapper<T> wrapper);

    public ChatPipelineNode<T, ChatData> getContext() {
        return this.context;
    }

    public enum FilterLevel {
        PASS,
        NOTIFY,
        SILENT;
    }
}
