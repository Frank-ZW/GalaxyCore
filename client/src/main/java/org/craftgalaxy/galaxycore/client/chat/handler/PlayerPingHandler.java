package org.craftgalaxy.galaxycore.client.chat.handler;

import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.chat.AbstractChatHandler;
import org.craftgalaxy.galaxycore.client.chat.manager.PooledResourceManager;
import org.craftgalaxy.galaxycore.client.chat.wrapper.IChatWrapper;
import org.craftgalaxy.galaxycore.client.data.ChatData;
import org.craftgalaxy.galaxycore.client.data.ClientData;
import org.craftgalaxy.galaxycore.client.data.manager.PlayerManager;

public class PlayerPingHandler<T> extends AbstractChatHandler<T> {

    public PlayerPingHandler(PooledResourceManager<T> manager) {
        super(manager);
    }

    @Override
    public void channelRead(Player player, Object msg, IChatWrapper<T> wrapper) {
        ChatData chatData = (ChatData) msg;
        String[] words = chatData.getOriginalMsg().trim().split(" ");
        if (this.plugin.isDependencyPresent("TownyChat")) {
            if (wrapper.get() instanceof AsyncChatHookEvent event && event.getChannel().getType() != channelTypes.GLOBAL) {
                this.manager.release(chatData);
                this.manager.release(wrapper);
                return;
            }
        }

        for (String word : words) {
            Player client = Bukkit.getPlayer(word);
            if (client != null) {
                ClientData clientData = PlayerManager.getInstance().getClientData(client);
                if (clientData != null && !clientData.isReceivePings()) {
                    client.playEffect(client.getLocation(), Effect.CLICK2, Effect.CLICK2.getData());
                }
            }
        }

        this.manager.release(chatData);
        this.manager.release(wrapper);
    }
}
