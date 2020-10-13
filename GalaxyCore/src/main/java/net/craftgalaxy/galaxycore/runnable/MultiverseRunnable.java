package net.craftgalaxy.galaxycore.runnable;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MultiverseRunnable implements Runnable {

	/*
	 * Runnable set to run when one of my friends joined - it is
	 * completely unnecessary to the plugin and is only added for
	 * fun.
	 */

	private final CorePlugin plugin;
	private final Player player;

	public MultiverseRunnable(CorePlugin plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
	}

	@Override
	public void run() {
		this.player.sendMessage(ChatColor.DARK_GRAY + "RecordTime" + ChatColor.GREEN + " joined.");
		this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () -> this.player.sendMessage(ChatColor.DARK_GRAY + "Multiverse" + ChatColor.GREEN + " joined."), 200L);
		this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () -> this.player.sendMessage(ChatColor.DARK_GRAY + "Z3nyph" + ChatColor.GREEN + " joined."), 280L);
		this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () -> {
			this.player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "[Server: Made RecordTime a server operator]");
			this.player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "[Server: Made Multiverse a server operator]");
			this.player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "[Server: Made Z3nyph a server operator]");
		}, 420L);
		this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () -> {
			this.player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Detected unknown IP login and UUID spoof for the following players: " + ChatColor.WHITE + "RecordTime, Multiverse, " + ChatColor.RED + "and " + ChatColor.WHITE + "Z3nyph" + ChatColor.RED + ".");
			this.player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " Manually overriding default player permissions and silently blacklisting the follwing players: " + ChatColor.WHITE + "RecordTime, Multiverse, " + ChatColor.RED + "and " + ChatColor.WHITE + "Z3nyph" + ChatColor.RED + ".");
		}, 440L);
		this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () -> {
			this.player.sendMessage(ChatColor.DARK_GRAY + "Z3nyph" + ChatColor.RED + " left.");
			this.player.sendMessage(ChatColor.DARK_GRAY + "RecordTime" + ChatColor.RED + " left.");
			this.player.sendMessage(ChatColor.DARK_GRAY + "Multiverse" + ChatColor.RED + " left.");
		}, 460L);
	}
}
