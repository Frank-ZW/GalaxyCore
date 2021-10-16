package org.craftgalaxy.galaxycore.proxy.command;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class IPLookupCommand extends Command {

    private final CoreProxyPlugin plugin;
    private final Gson gson = new Gson();

    public IPLookupCommand(CoreProxyPlugin plugin) {
        super("iplookup", StringUtil.IP_LOOKUP_PERMISSION);
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
                List<String> players = new ArrayList<>();
                try (
                        Connection connection = this.plugin.getDatabase().loadConnection();
                        PreparedStatement statement = connection.prepareStatement("SELECT username, ip_addresses FROM userdata");
                        ResultSet result = statement.executeQuery()
                ) {
                    while (result.next()) {
                        List<String> addresses = this.gson.fromJson(result.getString("ip_addresses"), new TypeToken<ArrayList<String>>() {}.getType());
                        if (addresses.contains(args[0])) {
                            players.add(result.getString("username"));
                        }
                    }

                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred while retrieving data from the database."));
                    this.plugin.getLogger().log(Level.SEVERE, null, e);
                    return;
                }

                StringBuilder builder = new StringBuilder(StringUtil.SERVER_PREFIX + ChatColor.YELLOW + " The following players have joined from " + ChatColor.LIGHT_PURPLE + args[0] + ChatColor.YELLOW + ": ");
                switch(players.size()) {
                    case 0:
                        break;
                    case 1:
                        builder.append(ChatColor.GREEN).append(players.get(0));
                        break;
                    case 2:
                        builder.append(ChatColor.GREEN).append(players.get(0)).append(ChatColor.YELLOW).append(" and ").append(ChatColor.GREEN).append(players.get(1));
                        break;
                    default:
                        for(int i = 0; i < players.size(); i++) {
                            if (i == players.size() - 1) {
                                builder.append("and ").append(ChatColor.GREEN).append(players.get(i));
                            } else {
                                builder.append(ChatColor.GREEN).append(players.get(i)).append(ChatColor.YELLOW).append(", ");
                            }
                        }
                }

                sender.sendMessage(new TextComponent(builder.toString()));
            });
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + "To search an IP address, type /iplookup <address>"));
        }
    }
}
