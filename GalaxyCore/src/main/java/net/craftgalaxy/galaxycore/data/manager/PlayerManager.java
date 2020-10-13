package net.craftgalaxy.galaxycore.data.manager;

import com.google.common.collect.Sets;
import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.data.PlayerData;
import net.craftgalaxy.galaxycore.util.java.CooldownList;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PlayerManager {

    private final CorePlugin plugin;

    /*
     * A map of the players online and their associated player data information.
     */
    private final Map<UUID, PlayerData> players;

    /*
     * A list of the players with a command cooldown. This is currently not used.
     */
    private final CooldownList<UUID> commandCooldown;

    /*
     * A list of the players with a chat cooldown when slow mode is enabled. Slow mode is
     * when there is a cooldown applied to the chat (typically three seconds) which is
     * useful in preventing players from spamming the chat. The CooldownList uses an internal
     * map and arraylist.
     */
    private final CooldownList<UUID> chatCooldown;

    /*
     * The manager (owner) whitelisted unique IDs. Players on this whitelist can run any command
     * on the server, including all permission-based and server-mechanic commands. Players that attempt
     * to run commands only available to owners will alert staff members. This is to prevent players
     * from hacking the server and giving themselves permission to do anything on our server.
     */
    private final Set<String> managerUUIDs = Sets.newHashSet("06f547ac-c298-4989-a8e7-5e08f89c7e5e", "69a7da3c-5e94-4bbe-883e-61568c928d05", "3823d47e-4f6a-4241-b61c-baefcce4f8f2", "2a79d400-de6b-4d15-838d-f7236925b354", "f64f05df-ea82-4991-931b-1f0d6149fe3c");

    /*
     * The staff whitelisted unique IDs. Players on this whitelist can run specific commands that normal
     * default players cannot, such as World Edit and blacklist commands. Players that attempt
     * to run commands available to staff members and owners will alert staff. This is to prevent players
     * from hacking the server and giving themselves permission to run any command on our server.
     */
    private final Set<String> staffUUIDs = Sets.newHashSet("7bfd6626-f7af-49dc-9893-09baae474e5d", "55da912d-9d0d-4067-a0a9-f82e793ce84e", "77bfa336-23bd-49e4-bf8e-055d65bb2fb0", "2c0c8432-e000-4b13-adab-82086ed305eb", "1dd22be6-c5b5-454f-958b-733d09509c2a");

    /*
     * Boolean value for whether the global chat is muted or not. If the global chat is silenced, nobody
     * will be able to chat, unless they have the specific permission to bypass.
     */
    private boolean chatSilenced;

    /*
     * Boolean value for whether slow mode has been enabled or not. When slow mode is enabled, players that
     * do not have an active cooldown can chat. Players that have the bypass permission will be able to chat
     * normally.
     */
    private boolean chatSlowed;

    /*
     * Since we only want a single instance of PlayerManager to handle PlayerData and player unique IDs, this class
     * needs to be structured as a singleton. Another way is to have an instance of PlayerManager in CorePlugin and a
     * getter method to call the PlayerManager instance.
     */
    private static PlayerManager instance;

    public PlayerManager(CorePlugin plugin) {
        this.plugin = plugin;
        this.players = new ConcurrentHashMap<>();
        this.commandCooldown = new CooldownList<>(TimeUnit.SECONDS, 1L);
        this.chatCooldown = new CooldownList<>(TimeUnit.SECONDS, 5L);

        new Thread(() -> plugin.getServer().getOnlinePlayers().forEach(player -> this.addPlayer(player, true))).start();
    }

    public static void enable(CorePlugin plugin) {
        PlayerManager.instance = new PlayerManager(plugin);
    }

    public static void disable() {
        Thread disable = new Thread(() -> PlayerManager.instance.plugin.getServer().getOnlinePlayers().forEach(PlayerManager.instance::removePlayer));
        disable.start();
        try {
            disable.join(TimeUnit.SECONDS.toMillis(15L));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PlayerManager.instance.commandCooldown.clear();
        PlayerManager.instance.chatCooldown.clear();
        PlayerManager.instance = null;
    }

    public static PlayerManager getInstance() {
        return PlayerManager.instance;
    }

    public void addPlayer(Player player) {
        this.addPlayer(player, false);
    }

    /*
     * Called when a player joins the server. If the player has never joined the server before,
     * the #fetchPlayerData method will return null. This method must be called asynchronously
     * in order to prevent the method from blocking the main server thread.
     */
    public void addPlayer(Player player, boolean passedAntiBot) {
        PlayerData playerData = this.plugin.getDatabase().fetchPlayerData(player);
        if (playerData == null) {
            playerData = new PlayerData(player);
        }

        playerData.handleLogin();
        playerData.setPassedAntiBot(passedAntiBot);
        this.players.put(player.getUniqueId(), playerData);
    }

    /*
     * Called when a player quits the server. This method must be called asynchronously
     * in order to prevent the method from blocking the main server thread.
     */
    public void removePlayer(Player player) {
        PlayerData playerData = this.players.remove(player.getUniqueId());
        if (playerData != null) {
            playerData.handleDisconnect();
            this.plugin.getDatabase().writePlayerData(playerData);
        }
    }

    /*
     * Returns the player data information associated with the player object. Since this method
     * can be called for players offline, when their information is not stored on the server, then
     * the PlayerData returned might be null, hence the annotation.
     */
    @Nullable
    public PlayerData getPlayerData(Player player) {
        return this.players.get(player.getUniqueId());
    }

    public void addChatCooldown(UUID uuid) {
        this.chatCooldown.addCooldown(uuid);
    }

    public long getChatCooldown(UUID uuid) {
        return this.chatCooldown.getSecondsRemaining(uuid);
    }

    public boolean isChatCooldown(UUID uuid) {
        return !this.chatCooldown.isExpired(uuid);
    }

    public boolean isChatSilenced() {
        return this.chatSilenced;
    }

    public void setChatSilenced(boolean chatSilenced) {
        this.chatSilenced = chatSilenced;
    }

    public boolean isChatSlowed() {
        return this.chatSlowed;
    }

    public void setChatSlowed(boolean chatSlowed) {
        if (!chatSlowed) {
            this.chatCooldown.clear();
        }

        this.chatSlowed = chatSlowed;
    }

    public void addStaff(UUID uniqueId) {
        this.staffUUIDs.add(uniqueId.toString());
    }

    public void removeStaff(UUID uniqueId) {
        this.staffUUIDs.remove(uniqueId.toString());
    }

    public void removeManager(UUID uniqueId) {
        this.managerUUIDs.remove(uniqueId.toString());
    }

    public Set<String> getManagerUUIDs() {
        return this.managerUUIDs;
    }

    public Set<String> getStaffUUIDs() {
        return this.staffUUIDs;
    }
}
