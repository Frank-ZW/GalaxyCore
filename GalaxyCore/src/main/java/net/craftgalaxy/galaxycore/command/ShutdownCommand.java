package net.craftgalaxy.galaxycore.command;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.runnable.ShutdownRunnable;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class ShutdownCommand extends Command {

	/*
	 * This command schedules a server shutdown for a specified time. The
	 * default time is 15 minutes.
	 */

	private final CorePlugin plugin;
	private ShutdownRunnable shutdown;

	public ShutdownCommand(CorePlugin plugin) {
		super("shutdown");
		this.plugin = plugin;
		this.shutdown = null;
		this.setUsage("Usage: /shutdown [time | cancel | start | status]");
		this.setDescription("Schedule a server shutdown.");
	}

	@Override
	public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
		if (!sender.hasPermission(CorePermissions.SHUTDOWN_PERMISSION)) {
			sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
			return true;
		}

		if (args.length != 1) {
			sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To start a shutdown countdown timer, type " + ChatColor.WHITE + "/shutdown [time | start | cancel | status]" + ChatColor.RED + ".");
		} else {
			switch (args[0].toLowerCase()) {
				case "status":
					if (this.shutdown != null) {
						sender.sendMessage(StringUtil.PREFIX + ChatColor.GREEN + " There is an ongoing server shutdown countdown scheduled. The server will shutdown in " + ChatColor.WHITE + this.shutdown.getSecondsRemaining() + " second" + (this.shutdown.getSecondsRemaining() == 1 ? "" : "s") + ChatColor.GREEN + ".");
					} else {
						sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " There is no ongoing server shutdown countdown scheduled. To start a shutdown, type " + ChatColor.WHITE + "/shutdown [time]" + ChatColor.RED + ".");
					}

					break;
				case "cancel":
					if (this.shutdown != null) {
						this.shutdown.cancel();
						this.shutdown = null;
						this.plugin.getServer().broadcastMessage("");
						this.plugin.getServer().broadcastMessage(StringUtil.PREFIX + ChatColor.GRAY + " The scheduled shutdown has been cancelled by " + (sender instanceof Player ? ((Player) sender).getDisplayName() : ChatColor.WHITE + "Console") + ChatColor.GRAY + "!");
						this.plugin.getServer().broadcastMessage("");
					} else {
						sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " There is no active shutdown countdown timer running. To start one, type " + ChatColor.WHITE + "/shutdown start" + ChatColor.RED + ".");
					}

					break;
				case "start":
					if (this.shutdown != null) {
						sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " There is an active shutdown countdown timer running. To cancel, type " + ChatColor.WHITE + "/shutdown cancel" + ChatColor.RED + ".");
						return true;
					}

					this.shutdown = new ShutdownRunnable(this.plugin, 900);
					this.shutdown.runTaskTimerAsynchronously(this.plugin, 20L, 20L);
					this.plugin.getServer().broadcastMessage("");
					this.plugin.getServer().broadcastMessage(StringUtil.PREFIX + ChatColor.GRAY + " The server has been scheduled to shutdown in " + ChatColor.WHITE + 900 + " seconds" + ChatColor.GRAY + ".");
					this.plugin.getServer().broadcastMessage("");
					break;
				default:
					if (this.shutdown != null) {
						sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " There is already an ongoing shutdown countdown.");
						return true;
					}

					int countdown;
					try {
						countdown = Integer.parseInt(args[0]);
					} catch (NumberFormatException e) {
						sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " You must enter a number to start the countdown.");
						return true;
					}

					if (countdown < 1) {
						sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Please enter a positive number to start the countdown.");
						return true;
					}

					this.shutdown = new ShutdownRunnable(this.plugin, countdown);
					this.shutdown.runTaskTimerAsynchronously(this.plugin, 20L, 20L);
					this.plugin.getServer().broadcastMessage("");
					this.plugin.getServer().broadcastMessage(StringUtil.PREFIX + ChatColor.RED + " The server has been scheduled to shutdown in " + ChatColor.WHITE + countdown + " second" + (countdown == 1 ? "" : "s") + ChatColor.RED + ".");
					this.plugin.getServer().broadcastMessage("");
			}
		}

		return true;
	}
}
