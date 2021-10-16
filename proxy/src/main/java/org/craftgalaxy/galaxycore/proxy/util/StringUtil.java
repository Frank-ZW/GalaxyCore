package org.craftgalaxy.galaxycore.proxy.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class StringUtil {

    public static final BaseComponent INSUFFICIENT_PERMISSION = new TextComponent(ChatColor.RED + "You do not have permission to run this command.");
    public static final BaseComponent PLAYERS_ONLY = new TextComponent(ChatColor.RED + "You must be a player to run this command.");
    public static final BaseComponent KICK_IN_CONNECTION = new TextComponent(ChatColor.RED + "A kick occurred while sending you to another server.");
    public static final BaseComponent ERROR_LOADING_DATA = new TextComponent(ChatColor.RED + "An error occurred while loading in your player data. If this occurs, contact an administrator.");
    public static final BaseComponent EARLY_LOGIN = new TextComponent(ChatColor.RED + "Please wait for the Core plugin to finish setting up before logging in.");
    public static final BaseComponent EMPTY_STRING = new TextComponent("");
    public static final String SERVER_PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "Craft" + ChatColor.BLUE + "Galaxy" + ChatColor.DARK_GRAY + "]";

    public static final String SERVER_STATUS_PERMISSION = "galaxycore.command.serverstatus";
    public static final String FREEZE_PERMISSION = "galaxycore.command.freeze";
    public static final String AUTHENTICATE_PERMISSION = "galaxycore.command.authenticate";
    public static final String SHUTDOWN_CANCEL_PERMISSION = "galaxycore.command.shutdown.cancel";
    public static final String SHUTDOWN_STATUS_PERMISSION = "galaxycore.command.shutdown.status";
    public static final String SHUTDOWN_START_PERMISSION = "galaxycore.command.shutdown.start";
    public static final String TOGGLE_PINGS_PERMISSION = "galaxycore.command.pings";
    public static final String ALTS_PERMISSION = "galaxycore.command.alts";
    public static final String IP_LOOKUP_PERMISSION = "galaxycore.command.iplookup";
    public static final String FILTER_NOTIFY_PERMISSION = "galaxycore.notify.filter";
    public static final String BYPASS_BOT_CHECK_PERMISSION = "galaxycore.bypass.bot";
}
