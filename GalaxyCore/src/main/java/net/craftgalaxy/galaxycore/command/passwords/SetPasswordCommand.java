package net.craftgalaxy.galaxycore.command.passwords;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.data.PlayerData;
import net.craftgalaxy.galaxycore.data.manager.PlayerManager;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import net.craftgalaxy.galaxycore.util.bukkit.PlayerUtil;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.logging.Level;

public class SetPasswordCommand extends Command {

    /*
     * The console command to set a staff member's password. Useful
     * when someone forgets their password. This command can only be
     * run through the console and first checks if the player is
     * online. If the player is online, the plugin sets their password
     * in the PlayerData instance. If the player is offline, the plugin
     * searches through the database and finds the player's row (if it exists)
     * and changes the password.
     */

    private final CorePlugin plugin;

    public SetPasswordCommand(CorePlugin plugin) {
        super("password");
        this.plugin = plugin;
        this.setUsage("Usage: /password <old password> <new password>");
        this.setDescription("Change your in-game authentication password.");
    }

    /**
     *
     * @param sender    The entity or object that sent the command.
     * @param label     The name of the server.
     * @param args      The arguments of the command.
     * @return          Always true since we do not want the server to send a message of the command's format.
     */
    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(CorePermissions.AUTHENTICATE_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return true;
        }

        /*
         * The list of possible commands
         * /password <password>
         * /password <old password> <new password>
         * /password <player name> <new password>
         */
        if (args.length == 1) {
            /*
             * /password <password>
             * If the length of the arguments, that is the array of Strings after the name of the command (password) is equal to one,
             * the plugin sets the player's password to args[0], unless they already have a password.
             */
            if (!(sender instanceof Player)) {
                sender.sendMessage(StringUtil.PLAYER_ONLY);
                return true;
            }

            Player player = (Player) sender;
            PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
            if (playerData == null) {
                player.sendMessage(StringUtil.ERROR_GETTING_DATA);
                return true;
            }

            if (playerData.getPassword() == null) {
                playerData.setPassword(args[0]);
                player.sendMessage(StringUtil.PREFIX + ChatColor.GREEN + " Your password was successfully set to " + ChatColor.WHITE + args[0] + ChatColor.GREEN + ".");
            } else {
                player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " You must specify your old password before you change it to a new password. If you have forgotten your old password, contact one of the developers.");
            }
        } else if (args.length == 2) {
            /*
             * /password <old password> <new password>
             * If the sender is a player, the plugin checks if the first argument args[0] is the correct password. If so,
             * the player's password is updated to args[1].
             */
            if (sender instanceof Player) {
                Player player = (Player) sender;
                PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
                if (playerData == null) {
                    player.sendMessage(StringUtil.ERROR_GETTING_DATA);
                    return true;
                }

                if (!args[0].equals(playerData.getPassword())) {
                    player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " The password you entered was incorrect.");
                } else {
                    playerData.setPassword(args[1]);
                    player.sendMessage(StringUtil.PREFIX + ChatColor.GREEN + " Your password was successfully set to " + ChatColor.WHITE + args[1] + ChatColor.GREEN + ".");
                }

                return true;
            }

            /*
             * /password <player name> <new password>
             * Gets the OfflinePlayer object of the player. The server stores a player's OfflinePlayer object in the server database and the
             * Player object only when the player is online. If the offline player is not online, the plugin sets the player's password in the
             * embedded database. Otherwise, the plugin sets the PlayerData password.
             */
            if (sender instanceof ConsoleCommandSender) {
                ConsoleCommandSender console = (ConsoleCommandSender) sender;
                OfflinePlayer offlinePlayer = PlayerUtil.getOfflinePlayer(args[0]);
                if (offlinePlayer == null) {
                    console.sendMessage(StringUtil.PREFIX + ChatColor.RED + " The player with the specified name has never joined the server.");
                    return true;
                }

                if (offlinePlayer.getPlayer() == null || !offlinePlayer.isOnline()) {
                    Connection connection = this.plugin.getDatabase().getConnection();
                    if (connection == null) {
                        console.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to retrieve database connection.");
                        return true;
                    }

                    try {
                        PreparedStatement countStatement = connection.prepareStatement("SELECT count(*) AS playercount FROM userdata WHERE unique_id = ?");
                        countStatement.setString(1, offlinePlayer.getUniqueId().toString());
                        ResultSet countResult = countStatement.executeQuery();
                        while (countResult.next()) {
                            int count = countResult.getInt("playercount");
                            if (count != 1) {
                                console.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to retrieve the player's password from the database.");
                                countStatement.close();
                                countResult.close();
                                return true;
                            }
                        }

                        PreparedStatement statement = connection.prepareStatement("UPDATE userdata SET password = ? WHERE unique_id = ?");
                        statement.setString(1, args[0]);
                        statement.setString(2, offlinePlayer.getUniqueId().toString());
                        statement.executeUpdate();
                        statement.close();
                        countStatement.close();
                        countResult.close();
                        console.sendMessage(StringUtil.PREFIX + ChatColor.GREEN + " Successfully set " + ChatColor.WHITE + offlinePlayer.getName() + "'s" + ChatColor.GREEN + " password to " + ChatColor.WHITE + args[0] + ChatColor.GREEN + ".");
                    } catch (SQLException e) {
                        this.plugin.getLogger().log(Level.SEVERE, "Failed to fetch the player's UUID and password from the database", e);
                    } finally {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            this.plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
                        }
                    }
                } else {
                    Player player = offlinePlayer.getPlayer();
                    PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
                    if (playerData == null) {
                        console.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to retrieve the player data associated with that player.");
                        return true;
                    }

                    playerData.setPassword(args[1]);
                    player.sendMessage(StringUtil.PREFIX + ChatColor.GREEN + " Your password was successfully set to " + ChatColor.WHITE + args[1] + ChatColor.GREEN + " by " + ChatColor.WHITE + "Console" + ChatColor.GREEN + ".");
                    console.sendMessage(StringUtil.PREFIX + ChatColor.GREEN + " Successfully set " + ChatColor.WHITE + player.getName() + "'s" + ChatColor.GREEN + " password to " + ChatColor.WHITE + args[1] + ChatColor.GREEN + ".");
                }

                return true;
            }
        } else {
            if (sender instanceof Player) {
                sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To change your password, type " + ChatColor.WHITE + "/password <old password> <new password>" + ChatColor.RED + ".");
            } else if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(ChatColor.RED + " To change a staff member's password, type " + ChatColor.WHITE + "/password <player> <new password>" + ChatColor.RED + ".");
            }
        }

        return true;
    }
}
