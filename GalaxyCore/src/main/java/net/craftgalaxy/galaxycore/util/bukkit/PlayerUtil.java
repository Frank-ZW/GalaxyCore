package net.craftgalaxy.galaxycore.util.bukkit;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R2.IChatBaseComponent;
import net.minecraft.server.v1_16_R2.PacketPlayOutTitle;
import net.minecraft.server.v1_16_R2.PlayerConnection;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlayerUtil {

    public static void messagePlayers(String message, String permission) {
        Collection<? extends Player> players = CorePlugin.getInstance().getServer().getOnlinePlayers();
        for (Player player : players) {
            if (player.hasPermission(permission)) {
                player.sendMessage(message);
            }
        }
    }

    /**
     * Sends a title and subtitle packet to the player using NMS methods.
     *
     * @param player            The player to send the title and subtitle packet.
     * @param titleMessage      The message to be displayed in the title packet.
     * @param subtitleMessage   The message to be displayed in the subtitle packet.
     * @param fadeInTicks       The number of ticks (amount of 50ms) to fade in the message.
     * @param displayTicks      The number of ticks (amount of 50ms) to display the messages.
     * @param fadeOutTicks      The number of ticks (amount of 50ms) to fade out the message.
     */
    public static void sendTitleSubtitlePackets(Player player, String titleMessage, String subtitleMessage, int fadeInTicks, int displayTicks, int fadeOutTicks) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        TextComponent titleText = new TextComponent(titleMessage);
        TextComponent subtitleText = new TextComponent(subtitleMessage);
        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(titleText)), fadeInTicks, displayTicks, fadeOutTicks);
        PacketPlayOutTitle subtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(subtitleText)), fadeInTicks, displayTicks, fadeOutTicks);
        connection.sendPacket(title);
        connection.sendPacket(subtitle);
    }

    /**
     * This method exists since the bukkit method of retrieving an OfflinePlayer object by name is
     * deprecated. This method, quite literally, iterates through the array of offline players and
     * checks if their name is the same as the parameter.
     *
     * @param name  The name of the player.
     * @return      The OfflinePlayer object for the player.
     */
    @Nullable
    public static OfflinePlayer getOfflinePlayer(String name) {
        OfflinePlayer[] offlinePlayers = CorePlugin.getInstance().getServer().getOfflinePlayers();
        for (OfflinePlayer player : offlinePlayers) {
            if (name.equalsIgnoreCase(player.getName())) {
                return player;
            }
        }

        return null;
    }

    /*
     * The list of players that logged in from the same IP-address. This method probably should
     * be written in the AltsCommand class.
     */
    public static String formatAltNames(List<String> playerNames) {
        List<OfflinePlayer> players = new ArrayList<>();
        OfflinePlayer[] offlinePlayers = CorePlugin.getInstance().getServer().getOfflinePlayers();
        for (OfflinePlayer player : offlinePlayers) {
            if (playerNames.contains(player.getName())) {
                players.add(player);
                playerNames.remove(player.getName());
            }
        }

        switch (players.size()) {
            case 0: return "None";
            case 1: return PlayerUtil.handleBanChatColor(players.get(0));
            case 2: return PlayerUtil.handleBanChatColor(players.get(0)) + ChatColor.GRAY + " and " + PlayerUtil.handleBanChatColor(players.get(1));
            default:
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < players.size(); i++) {
                    OfflinePlayer player = players.get(i);
                    if (i == players.size() - 1) {
                        result.append(ChatColor.GRAY).append("and ").append(PlayerUtil.handleBanChatColor(player));
                    } else {
                        result.append(PlayerUtil.handleBanChatColor(player)).append(ChatColor.GRAY).append(", ");
                    }
                }

                return result.toString();
        }
    }

    /**
     * Uses the server's internal banlist to determine whether the player is banned.
     *
     * @param player    The offline player object to check ban status.
     * @return          Red chat color if the player is banned or gray chat color if the player is unbanned.
     */
    private static String handleBanChatColor(OfflinePlayer player) {
        return (player.isBanned() ? ChatColor.RED : ChatColor.GRAY) + player.getName();
    }
}
