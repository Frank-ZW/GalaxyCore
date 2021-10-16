package org.craftgalaxy.galaxycore.client.util;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class StringUtil {

    public static final Component AUTHENTICATE_MESSAGE = Component.text(ChatColor.RED + "You logged in from a new IP address! Type /authenticate <password> to authenticate your connection.")
            .append(Component.newline())
            .append(Component.newline());
    public static final Component SERVER_PREFIX = Component.text(ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "Craft" + ChatColor.BLUE + "Galaxy" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET);

    public static final String DONATOR_CHAT_COOLDOWN = "galaxycore.filter.donator";
    public static final String FILTER_BYPASS_PERMISSION = "galaxycore.bypass.filter";
    public static final String SILENCED_BYPASS_PERMISSION = "galaxycore.bypass.silence";
    public static final String SLOW_MODE_BYPASS_PERMISSION = "galaxycore.bypass.slowmode";
    public static final String MUTECHAT_COMMAND_PERMISSION = "galaxycore.command.mutechat";
    public static final String SLOWCHAT_COMMAND_PERMISSION = "galaxycore.command.slowchat";
    public static final String MINERS_COMMAND_PERMISSION = "galaxycore.command.miners";
    public static final String CLEARCHAT_COMMAND_PERMISSION = "galaxycore.command.clearchat";
    public static final String PLAYER_COUNT_BYPASS_PERMISSION = "galaxycore.bypass.playercount";

    public static final String NO_PLAYER_DATA = ChatColor.RED + "You do not have a player profile on the server. Please re-log if this occurs.";
    public static final String PLAYERS_ONLY = ChatColor.RED + "You must be a player to run this command.";
    public static final String INSUFFICIENT_PERMISSION = ChatColor.RED + "You do not have permission to run this command.";
}
