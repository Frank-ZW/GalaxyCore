package net.craftgalaxy.galaxycore.command.troll;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.command.SubCommand;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.entity.Player;

public class CrashSubcommand extends SubCommand {

    /*
     * This is currently a work in progress. Just a helpful command to
     * troll (or mess with) cheaters and hackers.
     */

    public CrashSubcommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    public void accept(Player sender, Player target, String[] args) {
        if (!sender.hasPermission(CorePermissions.TROLL_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return;
        }
    }
}
