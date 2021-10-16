package org.craftgalaxy.galaxycore.proxy.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.player.PlayerData;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

public final class FreezeCommand extends Command {

    private final CoreProxyPlugin plugin;

    public FreezeCommand(CoreProxyPlugin plugin) {
        super("freeze", StringUtil.FREEZE_PERMISSION);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return;
        }

        if (args.length == 1) {
            ProxiedPlayer target = this.plugin.getProxy().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(new TextComponent(ChatColor.RED + "You can only freeze players that are online."));
                return;
            }

            if (target.hasPermission(StringUtil.AUTHENTICATE_PERMISSION) && !sender.equals(ProxyServer.getInstance().getConsole())) {
                sender.sendMessage(new TextComponent(ChatColor.RED + "This player can only be frozen through Console."));
                return;
            }

            if (target.getName().equals(sender.getName())) {
                sender.sendMessage(new TextComponent(ChatColor.RED + "You cannot freeze yourself."));
                return;
            }

            PlayerData targetData = this.plugin.getPlayerManager().getPlayerData(target);
            if (targetData == null) {
                target.disconnect(StringUtil.ERROR_LOADING_DATA);
                return;
            }

            if (targetData.freeze()) {
                this.plugin.getProxy().broadcast(new TextComponent(ChatColor.DARK_AQUA + "[" + target.getServer().getInfo().getName() + "] " + ChatColor.BLUE + sender.getName() + ChatColor.DARK_AQUA + " froze " + ChatColor.GREEN + target.getName() + "."));
            } else {
                sender.sendMessage(new TextComponent(ChatColor.RED + "Failed to freeze " + target.getName() + ". This error typically occurs when the player is either already frozen or is on a server that has not been properly connected to the Proxy."));
            }
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + "To freeze a player, type /freeze <player>"));
        }
    }
}
