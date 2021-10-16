package org.craftgalaxy.galaxycore.client.chat.handler;

import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.chat.AbstractChatHandler;
import org.craftgalaxy.galaxycore.client.chat.manager.PooledResourceManager;
import org.craftgalaxy.galaxycore.client.chat.wrapper.IChatWrapper;
import org.craftgalaxy.galaxycore.client.data.ChatData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHandler<T> extends AbstractChatHandler<T> {

    protected static final Pattern IP_REGEX = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])([.,])){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    public RegexHandler(PooledResourceManager<T> manager) {
        super(manager);
    }

    @Override
    public void channelRead(Player player, Object msg, IChatWrapper<T> wrapper) {
        ChatData chatData = (ChatData) msg;
        String[] words = chatData
                .getLowercaseMsg()
                .replace("(dot)", ".")
                .replace("[dot]", ".")
                .trim().split(" ");
        for (String word : words) {
            Matcher matcher = IP_REGEX.matcher(word);
            if (matcher.matches()) {
                chatData.setCancelled(true).setLevel(FilterLevel.SILENT);
                break;
            }
        }

        this.context.fireChannelRead(player, chatData.setWords(words), wrapper);
    }
}
