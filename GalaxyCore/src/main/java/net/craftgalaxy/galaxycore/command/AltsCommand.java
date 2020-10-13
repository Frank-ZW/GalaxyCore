package net.craftgalaxy.galaxycore.command;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import net.craftgalaxy.galaxycore.util.bukkit.PlayerUtil;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class AltsCommand extends Command {

    /*
     * One of the rules on our server is: no use of alternate accounts to circumvent
     * punishments and cheat. This command detects alts based on every player's latest IP
     * address.
     *
     * If the player specified is online, the command grabs the player's IP address and
     * loops through every player entry in the database for any shared IP addresses.
     * If the player specified is offline, the command first checks whether there is an entry
     * in the database with the same player username and if so, grabs the IP address. Then,
     * like before, loops through the database to find any shared IP addresses. Players with
     * the same IP address are recorded and sent to the command sender.
     *
     * As of right now, this command is not very efficient - I plan to reduce the amount of
     * looping in the future, but its impact is minimal since this is all done asynchronously.
     */

    private final CorePlugin plugin;

    public AltsCommand(CorePlugin plugin) {
        super("alts");
        this.plugin = plugin;
        this.setUsage("Usage: /alts <player>");
        this.setDescription("Retrieves all the alternate accounts for the specified player.");
    }

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(CorePermissions.ALTS_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To view a player's alt accounts, type " + ChatColor.WHITE + "/alts <player>" + ChatColor.RED + ".");
        } else {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                OfflinePlayer player = PlayerUtil.getOfflinePlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " That player has never joined the server before.");
                } else {
                    if (player.isOnline() && player.getPlayer() != null) {
                        this.handleOnlinePlayer(sender, player.getPlayer());
                    } else {
                        this.handleOfflinePlayer(sender, player);
                    }
                }
            });

//            Player player = this.plugin.getServer().getPlayerExact(args[0]);
//            if (player == null) {
//                sender.sendMessage(StringUtil.PLAYER_OFFLINE);
//                return true;
//            }
//
//            InetSocketAddress socketAddress = player.getAddress();
//            if (socketAddress == null) {
//                sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " The player has an invalid socket address.");
//                return true;
//            }
//
//            String ip = socketAddress.getAddress().getHostAddress();
//            Connection connection = this.plugin.getDatabase().getConnection();
//            if (connection == null) {
//                sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to retrieve database connection.");
//                return true;
//            }
//
//            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
//                List<String> alts = new ArrayList<>();
//                alts.add(player.getName());
//                try {
//                    PreparedStatement statement = connection.prepareStatement("SELECT username, ip_addresses FROM userdata");
//                    ResultSet result = statement.executeQuery();
//                    while (result.next()) {
//                        Deque<String> ipAddresses = new Gson().fromJson(result.getString("ip_addresses"), new TypeToken<LinkedList<String>>() {}.getType());
//                        String name = result.getString("username");
//                        if (!ipAddresses.isEmpty() && ip.equals(ipAddresses.getLast())) {
//                            alts.add(name);
//                        }
//                    }
//                } catch (SQLException e) {
//                    this.plugin.getLogger().log(Level.SEVERE, "Failed to retrieve database connection", e);
//                }
//
//                Collection<? extends Player> onlinePlayers = this.plugin.getServer().getOnlinePlayers();
//                for (Player online : onlinePlayers) {
//                    InetSocketAddress socket = online.getAddress();
//                    if (socket == null) {
//                        continue;
//                    }
//
//                    String onlineAddress = socket.getAddress().getHostAddress();
//                    if (ip.equals(onlineAddress) && !alts.contains(online.getName())) {
//                        alts.add(online.getName());
//                    }
//                }
//
//                sender.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " The following players have logged on from that IP: " + PlayerUtil.toString(alts));
//            });
        }

        return true;
    }

    private void handleOnlinePlayer(CommandSender sender, Player player) {
        InetSocketAddress socketAddress = player.getAddress();
        if (socketAddress == null) {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " The player has an invalid socket address.");
            return;
        }

        String ip = socketAddress.getAddress().getHostAddress();
        Connection connection = this.plugin.getDatabase().getConnection();
        if (connection == null) {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to retrieve database connection.");
            return;
        }

        this.handleAlts(sender, ip);
    }

    private void handleOfflinePlayer(CommandSender sender, OfflinePlayer player) {
        PreparedStatement statement;
        ResultSet result;
        Connection connection = this.plugin.getDatabase().getConnection();
        if (connection == null) {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to retrieve database connection.");
            return;
        }

        String ip = null;
        try {
            statement = connection.prepareStatement("SELECT username, ip_addresses FROM userdata");
            result = statement.executeQuery();
            while (result.next()) {
                if (result.getString("username").equalsIgnoreCase(player.getName())) {
                    LinkedList<String> ipAddresses = new Gson().fromJson(result.getString("ip_addresses"), new TypeToken<LinkedList<String>>() {}.getType());
                    if (!ipAddresses.isEmpty()) {
                        ip = ipAddresses.getLast();
                    }

                    break;
                }
            }
        } catch (SQLException e) {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to retrieve data from the database.");
            return;
        }

        if (ip == null) {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to determine the IP address of the specified player.");
            return;
        }

        this.handleAlts(sender, ip);
    }

    private void handleAlts(CommandSender sender, String ip) {
        List<String> alts = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet result = null;
        Connection connection = this.plugin.getDatabase().getConnection();
        if (connection == null) {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to establish database connection.");
            return;
        }

        try {
            statement = connection.prepareStatement("SELECT username, ip_addresses FROM userdata");
            result = statement.executeQuery();
            while (result.next()) {
                Deque<String> ipAddresses = new Gson().fromJson(result.getString("ip_addresses"), new TypeToken<LinkedList<String>>() {}.getType());
                if (!ipAddresses.isEmpty() && ip.equalsIgnoreCase(ipAddresses.getLast())) {
                    alts.add(result.getString("username"));
                }
            }
        } catch (SQLException e) {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to retrieve data from the database.");
            return;
        } finally {
            this.plugin.getDatabase().close(result, statement);
        }

        Collection<? extends Player> onlinePlayers = this.plugin.getServer().getOnlinePlayers();
        for (Player online : onlinePlayers) {
            InetSocketAddress onlineAddress = online.getAddress();
            if (onlineAddress == null) {
                continue;
            }

            String onlineIp = onlineAddress.getAddress().getHostAddress();
            if (ip.equalsIgnoreCase(onlineIp) && !alts.contains(online.getName())) {
                alts.add(online.getName());
            }
        }

        sender.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " The following players have logged on from the same IP: " + PlayerUtil.formatAltNames(alts));
    }
}
