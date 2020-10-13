package net.craftgalaxy.galaxycore.command;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.gui.GuiClickable;
import net.craftgalaxy.galaxycore.gui.GuiFolder;
import net.craftgalaxy.galaxycore.gui.GuiPage;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import net.craftgalaxy.galaxycore.util.bukkit.ItemUtil;
import net.craftgalaxy.galaxycore.util.bukkit.PlayerUtil;
import net.craftgalaxy.galaxycore.util.java.CooldownList;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ReportCommand extends Command {

	/*
	 * This command allows players to report others for rule violations and
	 * alerts online staff.
	 */

	private final CooldownList<UUID> cooldowns;

	private final CorePlugin plugin;

	public ReportCommand(CorePlugin plugin) {
		super("report");
		this.plugin = plugin;
		this.cooldowns = new CooldownList<>(TimeUnit.SECONDS, 60L);
		this.setUsage("Usage: /report <player>");
		this.setDescription("Report a player");
	}

	@Override
	public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(StringUtil.PLAYER_ONLY);
			return true;
		}

		Player player = (Player) sender;
		if (args.length != 1) {
			player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To report a player, type " + ChatColor.WHITE + "/report <player>" + ChatColor.RED + ".");
		} else {
			if (!this.cooldowns.isExpired(player.getUniqueId())) {
				long secondsLeft = this.cooldowns.getSecondsRemaining(player.getUniqueId());
				if (secondsLeft > 0L) {
					player.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " You must wait " + ChatColor.WHITE + secondsLeft + " second" + (secondsLeft == 1L ? "" : "s") + ChatColor.GRAY + " before you can report a player again.");
					return true;
				}
			}

			Player target = this.plugin.getServer().getPlayerExact(args[0]);
			if (target == null) {
				player.sendMessage(StringUtil.PLAYER_OFFLINE);
				return true;
			}

			if (target.getName().equals(player.getName())) {
				player.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " You cannot report yourself.");
				return true;
			}

			if (target.hasPermission(CorePermissions.REPORT_NOTIFICATION)) {
				player.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " If you wish to report this player, please create a ticket on the Discord.");
				return true;
			}

			GuiFolder folder = new GuiFolder(ChatColor.BLUE + "Report Menu", 9);
			GuiPage page = new GuiPage(folder);
			List<ItemStack> items = new ArrayList<>(Arrays.asList(ItemUtil.createItemStack(Material.DIAMOND_SWORD, ChatColor.RED + "Unfair Advantage", 1), ItemUtil.createItemStack(Material.GRASS_BLOCK, ChatColor.RED + "Inappropriate Builds", 1), ItemUtil.createItemStack(Material.WRITABLE_BOOK, ChatColor.RED + "Chat Violation", 1), ItemUtil.createItemStack(Material.ENDER_PEARL, ChatColor.RED + "Other", 1)));
			for (int i = 1; i < 8; i += 2) {
				page.addItem(i, new ReportClickable(items, this, player, target.getName()));
			}

			page.fill();
			folder.setCurrentPage(page);
			folder.openGui(player);
		}

		return true;
	}

	private static class ReportClickable implements GuiClickable {

		private final List<ItemStack> items;
		private final ReportCommand command;
		private final Player player;
		private final String target;

		public ReportClickable(List<ItemStack> items, ReportCommand command, Player player, String target) {
			this.items = items;
			this.command = command;
			this.player = player;
			this.target = target;
		}

		@Override
		public void onInventoryClick(InventoryClickEvent e) {
			this.player.closeInventory();
			this.command.cooldowns.addCooldown(this.player.getUniqueId());
			if (e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null) {
				String reportMessage = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Report" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + this.player.getName() + ChatColor.GRAY + " has reported " + ChatColor.RED + this.target + ChatColor.GRAY + " for " + e.getCurrentItem().getItemMeta().getDisplayName() + ChatColor.RED + ".";
				this.player.sendMessage(ChatColor.GREEN + "Thank you for reporting. Your report has been submitted.");
				PlayerUtil.messagePlayers("", CorePermissions.REPORT_NOTIFICATION);
				PlayerUtil.messagePlayers(reportMessage, CorePermissions.REPORT_NOTIFICATION);
				PlayerUtil.messagePlayers("", CorePermissions.REPORT_NOTIFICATION);
				CorePlugin.getInstance().getServer().getConsoleSender().sendMessage(reportMessage);
			}
		}

		@Override
		public ItemStack getItemStack() {
			return this.items.remove(0);
		}
	}
}
