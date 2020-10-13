package net.craftgalaxy.galaxycore.command;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.data.manager.PlayerManager;
import net.craftgalaxy.galaxycore.util.bukkit.PlayerUtil;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import javax.annotation.Nonnull;

public class GalaxyAdminCommand extends Command {

	/*
	 * Background information: This command is for adding certain players to the internal whitelist the plugin uses
	 * to authenticate certain commands. To prevent players from hacking into staff member's accounts and giving themselves
	 * permission to damage the server, I've added an internal whitelist called managerUUIDs and staffUUIDs that gives
	 * specific players special permission to run certain staff commands.
	 *
	 * This command is an extra layer of protection on our server.
	 */

	private final CorePlugin plugin;

	public GalaxyAdminCommand(CorePlugin plugin) {
		super("cgadmin");
		this.plugin = plugin;
		this.setDescription("Modify the internal permissions listing for the GalaxyCore plugin.");
		this.setUsage("/cgadmin <player name> [add | remove] [-s | -m]");
	}

	@Override
	public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
		if (!(sender instanceof ConsoleCommandSender)) {
			sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " This command can only be performed through console.");
			return true;
		}

		ConsoleCommandSender console = (ConsoleCommandSender) sender;
		if (args.length == 3) {
			this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
				OfflinePlayer target = PlayerUtil.getOfflinePlayer(args[0]);
				if (target == null) {
					console.sendMessage(StringUtil.PREFIX + ChatColor.RED + " That player has never joined the server.");
				} else {
					switch (args[1].toLowerCase()) {
						case "add":
							if (args[2].equalsIgnoreCase("-m") || args[2].equalsIgnoreCase("-manager")) {
								console.sendMessage(StringUtil.PREFIX + ChatColor.RED + " The list of internal managers for the server is immutable.");
							} else if (args[2].equalsIgnoreCase("-s") || args[2].equalsIgnoreCase("-staff")) {
								PlayerManager.getInstance().addStaff(target.getUniqueId());
								console.sendMessage(StringUtil.PREFIX + ChatColor.GREEN + " Successfully internally whitelisted " + ChatColor.WHITE + target.getName() + ChatColor.GREEN + ". They can now run WorldEdit commands without triggering the Alert filter.");
							} else {
								console.sendMessage(StringUtil.PREFIX + ChatColor.RED + " The only two available arguments are -s or -m.");
							}

							break;
						case "remove":
							if (args[2].equalsIgnoreCase("-m") || args[2].equalsIgnoreCase("-manager")) {
								PlayerManager.getInstance().removeManager(target.getUniqueId());
								console.sendMessage(StringUtil.PREFIX + ChatColor.GREEN + " If " + ChatColor.WHITE + target.getName() + ChatColor.GREEN + " was originally on the internal whitelist, he has been removed from the managers whitelist.");
							} else if (args[2].equalsIgnoreCase("-s") || args[2].equalsIgnoreCase("-staff")) {
								PlayerManager.getInstance().removeStaff(target.getUniqueId());
								console.sendMessage(StringUtil.PREFIX + ChatColor.GREEN + " If " + ChatColor.WHITE + target.getName() + ChatColor.GREEN + " was originally on the internal whitelist, he has been removed from the staff team whitelist.");
							} else {
								console.sendMessage(StringUtil.PREFIX + ChatColor.RED + " The only two available arguments are -s or -m.");
							}

							break;
						default:
							console.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To internally whitelist a player, type " + ChatColor.WHITE + "/cgadmin <player name> [add | remove] [-s | -m]" + ChatColor.RED + ".");
					}
				}
			});
		} else {
			console.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To internally whitelist a player, type " + ChatColor.WHITE + "/cgadmin <player name> [add | remove] [-s | -m]" + ChatColor.RED + ".");
		}

		return true;
	}
}
