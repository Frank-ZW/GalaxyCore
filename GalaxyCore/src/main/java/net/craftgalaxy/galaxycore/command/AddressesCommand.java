package net.craftgalaxy.galaxycore.command;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class AddressesCommand extends Command {

	/*
	 * This command brings up all the past addresses a player has
	 * logged on. Works for both online and offline players. In the
	 * future, I might consider removing all duplicate IP addresses
	 * to prevent spam.
	 */

	private final CorePlugin plugin;

	public AddressesCommand(CorePlugin plugin) {
		super("addresses");
		this.plugin = plugin;
		this.setDescription("List a player's last IP addresses.");
	}

	@Override
	public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
		if (!sender.hasPermission(CorePermissions.ADDRESSES_PERMISSION)) {
			sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
			return true;
		}

		if (args.length == 1) {
			this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
				OfflinePlayer offline = PlayerUtil.getOfflinePlayer(args[0]);
				if (offline == null) {
					sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " That player has never joined the server before.");
				} else if (offline.isOnline()) {
					this.handleOnlinePlayer(sender, offline.getPlayer());
				} else {
					this.handleOfflinePlayer(sender, offline);
				}
			});
		} else {
			sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To list a player's past IP addresses, type " + ChatColor.WHITE + "/addresses <player name>" + ChatColor.RED + ".");
		}

		return true;
	}

	private void handleOnlinePlayer(CommandSender sender, Player player) {
		if (player == null) {
			sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to retrieve the offline player's associated object. If this message occurs, contact the developer.");
			return;
		}

		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null) {
			sender.sendMessage(StringUtil.ERROR_GETTING_DATA);
			return;
		}

		sender.sendMessage(StringUtil.PREFIX + ChatColor.WHITE + " " + player.getName() + ChatColor.GRAY + " has logged on from the following IPs: " + StringUtil.formatGenericDeque(playerData.getIpAddresses()));
	}

	private void handleOfflinePlayer(CommandSender sender, OfflinePlayer offline) {
		PreparedStatement statement;
		ResultSet result;
		Connection connection = this.plugin.getDatabase().getConnection();
		if (connection == null) {
			sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to establish database connection.");
			return;
		}

		LinkedList<String> ipAddresses = null;
		try {
			statement = connection.prepareStatement("SELECT unique_id, ip_addresses FROM userdata");
			result = statement.executeQuery();
			while (result.next()) {
				if (result.getString("unique_id").equalsIgnoreCase(offline.getUniqueId().toString())) {
					ipAddresses = new Gson().fromJson(result.getString("ip_addresses"), new TypeToken<LinkedList<String>>() {}.getType());
					break;
				}
			}

			if (ipAddresses == null) {
				sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to retrieve data from the database. Did you spell the player's name correctly?");
				return;
			}

			sender.sendMessage(StringUtil.PREFIX + ChatColor.WHITE + " " + offline.getName() + ChatColor.GRAY + " has logged on from the following IPs: " + StringUtil.formatGenericDeque(ipAddresses));
			statement.close();
			result.close();
			connection.close();
		} catch (SQLException e) {
			sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Failed to retrieve data from the database.");
		}
	}
}
