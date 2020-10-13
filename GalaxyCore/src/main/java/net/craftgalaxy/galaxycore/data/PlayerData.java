package net.craftgalaxy.galaxycore.data;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import net.craftgalaxy.galaxycore.util.bukkit.ItemUtil;
import net.craftgalaxy.galaxycore.util.bukkit.PlayerUtil;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerData {

    private final CorePlugin plugin;

    /**
     * The player object associated with the PlayerData instance.
     */
    private final Player player;

    /**
     * The player's in-game username.
     */
    private final String name;

    /**
     * The unique ID associated with the player.
     * Each player receives their own unique UUID.
     */
    private final UUID uniqueId;

    /**
     * The player's IP-address from which they have logged onto the server with.
     */
    private String ipAddress;

    /**
     * The player's password for two-factor authentication.
     * This only applies to staff members to prevent players from hacking into a staff-member's
     * account and destroying the server or UUID spoofing whereby players log into a staff-member's
     * account using a different unique ID and simultaneously gaining access to the player's admin
     * privileges.
     */
    private String password;

    /**
     * This is the scheduler for handling newbie protection. Newbie protection is a player of protection
     * to players that have recently joined the server and protects them from dying in-game. When the
     * scheduler is null, the player does not have newbie protection enabled.
     */
    private BukkitTask newbieProtectionScheduler;

    /**
     * The location the player first logs on to; this is useful for preventing bots from joining the server
     * and spamming the chat to crash the server.
     */
    private Location startLocation;

    /**
     * The player's past IP-addresses. The data type is a LinkedList due to its efficiency getting the very
     * last element and stores every single IP-address the player has logged on from - useful for tracing
     * back IPs.
     */
    private Deque<String> ipAddresses = new LinkedList<>();

    /**
     * Sets whether or not the player should receive anticheat alerts to detect hackers - currently not in
     * use and is a work in progress.
     */
    private boolean receiveAlerts;

    /**
     * Sets whether or not the player should receive a verbal ping when their name is mentioned in chat.
     */
    private boolean receivePings;

    /**
     * Sets whether or not the player is in admin chat.
     */
    private boolean inAdminChat;

    /**
     * Sets whether or not the player has passed the bot check. Players that join the server must first move
     * before they are allowed to chat.
     */
    private boolean passedAntiBot;

    /**
     * Sets whether or not the player is frozen. Players are frozen when they are suspected of cheating and
     * are unable to do anything until they are unfrozen.
     */
    private boolean frozen;

    /**
     * Sets whether or not the player has newbie protection. Players that are under player protection cannot
     * receive damage until they harm another player.
     */
    private boolean newbieProtection;

    /**
     * Sets whether or not the player has forcefully lost their player protection. This only occurs when a
     * player has player protection and harms another player.
     */
    private boolean forceLostNewbieProtection;

    public PlayerData(Player player) {
        this.plugin = CorePlugin.getInstance();
        this.player = player;
        this.name = player.getName();
        this.uniqueId = player.getUniqueId();
        this.receivePings = player.hasPermission(CorePermissions.PINGS_PERMISSION);
        this.receiveAlerts = player.hasPermission(CorePermissions.ALERTS_PERMISSION);
        this.startLocation = player.getLocation();
    }

    /**
     * This handles the in-game logic for players joining the server. For staff members specifically, if the
     * player has logged on from a new, unregistered IP-address, the player is automatically frozen and must
     * authenticate their connection by entering their password.
     * This method also calculates the amount of time remaining for a player's newbie protection and starts a
     * scheduler in the background if the player is still protected.
     */
    public void handleLogin() {
        boolean shouldFreeze = false;
        this.startLocation = this.player.getLocation();
        InetSocketAddress socketAddress = this.player.getAddress();
        if (socketAddress == null) {
            this.player.kickPlayer(StringUtil.ERROR_LOADING_DATA);
            return;
        }

        this.ipAddress = socketAddress.getAddress().getHostAddress();
        if (!this.ipAddresses.isEmpty() && !this.ipAddresses.contains(this.ipAddress)) {
            shouldFreeze = true;
        }

        long protectionRemaining = 259200L - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.player.getFirstPlayed());
        if (protectionRemaining > 0 && !this.forceLostNewbieProtection) {
            this.player.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " You have " + ChatColor.WHITE + protectionRemaining + " second(s)" + ChatColor.GRAY + " remaining before your newbie protection wears out.");
            this.newbieProtection = true;
            this.newbieProtectionScheduler = this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () -> {
                this.newbieProtection = false;
                this.player.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " Your newbie protection has expired. Players can now damage you.");
            }, 20 * protectionRemaining);
        } else {
            this.newbieProtection = false;
            this.newbieProtectionScheduler = null;
        }

        // This handles the freeze mechanics and is done as asynchronously as possible to ensure
        // the server's main thread's performance is not affected by the plugin.
        if (shouldFreeze) {
            if (!this.player.hasPermission(CorePermissions.AUTHENTICATE_PERMISSION)) {
                return;
            }

            this.frozen = true;
            this.runOnMainThread(() -> this.player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0)));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.375F, 0.375F);
            PlayerUtil.sendTitleSubtitlePackets(this.player, ChatColor.RED.asBungee() + "Detected new IP Login...", ChatColor.RED.asBungee() + "Type /authenticate <password> to confirm your connection.", 20, 20, 20);
        }

        if (this.password == null && this.player.hasPermission(CorePermissions.AUTHENTICATE_PERMISSION)) {
            if (this.frozen) {
                this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () -> PlayerUtil.sendTitleSubtitlePackets(this.player, ChatColor.RED.asBungee() + "You do not have a password", ChatColor.BLUE.asBungee() + "Type /password <password> to set a password.", 20, 40, 20), 60L);
            } else {
                PlayerUtil.sendTitleSubtitlePackets(this.player, ChatColor.RED.asBungee() + "You do not have a password", ChatColor.BLUE.asBungee() + "Type /password <password> to set a password.", 20, 40, 20);
            }
        }
    }

    /**
     * Certain methods can only be run through the server's main thread, which is what this method
     * does.
     * @param runnable The runnable parameter to be run on the main thread.
     */
    public void runOnMainThread(Runnable runnable) {
        this.plugin.getServer().getScheduler().runTask(this.plugin, runnable);
    }

    /**
     * This handles player disconnection. If the player is frozen, either from logging on through a new
     * IP-address or is frozen by a staff-member, the player is instantly banned.
     * If the player is not frozen, the current IP-address is added. This is to prevent frozen players from
     * joining the server, logging off, and joining back to bypass the 2FA check.
     */
    public void handleDisconnect() {
        if (this.frozen) {
            this.runOnMainThread(() -> {
                if (this.player.hasPermission(CorePermissions.AUTHENTICATE_PERMISSION)) {
                    this.player.removePotionEffect(PotionEffectType.BLINDNESS);
                }

                this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "ban " + this.player.getName() + " " + ChatColor.GRAY + "Quitting the game whilst frozen.");
                this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "banip " + this.player.getName() + " " + ChatColor.GRAY + "Quitting the game whilst frozen.");
            });
        } else {
            this.ipAddresses.addLast(this.ipAddress);
        }
    }

    /**
     * This handles the logic for freezing a player.
     * @param sender The entity or object freezing the player.
     */
    public void freezePlayer(CommandSender sender) {
        if (this.frozen) {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " This player is already frozen.");
            return;
        }

        if (this.player.hasPermission(CorePermissions.AUTHENTICATE_PERMISSION)) {
            if (!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(ChatColor.RED + "This player can only be frozen through console.");
                return;
            }

            this.runOnMainThread(() -> this.player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0)));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.375F, 0.375F);
            PlayerUtil.sendTitleSubtitlePackets(this.player, ChatColor.RED.asBungee() + "You have been frozen.", ChatColor.RED.asBungee() + "Type /authenticate <password> to unfreeze yourself.", 20, 20, 20);
        } else {
            Inventory freezeInventory = this.plugin.getServer().createInventory(null, 9, ChatColor.RED + "You have been frozen");
            freezeInventory.setItem(4, ItemUtil.createItemStack(Material.PAPER, ChatColor.BLUE + "What to do", 1, ChatColor.GRAY + "You have 5 minutes to", ChatColor.GRAY + "join the CraftGalaxy Discord", ChatColor.GRAY + "and screenshare with a staff member."));
            this.player.openInventory(freezeInventory);
        }

        sender.sendMessage(StringUtil.PREFIX + " " + ChatColor.WHITE + this.player.getName() + ChatColor.GREEN + " has been frozen.");
        this.frozen = true;
    }

    /**
     * This handles the logic for unfreezing a player. The method is separate from the {#link freezePlayer}
     * since there are two separate commands controlling whether a player is frozen or not.
     * @param sender The entity or object freezing the player.
     */
    public void unfreezePlayer(CommandSender sender) {
        if (!this.frozen) {
            sender.sendMessage(StringUtil.PREFIX + ChatColor.RED + " This player is already unfrozen.");
            return;
        }

        if (this.player.hasPermission(CorePermissions.AUTHENTICATE_PERMISSION)) {
            if (!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(ChatColor.RED + "This player can only be unfrozen through console.");
                return;
            }

            this.runOnMainThread(() -> {
                this.player.removePotionEffect(PotionEffectType.BLINDNESS);
                this.player.sendMessage(ChatColor.GREEN + "You have been unfrozen.");
            });
        } else {
            this.runOnMainThread(() -> {
                this.player.closeInventory();
                this.player.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " You have been unfrozen.");
            });
        }

        sender.sendMessage(StringUtil.PREFIX + " " + ChatColor.WHITE + this.player.getName() + ChatColor.GREEN + " has been unfrozen.");
        this.frozen = false;
    }

    /**
     * Returns the player object associated with the player data instance.
     *
     * @return The player object of the player data.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return The unique ID of the player.
     */
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    /**
     * @return The name of the player.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return True if the player is frozen and false otherwise.
     */
    public boolean isFrozen() {
        return this.frozen;
    }

    /**
     * @param frozen Whether the player is frozen or not.
     */
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    /**
     * @return The Location object of the player's start location when they joined the server.
     */
    public Location getStartLocation() {
        return this.startLocation;
    }

    /**
     * @return True if the player failed the anti-bot check and false if the player has passed.
     */
    public boolean isFailedAntiBot() {
        return !this.passedAntiBot;
    }

    /**
     * @param passedAntiBot Set whether or not the player has passed the anti-bot check.
     */
    public void setPassedAntiBot(boolean passedAntiBot) {
        this.passedAntiBot = passedAntiBot;
    }

    /**
     * @return True if the player is in admin chat and false if otherwise.
     */
    public boolean isInAdminChat() {
        return this.inAdminChat;
    }

    /**
     * @param inAdminChat Set whether or not the player is in admin chat.
     */
    public void setInAdminChat(boolean inAdminChat) {
        this.inAdminChat = inAdminChat;
    }

    /**
     * @return True if the player should receive in-game pings and notifications and false otherwise.
     */
    public boolean isReceivePings() {
        return this.receivePings;
    }

    /**
     * @param receivePings Set whether or not the player should receive in-game pings.
     */
    public void setReceivePings(boolean receivePings) {
        this.receivePings = receivePings;
    }

    /**
     * This is currently not used.
     *
     * @return True if the player should receive anticheat alerts and false otherwise.
     */
    public boolean isReceiveAlerts() {
        return this.receiveAlerts;
    }

    /**
     * This is currently not used.
     *
     * @param receiveAlerts Set whether or not the player should receive anticheat alerts.
     */
    public void setReceiveAlerts(boolean receiveAlerts) {
        this.receiveAlerts = receiveAlerts;
    }

    /**
     * @return True if the player still has player protection and false otherwise.
     */
    public boolean isNewbieProtection() {
        return this.newbieProtection;
    }

    /**
     * Set to true if the player still has newbie protection and false otherwise. This method will also recalculate
     * the amount of time remaining before their player protection expires if the new value is set to true.
     *
     * @param newbieProtection Set whether or not the player has player protection.
     */
    public void setNewbieProtection(boolean newbieProtection) {
        this.newbieProtection = newbieProtection;
        if (this.newbieProtection) {
            long protectionRemaining = 259200L - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.player.getFirstPlayed());
            if (protectionRemaining > 0 && !this.forceLostNewbieProtection) {
                this.player.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " You have " + ChatColor.WHITE + protectionRemaining + " second(s)" + ChatColor.GRAY + " remaining before your newbie protection wears out.");
                this.newbieProtection = true;
                this.newbieProtectionScheduler = this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () -> {
                    this.newbieProtection = false;
                    this.player.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " Your newbie protection has expired. Players can now damage you.");
                }, 20 * protectionRemaining);
            }
        }
    }

    /**
     * A player has forcibly lost their player protection if they have damaged another player whilst under player protection.
     *
     * @return True if the player has forcibly lost their player protection and false otherwise.
     */
    public boolean isForceLostNewbieProtection() {
        return this.forceLostNewbieProtection;
    }

    /**
     * Set to true if the player has damaged another player whilst under player protection and false otherwise. This
     * method will also cancel the player protection scheduler if there is one active.
     *
     * @param forceLostNewbieProtection Sets whether or not the player has forcibly lost their player protection.
     */
    public void setForceLostNewbieProtection(boolean forceLostNewbieProtection) {
        this.forceLostNewbieProtection = forceLostNewbieProtection;
        if (this.forceLostNewbieProtection) {
            this.newbieProtection = false;
            if (this.newbieProtectionScheduler != null) {
                this.newbieProtectionScheduler.cancel();
                this.newbieProtectionScheduler = null;
            }
        }
    }

    /**
     * Returns the player's password. If the player is not a staff member, this will return null. If the
     * player was once a staff member, this will return their original password if they had one.
     *
     * @return The player's password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @param password Sets the new password for the player.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return The past IP-addresses the player has logged on from.
     */
    public Deque<String> getIpAddresses() {
        return this.ipAddresses;
    }

    /**
     * @param ipAddresses The new IP-addresses for the player.
     */
    public void setIpAddresses(Deque<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof PlayerData)) {
            return false;
        }

        PlayerData playerData = (PlayerData) obj;
        return this.name.equals(playerData.name) &&
                this.uniqueId.equals(playerData.uniqueId) &&
                this.password.equalsIgnoreCase(playerData.password) &&
                this.ipAddresses.equals(playerData.ipAddresses) &&
                this.ipAddress.equals(playerData.ipAddress) &&
                this.startLocation.equals(playerData.getStartLocation()) &&
                this.receiveAlerts == playerData.receiveAlerts &&
                this.receivePings == playerData.receivePings &&
                this.inAdminChat == playerData.inAdminChat &&
                this.passedAntiBot == playerData.passedAntiBot &&
                this.frozen == playerData.frozen &&
                this.newbieProtection == playerData.newbieProtection &&
                this.forceLostNewbieProtection == playerData.forceLostNewbieProtection &&
                this.newbieProtectionScheduler.equals(playerData.newbieProtectionScheduler);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashcode = 1;
        hashcode = prime * hashcode + this.name.hashCode();
        hashcode = prime * hashcode + this.uniqueId.hashCode();
        hashcode = prime * hashcode + (this.password == null ? 0 : this.password.hashCode());
        hashcode = prime * hashcode + this.ipAddress.hashCode();
        hashcode = prime * hashcode + this.ipAddresses.hashCode();
        hashcode = prime * hashcode + this.startLocation.hashCode();
        hashcode = prime * hashcode + (this.receiveAlerts ? 1 : 0);
        hashcode = prime * hashcode + (this.receivePings ? 1 : 0);
        hashcode = prime * hashcode + (this.inAdminChat ? 1 : 0);
        hashcode = prime * hashcode + (this.passedAntiBot ? 1 : 0);
        hashcode = prime * hashcode + (this.frozen ? 1 : 0);
        hashcode = prime * hashcode + (this.newbieProtection ? 1 : 0);
        hashcode = prime * hashcode + (this.forceLostNewbieProtection ? 1 : 0);
        hashcode = prime * hashcode + (this.newbieProtectionScheduler == null ? 0 : this.newbieProtectionScheduler.hashCode());
        return hashcode;
    }
}
