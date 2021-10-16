package org.craftgalaxy.galaxycore.proxy.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.player.PlayerData;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

public final class UnfreezeCommand extends Command {

    private final CoreProxyPlugin plugin;

    public UnfreezeCommand(CoreProxyPlugin plugin) {
        super("unfreeze", StringUtil.FREEZE_PERMISSION);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.hasPermission(sender)) {
            return;
        }

        if (args.length == 1) {
            ProxiedPlayer player = this.plugin.getProxy().getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage(new TextComponent(ChatColor.RED + "You can only unfreeze player that are online."));
                return;
            }

            PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player);
            if (playerData == null) {
                player.disconnect(StringUtil.ERROR_LOADING_DATA);
                return;
            }

            if (player.getName().equals(sender.getName())) {
                sender.sendMessage(new TextComponent(ChatColor.RED + "You cannot unfreeze yourself."));
                return;
            }

            if (playerData.unfreeze()) {
                this.plugin.getProxy().broadcast(new TextComponent(ChatColor.DARK_AQUA + "[" + player.getServer().getInfo().getName() + "] " + ChatColor.BLUE + sender.getName() + ChatColor.DARK_AQUA + " unfroze " + ChatColor.GREEN + player.getName() + "."));
            } else {
                sender.sendMessage(new TextComponent(ChatColor.RED + "Failed to unfreeze " + player.getName() + ". This error typically occurs when the player is either already frozen or is on a server that has not been properly connected to the Proxy."));
            }
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + "To unfreeze a player, type /unfreeze <player>"));
        }
    }
}
