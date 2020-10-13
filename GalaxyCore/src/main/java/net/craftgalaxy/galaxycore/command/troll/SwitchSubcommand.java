package net.craftgalaxy.galaxycore.command.troll;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.command.SubCommand;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SwitchSubcommand extends SubCommand {

	/*
	 * Troll command to mess with players by switching two players' location and
	 * switching their positions. This command is purely for fun and is in no way
	 * meant to be taken seriously. :)
	 */

	public SwitchSubcommand(CorePlugin plugin) {
		super(plugin);
	}

	@Override
	public void accept(Player sender, Player target, String[] args) {
		if (args.length == 3) {
			Player switchPlayer = this.plugin.getServer().getPlayerExact(args[2]);
			if (switchPlayer == null) {
				sender.sendMessage(StringUtil.PLAYER_OFFLINE);
				return;
			}

			Location targetLocation = target.getLocation();
			Location switchLocation = switchPlayer.getLocation();
			switchPlayer.teleport(targetLocation);
			target.teleport(switchLocation);
		} else {
			sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To switch two players, type " + ChatColor.WHITE + "/troll <player name> switch <other name>" + ChatColor.RED + ".");
		}
	}
}
