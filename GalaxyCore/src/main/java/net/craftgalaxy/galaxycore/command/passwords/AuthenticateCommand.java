package net.craftgalaxy.galaxycore.command.passwords;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.data.PlayerData;
import net.craftgalaxy.galaxycore.data.manager.PlayerManager;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;

public class AuthenticateCommand extends Command {

    /*
     * The authenticate command is used by staff members to authenticate
     * their connection or to unfreeze themselves. When a staff member joins
     * from a new ip address, the plugin automatically freezes the player and
     * asks them for their password. This is to prevent hackers from joining
     * the server through a staff member's account and abusing.
     *
     * In the future, add a limit to the amount of passwords the player can try.
     */

    private final CorePlugin plugin;

    public AuthenticateCommand(CorePlugin plugin) {
        super("authenticate");
        this.plugin = plugin;
        this.setUsage("Usage: /authenticate <password>");
        this.setDescription("Authenticate your connection and unfreeze yourself.");
    }

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(CorePermissions.AUTHENTICATE_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(StringUtil.PLAYER_ONLY);
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
        if (playerData == null) {
            player.sendMessage(StringUtil.ERROR_GETTING_DATA);
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " To confirm your connection, type " + ChatColor.WHITE + "/authenticate <password>" + ChatColor.RED + ".");
        } else {
            if (!playerData.isFrozen()) {
                player.sendMessage(ChatColor.RED + "You must be already frozen to run this command.");
                return true;
            }

            String password = args[0];
            if (password.equals(playerData.getPassword())) {
                player.sendMessage(ChatColor.GREEN + "You have been unfrozen!");
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                playerData.setFrozen(false);
            } else {
                player.sendMessage(ChatColor.RED + "The password you entered was incorrect. Try again. If you log off whilst frozen, your account will be ip-banned.");
            }
        }

        return true;
    }
}
