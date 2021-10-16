package org.craftgalaxy.galaxycore.proxy.command;

import me.leoko.advancedban.manager.PunishmentManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class AltsCommand extends Command {

    private final CoreProxyPlugin plugin;

    public AltsCommand(CoreProxyPlugin plugin) {
        super("alts", StringUtil.ALTS_PERMISSION);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return;
        }

        if (args.length == 1) {
            this.plugin.getProxy().getScheduler().runAsync(this.plugin, () -> {
                ProxiedPlayer player = this.plugin.getProxy().getPlayer(args[0]);
                String host = null;
                if (player == null) {
                    try (
                            Connection connection = this.plugin.getDatabase().loadConnection();
                            PreparedStatement statement = connection.prepareStatement("SELECT username, recent_address FROM userdata");
                            ResultSet result = statement.executeQuery()
                    ) {
                        while (result.next()) {
                            if (result.getString("username").equalsIgnoreCase(args[0])) {
                                host = result.getString("recent_address");
                                break;
                            }
                        }
                    } catch (SQLException e) {
                        sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred while connecting to the embedded database"));
                        this.plugin.getLogger().log(Level.SEVERE, null, e);
                        return;
                    }
                } else {
                    host = ((InetSocketAddress) player.getSocketAddress()).getAddress().getHostAddress();
                }

                if (host == null) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Failed to fetch the IP address of the player from the database."));
                    return;
                }

                List<String> alts = new ArrayList<>();
                boolean dependencyPresent = this.plugin.isDependencyPresent("AdvancedBan");
                for (ProxiedPlayer p : this.plugin.getProxy().getPlayers()) {
                    InetSocketAddress remoteAddress = (InetSocketAddress) p.getSocketAddress();
                    if (host.equals(remoteAddress.getAddress().getHostAddress())) {
                        if (dependencyPresent) {
                            alts.add((PunishmentManager.get().isBanned(p.getUniqueId().toString().replace("-", "")) ? ChatColor.RED : ChatColor.GREEN) + p.getName());
                        } else {
                            alts.add(p.getName());
                        }
                    }
                }

                try (
                        Connection connection = this.plugin.getDatabase().loadConnection();
                        PreparedStatement statement = connection.prepareStatement("SELECT username, unique_id, recent_address FROM userdata");
                        ResultSet result = statement.executeQuery()
                ) {
                    while(result.next()) {
                        String username = result.getString("username");
                        if (!alts.contains(username) && host.equals(result.getString("recent_address"))) {
                            if (dependencyPresent) {
                                alts.add((PunishmentManager.get().isBanned(result.getString("unique_id").replace("-", "")) ? ChatColor.RED : ChatColor.GREEN) + username);
                            } else {
                                alts.add(username);
                            }
                        }
                    }
                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred while connecting to the database."));
                    this.plugin.getLogger().log(Level.SEVERE, null, e);
                    return;
                }

                StringBuilder builder = new StringBuilder(StringUtil.SERVER_PREFIX + ChatColor.YELLOW + " The following players logged on from " + ChatColor.LIGHT_PURPLE + host + ChatColor.YELLOW + ": ");
                switch(alts.size()) {
                    case 0:
                        break;
                    case 1:
                        builder.append(alts.get(0));
                        break;
                    case 2:
                        builder.append(alts.get(0)).append(ChatColor.YELLOW).append(" and ").append(alts.get(1));
                        break;
                    default:
                        for (int i = 0; i < alts.size(); i++) {
                            if (i == alts.size() - 1) {
                                builder.append("and ").append(alts.get(i));
                            } else {
                                builder.append(alts.get(i)).append(ChatColor.YELLOW).append(", ");
                            }
                        }
                }

                sender.sendMessage(new TextComponent(builder.toString()));
            });
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + "To view a player's alts, type /alts <player>"));
        }
    }
}
