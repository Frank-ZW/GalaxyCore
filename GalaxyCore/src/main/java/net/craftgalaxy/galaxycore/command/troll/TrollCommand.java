package net.craftgalaxy.galaxycore.command.troll;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.command.SubCommand;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class TrollCommand extends Command {

    private final CorePlugin plugin;
    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public TrollCommand(CorePlugin plugin) {
        super("troll");
        this.plugin = plugin;
        this.setUsage("Usage: /troll <player> <type>");
        this.setDescription("Troll the specified player.");
        this.subcommands.put("switch", new SwitchSubcommand(plugin));
        this.subcommands.put("demomenu", new DemoMenuSubcommand(plugin));
    }

    public SubCommand getSubCommand(String name) {
        return this.subcommands.get(name);
    }

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(CorePermissions.TROLL_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(StringUtil.PLAYER_ONLY);
            return true;
        }

        Player player = (Player) sender;
        switch (args.length) {
            case 0:
                player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To troll a player, type " + ChatColor.WHITE + "/troll <player name>" + ChatColor.RED + " to bring up the list of trolls.");
                break;
            case 1:
                player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " The available troll types are " + ChatColor.WHITE + "demomenu" + ChatColor.RED + " and " + ChatColor.WHITE + "switch" + ChatColor.RED + ".");
                break;
            default:
                Player target = this.plugin.getServer().getPlayerExact(args[0]);
                if (target == null) {
                    player.sendMessage(StringUtil.PLAYER_OFFLINE);
                    return true;
                }

                switch (args[1].toLowerCase()) {
                    case "demomenu":
                        this.getSubCommand("demomenu").accept(player, target, args);
                        break;
                    case "switch":
                        this.getSubCommand("switch").accept(player, target, args);
                        break;
                    default:
                        player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " That is an invalid troll dommand.");
                }
        }

        return true;
    }
}
