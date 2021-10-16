package org.craftgalaxy.galaxycore.client.chat.handler;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.chat.AbstractChatHandler;
import org.craftgalaxy.galaxycore.client.chat.manager.PooledResourceManager;
import org.craftgalaxy.galaxycore.client.chat.wrapper.IChatWrapper;
import org.craftgalaxy.galaxycore.client.connection.manager.ConnectionManager;
import org.craftgalaxy.galaxycore.client.data.ChatData;
import org.craftgalaxy.galaxycore.client.data.manager.PlayerManager;
import org.craftgalaxy.galaxycore.client.login.impl.BotCheckTask;
import org.craftgalaxy.galaxycore.client.util.StringUtil;
import org.craftgalaxy.galaxycore.compat.impl.PacketBroadcast;

import java.util.concurrent.TimeUnit;

public class ChatHandlerBoss<T> extends AbstractChatHandler<T> {

    public ChatHandlerBoss(PooledResourceManager<T> manager) {
        super(manager);
    }

    @Override
    public void channelRead(Player player, Object msg, IChatWrapper<T> wrapper) {
        ChatData chatData = (ChatData) msg;
        if (chatData.isCancelled() && !player.hasPermission(StringUtil.FILTER_BYPASS_PERMISSION)) {
            wrapper.cancel();
            if (chatData.getReason() != null && !chatData.getReason().isEmpty()) {
                player.sendMessage(chatData.getReason());
            }
        }

        if (chatData.getClientData() != null && !chatData.getClientData().handlerPresent(BotCheckTask.class)) {
            String format = wrapper.format();
            if (chatData.getClientData().isFrozen()) {
                format = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Frozen" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET + format;
                ConnectionManager.getInstance().write(new PacketBroadcast(format));
            } else {
                if (chatData.getLevel() != FilterLevel.PASS) {
                    if (!player.hasPermission(StringUtil.FILTER_BYPASS_PERMISSION)) {
                        if (chatData.getLevel() == FilterLevel.NOTIFY) {
                            player.sendMessage(Component.newline().append(Component.text(ChatColor.RED + "Your message was blocked because it breaks the " + ChatColor.GREEN + "Craft" + ChatColor.BLUE + "Galaxy" + ChatColor.RED + " community guidelines. Continued attempts to break the rules will be met with punishment.")).append(Component.newline()));
                        } else {
                            boolean spoofMsg = !PlayerManager.getInstance().isSilenced();
                            if (PlayerManager.getInstance().isSlowMode() && !player.hasPermission(StringUtil.SLOW_MODE_BYPASS_PERMISSION)) {
                                long timestamp = chatData.getClientData().getLastChatTimestamp();
                                int secondsLeft = PlayerManager.getInstance().getChatCooldown(player) - (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - timestamp);
                                if (secondsLeft > 0) {
                                    spoofMsg = false;
                                }
                            }

                            if (spoofMsg) {
                                player.sendMessage(format);
                            }
                        }

                        format = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Filtered" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET + format;
                        ConnectionManager.getInstance().write(new PacketBroadcast(format));
                        this.manager.release(wrapper);
                        this.manager.release(chatData);
                        return;
                    }

                    player.sendMessage(ChatColor.RED + "That would have been filtered.");
                }

                if (PlayerManager.getInstance().isSlowMode() && !player.hasPermission(StringUtil.SLOW_MODE_BYPASS_PERMISSION)) {
                    chatData.getClientData().setLastChatTimestamp(System.currentTimeMillis());
                }

                this.context.fireChannelRead(player, chatData, wrapper);
            }
        } else {
            this.manager.release(chatData);
            this.manager.release(wrapper);
        }
    }
}
