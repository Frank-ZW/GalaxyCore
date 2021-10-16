package org.craftgalaxy.galaxycore.proxy.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.player.PlayerData;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class PasswordCommand extends Command {

    private final CoreProxyPlugin plugin;

    public PasswordCommand(CoreProxyPlugin plugin) {
        super("password", StringUtil.AUTHENTICATE_PERMISSION);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return;
        }

        switch (args.length) {
            case 1 -> {
                if (!(sender instanceof ProxiedPlayer player)) {
                    sender.sendMessage(StringUtil.PLAYERS_ONLY);
                    return;
                }

                PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
                if (playerData == null) {
                    player.disconnect(StringUtil.ERROR_LOADING_DATA);
                    return;
                }

                if (playerData.getPassword() == null) {
                    playerData.setPassword(args[0]);
                    player.sendMessage(new TextComponent(ChatColor.GREEN + "Your password has been set to " + ChatColor.WHITE + args[0] + ChatColor.GREEN + ". Don't forget this password since you will need this to authenticate connections from new IP addresses."));
                } else {
                    player.sendMessage(new TextComponent(ChatColor.RED + "You already have a password set. To change your password, type /password <old password> <new password> or contact an administrator if you have forgotten your password."));
                }
            }

            case 2 -> {
                if (sender.equals(ProxyServer.getInstance().getConsole())) {
                    ProxiedPlayer player = this.plugin.getProxy().getPlayer(args[0]);
                    if (player == null) {
                        this.plugin.getProxy().getScheduler().runAsync(this.plugin, () -> {
                            Connection connection = this.plugin.getDatabase().loadConnection();
                            if (connection == null) {
                                sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred while connecting with the embedded database. Contact the developer if this occurs."));
                            } else {
                                try {
                                    PreparedStatement statement = connection.prepareStatement("SELECT username, password, unique_id FROM userdata");
                                    ResultSet result = statement.executeQuery();
                                    String username = null;
                                    String uniqueId = null;
                                    String password = null;

                                    while (result.next()) {
                                        if (result.getString("username").equalsIgnoreCase(args[0])) {
                                            username = result.getString("username");
                                            password = result.getString("password");
                                            uniqueId = result.getString("unique_id");
                                            break;
                                        }
                                    }

                                    if (password == null || username == null || uniqueId == null) {
                                        sender.sendMessage(new TextComponent(ChatColor.RED + "A player with the name " + args[0] + " is not registered in the core database."));
                                        return;
                                    }

                                    statement = connection.prepareStatement("UPDATE userdata SET password = ? WHERE unique_id = ?");
                                    statement.setString(1, args[1]);
                                    statement.setString(2, uniqueId);
                                    statement.executeUpdate();
                                    result.close();
                                    statement.close();
                                    connection.close();
                                    sender.sendMessage(new TextComponent(ChatColor.GREEN + username + "'s password has been set to " + ChatColor.WHITE + args[1] + ChatColor.GREEN + "."));
                                } catch (SQLException e) {
                                    this.plugin.getLogger().log(Level.SEVERE, "An error occurred while connecting to the database. Contact the developer if this occurs", e);
                                }
                            }
                        });
                    } else {
                        if (!this.hasPermission(player)) {
                            sender.sendMessage(new TextComponent(ChatColor.RED + "That player does not have permission to set a password."));
                            return;
                        }

                        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player);
                        if (playerData == null) {
                            player.disconnect(StringUtil.ERROR_LOADING_DATA);
                            return;
                        }

                        playerData.setPassword(args[1]);
                        player.sendMessage(new TextComponent(ChatColor.GREEN + "Your password has been forcibly reset to " + ChatColor.WHITE + args[1] + ChatColor.GREEN + " by " + ChatColor.WHITE + "Console"));
                        sender.sendMessage(new TextComponent(ChatColor.GREEN + player.getName() + "'s password has been set to " + ChatColor.WHITE + args[1]));
                    }
                } else if (sender instanceof ProxiedPlayer player) {
                    PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player);
                    if (playerData == null) {
                        player.disconnect(StringUtil.ERROR_LOADING_DATA);
                        return;
                    }

                    if (args[0].equals(playerData.getPassword())) {
                        playerData.setPassword(args[1]);
                        player.sendMessage(new TextComponent(ChatColor.GREEN + "Your password has been set to " + ChatColor.WHITE + args[1] + ChatColor.GREEN + ". Do not forget this password, you will need to use this password to authenticate connections coming from new IP addresses!"));
                    } else {
                        player.sendMessage(new TextComponent(ChatColor.RED + "The password you entered was incorrect."));
                    }
                } else {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "For security reasons, this command can only be entered by a player or through Console."));
                }
            }

            default -> {
                if (sender.equals(ProxyServer.getInstance().getConsole())) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "To reset a player's password, type /password <player> <password>"));
                } else {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "To reset your password, type /password <old password> <new password> or contact an administrator"));
                }
            }
        }
    }
}
