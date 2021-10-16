package org.craftgalaxy.galaxycore.client.chat.handler;

import com.gmail.nossr50.api.PartyAPI;
import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.chat.AbstractChatHandler;
import org.craftgalaxy.galaxycore.client.chat.manager.PooledResourceManager;
import org.craftgalaxy.galaxycore.client.chat.wrapper.IChatWrapper;
import org.craftgalaxy.galaxycore.client.data.ChatData;

public class WordMatcherHandler<T> extends AbstractChatHandler<T> {

    public WordMatcherHandler(PooledResourceManager<T> manager) {
        super(manager);
    }

    public void channelRead(Player player, Object msg, IChatWrapper<T> wrapper) {
        ChatData chatData = (ChatData) msg;
        String checkMsg = chatData
                .getLowercaseMsg()
                .replace("3", "e")
                .replace("1", "i")
                .replace("!", "i")
                .replace("@", "a")
                .replace("7", "t")
                .replace("0", "o")
                .replace("5", "s")
                .replace("8", "b")
                .replace("l", "i")
                .replace("/\\/", "n")
                .replaceAll("\\p{Punct}|\\d}", "")
                .trim();
        String[] words = checkMsg.split(" ");
        for (String word : words) {
            switch (word) {
                case "xvideos", "pornhub", "cum", "dox", "kys", "sex", "ddos" -> chatData.setCancelled(true).setLevel(FilterLevel.SILENT);
                default -> {
                    if ((!this.plugin.isDependencyPresent("mcMMO") || !PartyAPI.inParty(player)) && this.foreignWords.contains(word)) {
                        chatData.setCancelled(true).setLevel(FilterLevel.NOTIFY);
                    }
                }
            }
        }

        this.context.fireChannelRead(player, chatData.setWords(words).setCheckMsg(checkMsg), wrapper);
    }
}
