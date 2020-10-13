package net.craftgalaxy.galaxycore.command.troll;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.command.SubCommand;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import net.minecraft.server.v1_16_R2.PacketPlayOutGameStateChange;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class DemoMenuSubcommand extends SubCommand {

    /*
     * Troll command to mess with players by forcing their Minecraft client to show up
     * the demo menu prompting them to buy the game. This command is purely for fun and
     * is in no way meant to be taken seriously. :)
     */

    public DemoMenuSubcommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    public void accept(Player sender, Player target, String[] args) {
        if (args.length == 2) {
            PacketPlayOutGameStateChange gameStateChange = new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.f, 0.0F);
            ((CraftPlayer) target).getHandle().playerConnection.sendPacket(gameStateChange);
        } else {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To demo menu troll a player, type " + ChatColor.WHITE + "/troll <player name> demomenu" + ChatColor.RED + ".");
        }
    }
}
