package net.craftgalaxy.galaxycore.command;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MinersCommand extends Command {

	/*
	 * This command lists all the players below a certain Y-level and
	 * is useful for catching x-ray hackers. X-ray is a hack that allows
	 * players to see through blocks to gain an unfair advantage mining
	 * ores such as iron, gold, diamonds, etc. Since our currency is
	 * based on gold, x-ray would hurt the economy and introduce unnecessary
	 * inflation into the economy.
	 */

	private final CorePlugin plugin;

	public MinersCommand(CorePlugin plugin) {
		super("miners");
		this.plugin = plugin;
		this.setUsage("Usage: /miners [Y-level]");
		this.setDescription("List all the players below a certain Y-level.");
	}

	/**
	 * @param sender The player, object, or entity that sent the command
	 * @param label The name of the plugin
	 * @param args The list of arguments in the command
	 * @return True
	 */
	@Override
	public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
		if (!sender.hasPermission(CorePermissions.MINERS_PERMISSION)) {
			sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
			return true;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage(StringUtil.PLAYER_ONLY);
			return true;
		}

		Player player = (Player) sender;
		if (args.length > 1) {
			player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To view all the players below a certain Y-level, type " + ChatColor.WHITE + "/miners [Y-level]" + ChatColor.RED + ". If no Y-level is specified, the default value of " + ChatColor.WHITE + "Y = 64" + ChatColor.RED + " will be used.");
			return true;
		}

		/*
		 * The plugin will default to a Y-level of 64 or, if a number is specified,
		 * set the Y-level to the specified number.
		 */
		int yLevel;
		if (args.length == 1) {
			try {
				yLevel = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " The argument entered must be a number.");
				return true;
			}
		} else {
			yLevel = 64;
		}

		/*
		 * Counts the total number of players below the specified Y-level
		 * by looping through all the players in the command sender's world
		 * and checking their location.
		 */
		this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
			List<Player> players = player.getWorld().getPlayers();
			List<Player> miners = new ArrayList<>();
			for (Player target : players) {
				if (target.getLocation().getBlockY() < yLevel) {
					miners.add(target);
				}
			}

			player.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " The following players are below Y = " + yLevel + ChatColor.GRAY + ": " + ChatColor.WHITE + this.formatNames(miners));
		});

		return true;
	}

	/*
	 * This is just to make the names of the players look pretty.
	 */
	private String formatNames(List<Player> players) {
		switch (players.size()) {
			case 0: return "";
			case 1: return players.get(0).getName();
			case 2: return players.get(0).getName() + " and " + players.get(1).getName();
			default:
				StringBuilder result = new StringBuilder();
				for (int i = 0; i < players.size(); i++) {
					result.append(i == players.size() - 1 ? "and " + players.get(i).getName() : players.get(i).getName() + ", ");
				}

				return result.toString();
		}
	}
}
