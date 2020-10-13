package net.craftgalaxy.galaxycore.util.java;

import org.bukkit.ChatColor;

import java.util.Deque;

public class StringUtil {

    public static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "Craft" + ChatColor.BLUE + "Galaxy" + ChatColor.DARK_GRAY + "]";
    public static final String ERROR_LOADING_DATA = ChatColor.RED + "Failed to successfully load in your player data.";
    public static final String ERROR_GETTING_DATA = StringUtil.PREFIX + ChatColor.RED + " Failed to retrieve that player's associated player data.";
    public static final String INSUFFICIENT_PERMISSION = StringUtil.PREFIX + ChatColor.GRAY + " You do not have permission to run this command.";
    public static final String PLAYER_OFFLINE = StringUtil.PREFIX + ChatColor.RED + " The player specified must be online.";
    public static final String PLAYER_ONLY = StringUtil.PREFIX + ChatColor.RED + " You must be a player to run this command.";
    public static final String CHAT_IS_SLOWED = StringUtil.PREFIX + ChatColor.GRAY + " You must wait" + ChatColor.WHITE + " %s second(s)" + ChatColor.GRAY + " before you can chat again.";

    public static String formatGenericDeque(Deque<String> deque) {
        switch (deque.size()) {
            case 1: return ChatColor.WHITE + deque.poll();
            case 2: return ChatColor.WHITE + deque.poll() + ChatColor.GRAY + " and " + ChatColor.WHITE + deque.poll();
            default:
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < deque.size(); i++) {
                    result.append(i == deque.size() - 1 ? ChatColor.GRAY + "and " : "").append(ChatColor.WHITE).append(deque.poll()).append(ChatColor.GRAY).append(", ");
                }

                return result.toString();
        }
    }
}
