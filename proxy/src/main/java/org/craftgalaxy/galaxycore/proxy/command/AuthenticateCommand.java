package org.craftgalaxy.galaxycore.proxy.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.player.PlayerData;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

public final class AuthenticateCommand extends Command {

    private final CoreProxyPlugin plugin;

    public AuthenticateCommand(CoreProxyPlugin plugin) {
        super("authenticate", StringUtil.AUTHENTICATE_PERMISSION);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return;
        }

        if (sender instanceof ProxiedPlayer player) {
            PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player);
            if (playerData == null) {
                player.disconnect(StringUtil.ERROR_LOADING_DATA);
                return;
            }

            if (args.length == 1) {
                if (!playerData.isFrozen()) {
                    player.sendMessage(new TextComponent(ChatColor.RED + "You must be frozen to run this command."));
                    return;
                }

                if (args[0].equals(playerData.getPassword())) {
                    playerData.unfreeze();
                } else {
                    player.sendMessage(new TextComponent(ChatColor.RED + "The password you entered was incorrect. If you log off while frozen, your account will be banned."));
                }
            } else {
                player.sendMessage(new TextComponent(ChatColor.RED + "To confirm your connection, type /authenticate <password>."));
            }
        } else {
            sender.sendMessage(StringUtil.PLAYERS_ONLY);
        }
    }
}
