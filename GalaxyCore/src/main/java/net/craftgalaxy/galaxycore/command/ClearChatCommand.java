package net.craftgalaxy.galaxycore.command;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class ClearChatCommand extends Command {

	/*
	 * This command clears the global chat by spamming new line characters. Not very
	 * efficient but it's the only way I was able to clear the chat.
	 *
	 * Work in progress - perhaps have player-specific clear chat command for YouTubers and
	 * special donators.
	 */

	private final CorePlugin plugin;

	public ClearChatCommand(CorePlugin plugin) {
		super("clearchat");
		this.plugin = plugin;
		this.setUsage("Usage: /clearchat");
		this.setDescription("Clear the general chat.");
	}

	@Override
	public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
		if (!sender.hasPermission(CorePermissions.CLEAR_CHAT_PERMISSION)) {
			sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
			return true;
		}

		if (args.length == 0) {
			this.clearChat(sender, true);
		} else if (args.length == 1 && args[0].equalsIgnoreCase("-s")) {
			this.clearChat(sender, false);
		} else {
			sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To clear the general chat, type " + ChatColor.WHITE + "/clearchat [-s]" + ChatColor.RED + ".");
		}

		return true;
	}

	private void clearChat(CommandSender sender, boolean displaySender) {
		this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
			String displayMessage = ChatColor.RED + "The chat has been cleared by " + (sender instanceof Player ? ((Player) sender).getDisplayName() : ChatColor.WHITE + "Console") + ChatColor.RESET + ChatColor.RED + ".";
			this.plugin.getServer().getOnlinePlayers().parallelStream().forEach(player -> {
				if (player.hasPermission(CorePermissions.CLEAR_CHAT_PERMISSION)) {
					player.sendMessage("");
					player.sendMessage("");
				} else {
					for (int i = 0; i < 101; i++) {
						player.sendMessage("");
					}
				}

				if (displaySender) {
					player.sendMessage(displayMessage);
				}
			});

			this.plugin.getLogger().log(Level.FINE, "");
			this.plugin.getLogger().log(Level.FINE, "");
			this.plugin.getLogger().info(displayMessage);
		});
	}
}
