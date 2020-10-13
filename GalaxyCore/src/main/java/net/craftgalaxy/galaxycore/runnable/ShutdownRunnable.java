package net.craftgalaxy.galaxycore.runnable;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class ShutdownRunnable extends BukkitRunnable {

	/*
	 * This runnable schedules the shutdown task when executed and counts down from the time specified using
	 * in-game server ticks. That means when the server is lagging, the scheduler will also slow down since it
	 * is not based on the real-world clock. The runnable is scheduled to broadcast the seconds remaining at 6 hours, 3 hours,
	 * 1 hour, 30 minutes, 15 minutes, 3 minutes, 2 minutes, 1 minute, 45 seconds, 30 seconds, 15 seconds, 10
	 * seconds, 5 seconds, 4 seconds, 3 seconds, 2 seconds, 1 second.
	 * Perhaps in the future, use a scheduler not based on Minecraft in-game ticks.
	 */

	private final CorePlugin plugin;

	// The amount of seconds remaining, pretty self explanatory if you ask me
	private int secondsRemaining;

	// Broadcast times
	private final List<Integer> broadcasts = Arrays.asList(21600, 10800, 3600, 1800, 900, 600, 300, 180, 120, 60, 45, 30, 15, 10, 5, 4, 3, 2, 1);

	public ShutdownRunnable(CorePlugin plugin, int secondsRemaining) {
		this.plugin = plugin;
		this.secondsRemaining = secondsRemaining;
	}

	public int getSecondsRemaining() {
		return this.secondsRemaining;
	}

	// This method will be run once every 50ms or one server tick (there are 20 ticks in a second, therefore each tick
	// occupies 50ms)
	@Override
	public void run() {
		if (this.broadcasts.contains(this.secondsRemaining)) {
			this.plugin.getServer().broadcastMessage(StringUtil.PREFIX + ChatColor.RED + " The server will shutdown in " + ChatColor.WHITE + this.secondsRemaining + " second" + (this.secondsRemaining == 1L ? "" : "s") + ChatColor.RED + ".");
		}

		if (this.secondsRemaining <= 0) {
			this.plugin.getServer().getOnlinePlayers().parallelStream().forEach(player -> player.sendMessage(ChatColor.RED + "The server has shut down."));
			this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.getServer().shutdown());
			return;
		}

		this.secondsRemaining--;
	}
}
