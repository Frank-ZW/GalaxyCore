package net.craftgalaxy.galaxycore.listener;

import com.gmail.nossr50.api.PartyAPI;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.data.PlayerData;
import net.craftgalaxy.galaxycore.data.manager.PlayerManager;
import net.craftgalaxy.galaxycore.runnable.MultiverseRunnable;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import net.craftgalaxy.galaxycore.util.bukkit.ItemUtil;
import net.craftgalaxy.galaxycore.util.bukkit.PlayerUtil;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerListeners implements Listener {

	private final CorePlugin plugin;

	// Regular expression for detecting URLs sent into the chat.
	private final Pattern URL_REGEX = Pattern.compile("^(http://www\\.|https://www\\.|http://|https://)?[a-z0-9]+([\\-.][a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(/.*)?$");

	/*
	 * Regular expression for detecting IPs sent into the chat. This was to prevent
	 * unfriendly players from leaking other player's personal information and violating
	 * their privacy on the server.
	 */
	private final Pattern IP_REGEX = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])([.,])){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

	/*
	 * List of words that will be blocked when a player tries sending. This was to prevent
	 * players from insulting others based on race, ethnicity, identity, etc. I have censored
	 * the words in the list but they were commonly used racial slurs that players often used
	 * to harass others.
	 */
	private final List<String> blacklistedWords = Arrays.asList(/* Censored words would go here */);

	/*
	 * Executor service to handle PlayerData information asynchronously and to minimize overhead on the main
	 * server thread. I used #newCachedThreadPool to reuse unused threads instead of creating a new instance
	 * of a thread and starting it manually.
	 */
	private final ExecutorService executors = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("Player data thread").build());

	/*
	 * List of players that should be pinged. If a player mentions a staff member's name in chat, the staff member's
	 * player object will be added to the List.
	 */
	private final List<Player> playersToPing = new ArrayList<>();

	public PlayerListeners(CorePlugin plugin) {
		this.plugin = plugin;
	}

	/*
	 * Shutdowns down the executors.
	 */
	public void disable() {
		this.executors.shutdown();
	}

	/*
	 * Listener for the PlayerPreLoginEvent that is sent before a player joins the server. This event is asynchronous
	 * and allows for plugins to load in player data or kick players. This method is to prevent the connection handler
	 * in the server from being overloaded when the server is starting up for the first time and is loading in all
	 * the data.
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onAsyncPreLogin(AsyncPlayerPreLoginEvent e) {
		if (!CorePlugin.SETUP) {
			e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Please wait for the server core to finish setting up the server.");
		}
	}

	/*
	 * Listener for PlayerJoinEvent, fired every time a player joins the server. If the player has played before, the
	 * method loads in their data from the SQLite database asynchronously.
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		this.executors.execute(() -> PlayerManager.getInstance().addPlayer(player));

		// Runnable to mess with one of my friends on the server; this is purely for fun and serves no
		// actual purpose.
		if ("3823d47e-4f6a-4241-b61c-baefcce4f8f2".equalsIgnoreCase(player.getUniqueId().toString())) {
			int random = ThreadLocalRandom.current().nextInt(0, 201);
			if (random == 1) {
				this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, new MultiverseRunnable(this.plugin, player), 2400L);
			}
		}
	}

	/**
	 * Listener for the PlayerQuitEvent, fired every time a player quits the server. The method calls the
	 * {@see PlayerManager#removePlayer} method which saves the player data to the database.
	 *
	 * @param e The synchronous PlayerQuitEvent fired when a player leaves the server.
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		this.executors.execute(() -> PlayerManager.getInstance().removePlayer(e.getPlayer()));
	}

	/*
	 * This method is run every time a player moves. The method is used to check whether or not the
	 * player has passed the Anti-Bot check. If the player's location travelled from is the player's
	 * login starting location and the player has not passed the Anti-Bot check, this method sets the
	 * value to true.
	 * This method also prevents players from moving if they are frozen.
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Location to = e.getTo();
		Location from = e.getFrom();
		Player player = e.getPlayer();
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null || to == null) {
			return;
		}

		if (!playerData.getStartLocation().equals(to) && playerData.getStartLocation().equals(from) && playerData.isFailedAntiBot()) {
			playerData.setPassedAntiBot(true);
			return;
		}

		if (playerData.isFrozen()) {
			if (!to.getBlock().equals(from.getBlock())) {
				e.setCancelled(true);
			}
		}
	}

	/*
	 * This method listens for inventory close actions. If the player is frozen and has the frozen inventory open on their screen and
	 * tries to close out, we want the plugin to reopen the inventory.
	 */
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData((Player) e.getPlayer());
		if (playerData != null && playerData.isFrozen() && e.getView().getTitle().equals(ChatColor.RED + "You have been frozen")) {
			this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
				Inventory freezeInventory = this.plugin.getServer().createInventory(null, 9, ChatColor.RED + "You have been frozen");
				freezeInventory.setItem(4, ItemUtil.createItemStack(Material.PAPER, ChatColor.BLUE + "What to do", 1, ChatColor.GRAY + "You have 5 minutes to", ChatColor.GRAY + "join the CraftGalaxy Discord", ChatColor.GRAY + "and screenshare with a staff member."));
				playerData.getPlayer().openInventory(freezeInventory);
			}, 1L);
		}
	}

	/*
	 * This method listens for inventory click actions or mouse clicks in the inventory menu screen. If the player is frozen
	 * and clicks on an item in the inventory, we want that to be cancelled.
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData((Player) e.getWhoClicked());
		if (playerData != null && playerData.isFrozen() && e.getView().getTitle().equals(ChatColor.RED + "You have been frozen")) {
			e.setCancelled(true);
		}
	}

	/*
	 * Code that handles pre-chat logic. If the global chat has been muted, then the event should be cancelled. If
	 * the chat has been slowed down and the player still has a cooldown, the event should be cancelled.
	 */
	private boolean handlePreChatEvent(Player player, AsyncChatHookEvent e) {
		if (PlayerManager.getInstance().isChatSilenced() && !player.hasPermission(CorePermissions.SILENCE_CHAT_PERMISSION)) {
			player.sendMessage(StringUtil.PREFIX + ChatColor.RED + " You cannot talk since the chat has been muted.");
			e.setCancelled(true);
			return true;
		}

		if (PlayerManager.getInstance().isChatSlowed() && PlayerManager.getInstance().isChatCooldown(player.getUniqueId())) {
			long secondsLeft = PlayerManager.getInstance().getChatCooldown(player.getUniqueId());
			if (secondsLeft > 0) {
				player.sendMessage(String.format(StringUtil.CHAT_IS_SLOWED, secondsLeft));
				e.setCancelled(true);
				return true;
			}
		}

		return false;
	}

	/*
	 * Method to listen for player chat events using the Towny chat event. Using Towny's chat event allows us to
	 * check which channel the chat was sent to: global chat, alliance chat, nation chat, town chat, etc. Only specific
	 * aspects of the chat filter applies to each chat type.
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerChat(AsyncChatHookEvent e) {
		Player player = e.getPlayer();
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null || e.isCancelled()) {
			return;
		}

		// First checks to see if the player has passed the Anti-Bot check and does not have permission to bypass the check
		if (playerData.isFailedAntiBot() && !player.hasPermission(CorePermissions.BOT_BYPASS_PERMISSION)) {
			player.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " You must move first before you can chat.");
			e.setCancelled(true);
			return;
		}

		// If the message hasn't been cancelled, we want to continue with the chat filter.
		if (!this.handlePreChatEvent(player, e)) {
			FilterType filterType = FilterType.PASS;
			Channel channel = e.getChannel();
			String message = e.getMessage().toLowerCase();

			// Removes common character substitutions to bypass chat filter words. For instance, if the player tries sending
			// H3LL0 Ml7, the chat filter will recognize it as "Hello MIT"
			String checkMessage = message
					.replace("3", "e")
					.replace("1", "i")
					.replace("!", "i")
					.replace("@", "a")
					.replace("7", "t")
					.replace("0", "o")
					.replace("5", "s")
					.replace("8", "b")
					.replace("l", "i")
					.replaceAll("\\p{Punct}|\\d", "")
					.trim();
			String[] words = checkMessage.split(" ");

			// Checks each individual word if it is a thread. If it is, then the filter type should be sent to silent,
			// where the message is silently cancelled and the player is NOT warned. This is to prevent players from bypassing
			// the chat filter.
			for (String word : words) {
				if (word.equalsIgnoreCase("kys") || word.equalsIgnoreCase("dox") || word.equalsIgnoreCase("ddos") || word.equalsIgnoreCase("spic") || word.equalsIgnoreCase("swastika") || word.equalsIgnoreCase("swastikas") || word.equalsIgnoreCase("hitler") || word.equalsIgnoreCase("porn") || word.equalsIgnoreCase("cum") || word.equalsIgnoreCase("sex") || word.equalsIgnoreCase("pornhub") || word.equalsIgnoreCase("xvideos") || word.equalsIgnoreCase("fag")) {
					filterType = FilterType.SILENT;
					break;
				}

				/*
				 * One of the rules on the server is to speak only English in the general chat for everybody to understand. Foreign languages are permitted in
				 * every other chat channel. This filter checks to see if the player is in the global chat and is not in a group (party) with other players and
				 * the common Spanish words (Spanish was by far the biggest issue in the server).
				 *
				 * If the player is speaking Spanish, the message is blocked and the player is notified.
				 */
				if (channel.getType().equals(channelTypes.GLOBAL) && PartyAPI.inParty(player)) {
					if (word.equalsIgnoreCase("la") || word.equalsIgnoreCase("vamos") || word.equalsIgnoreCase("estar") || word.equalsIgnoreCase("dos") || word.equalsIgnoreCase("estamos") || word.equalsIgnoreCase("estoy") || word.equalsIgnoreCase("estabamos") || word.equalsIgnoreCase("estuvimos") || word.equalsIgnoreCase("estariamos") || word.equalsIgnoreCase("nosotros") || word.equalsIgnoreCase("vosotros") || word.equalsIgnoreCase("usted") || word.equalsIgnoreCase("en") || word.equalsIgnoreCase("que") || word.equalsIgnoreCase("solamente")) {
						filterType = FilterType.NOTIFY;
						break;
					}
				}
			}

			/*
			 * Checks if the player has mentioned a staff member's name and pings the player appropriately.
			 */
			if (channel.getType() == channelTypes.GLOBAL) {
				String[] playerCheck = message.trim().split(" ");
				for (String word : playerCheck) {
					Player target = this.plugin.getServer().getPlayerExact(word);
					if (target != null) {
						PlayerData targetData = PlayerManager.getInstance().getPlayerData(target);
						if (targetData != null && target.hasPermission(CorePermissions.PINGS_PERMISSION) && targetData.isReceivePings()) {
							this.playersToPing.add(target);
						}
					}
				}
			}

			/*
			 * Regular expression check; if an IP was sent in chat or a website was sent that does not end with the server site extension,
			 * then the message is cancelled for advertisement.
			 */
			String[] regexCheck = e.getMessage().replace("(dot)", ".").replace("[dot]", ".").trim().split(" ");
			for (String word : regexCheck) {
				Matcher matcher = IP_REGEX.matcher(word);
				if (matcher.matches()) {
					filterType = FilterType.SILENT;
					break;
				}

				matcher = URL_REGEX.matcher(word);
				if (matcher.matches() && !word.contains("craftgalaxy.net") && !word.contains("discord")) {
					filterType = FilterType.NOTIFY;
					break;
				}
			}

			/*
			 * Uses a stream to filter through and check if a message contains a racial slur, discriminatory language, and
			 * other offensive languages. This is different in that it checks if a message contains, rather than equals, a
			 * specific word. Used for people who pad offensive language with extra characters to bypass the filter.
			 */
			Stream<String> filterStream = this.blacklistedWords.stream().map(s -> s.replaceAll(" ", ""));
			Set<String> wordSet = filterStream.filter(checkMessage::contains).collect(Collectors.toSet());
			if (!wordSet.isEmpty()) {
				filterType = FilterType.SILENT;
			}

			/*
			 * Manages the prefix of the player. If the player is frozen, a [Frozen] prefix will be added to the front of the
			 * player's chat format.
			 */
			String format = e.getFormat();
			if (playerData.isFrozen()) {
				format = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Frozen" + ChatColor.DARK_GRAY + "] " + format;
				if (player.hasPermission(CorePermissions.AUTHENTICATE_PERMISSION)) {
					e.setCancelled(true);
					this.playersToPing.clear();
					player.sendMessage("");
					player.sendMessage(String.format(format, player.getDisplayName(), e.getMessage()));
					player.sendMessage("");
					return;
				}

				e.setFormat(format);
			}

			/*
			 * If the player has not passed the chat filter and does not have permission to bypass the chat filter, then
			 * their message will be cancelled and depending on the filter type (silent or notify), the plugin will either
			 * alert or silently cancel the message.
			 */
			if (!filterType.isPassed()) {
				if (!player.hasPermission(CorePermissions.CHAT_FILTER_BYPASS)) {
					e.setCancelled(true);
					this.playersToPing.clear();
					if (filterType.isNotify()) {
						// Sends an interactive message to the player; they can click on "Discord" to join our Discord, which is
						// where we handle server-related announcements, updates, development, issues, etc.
						BaseComponent[] filterComponent = new ComponentBuilder(ChatColor.RED.asBungee() + "Your message was blocked because it breaks the ")
								.append("Craft")
								.color(ChatColor.GREEN.asBungee())
								.append("Galaxy")
								.color(ChatColor.BLUE.asBungee())
								.append(" community guidelines. Continued attempts to break the rules will be met with a punishment. Please review our rules on our ")
								.color(ChatColor.RED.asBungee())
								.append("Discord")
								.color(ChatColor.BLUE.asBungee())
								.event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/TKYXzBZ"))
								.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN.asBungee() + "Join the Discord!")))
								.append(". If you believe this is a mistake, contact a staff member or file a ticket on the Discord.")
								.color(ChatColor.RED.asBungee())
								.event((ClickEvent) null)
								.event((HoverEvent) null)
								.create();
						player.sendMessage("");
						player.spigot().sendMessage(filterComponent);
						player.sendMessage("");
					} else {
						player.sendMessage(String.format(format, player.getDisplayName(), e.getMessage()));
					}

					/*
					 * If the player has permission to bypass the chat filter, this will notify them that their
					 * message would have been filtered.
					 */
					format = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Filtered" + ChatColor.DARK_GRAY + "] " + format;
					PlayerUtil.messagePlayers("", CorePermissions.CHAT_FILTER_NOTIFICATION);
					PlayerUtil.messagePlayers(String.format(format, player.getDisplayName(), e.getMessage()), CorePermissions.CHAT_FILTER_NOTIFICATION);
					PlayerUtil.messagePlayers("", CorePermissions.CHAT_FILTER_NOTIFICATION);
					this.plugin.getServer().getConsoleSender().sendMessage(String.format(format, player.getDisplayName(), e.getMessage()));
					e.setFormat(format);
					return;
				}

				player.sendMessage(ChatColor.RED + "That would have been filtered.");
			}

			/*
			 * Checks if the list of players that should be pinged is empty. If it is not empty, a
			 * sound will be played to alert the player that their name has been mentioned in chat.
			 */
			if (!this.playersToPing.isEmpty()) {
				for (Player target : this.playersToPing) {
					target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.875F, 0.875F);
				}

				this.playersToPing.clear();
			}

			/*
			 * If the server chat has been slowed down (has a cooldown enabled), this method will append a
			 * cooldown to the player.
			 */
			if (PlayerManager.getInstance().isChatSlowed() && !player.hasPermission(CorePermissions.SLOW_CHAT_PERMISSION) && channel.getType() == channelTypes.GLOBAL) {
				PlayerManager.getInstance().addChatCooldown(player.getUniqueId());
			}

			e.setFormat(format);
		}
	}

	/*
	 * Checks for players running permissions-based commands and whether they have permission.
	 * Staff members can run:
	 *  - World Edit commands, prefix: //
	 *  - banip, ban-ip, blacklist commands
	 * Founders (owners) can run:
	 *  - Permission commands, prefix: /luckperms, /lp, /op, /deop
	 *  - Logger commands, prefix: /coreprotect, /co
	 *  - Stop the server, prefix: /stop
	 *  - Disable this plugin, prefix: /plugman unload GalaxyCore
	 * If a player attempts to run any of those commands and does not have permission, the plugin will cancel the
	 * command and alert staff members. This is to prevent players who hack into the server and give themselves permission.
	 * This method also checks if the player is frozen, and if they are and the command they are running is not the authentication
	 * command, then the plugin cancels the action.
	 */
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		if (!e.isCancelled()) {
			Player player = e.getPlayer();
			PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
			if (playerData == null) {
				return;
			}

			String[] message = e.getMessage().trim().split(" ");
			String commandAlert = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Alert" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY + "%s" + ChatColor.RESET + ChatColor.DARK_GRAY + ChatColor.BOLD + " » " + ChatColor.RESET + ChatColor.GRAY + "%s";
			if (!PlayerManager.getInstance().getManagerUUIDs().contains(player.getUniqueId().toString()) && ("/luckperms".equalsIgnoreCase(message[0]) || "/lp".equalsIgnoreCase(message[0]) || "/op".equalsIgnoreCase(message[0]) || "/deop".equalsIgnoreCase(message[0]) || "/stop".equalsIgnoreCase(message[0]) || (message.length == 3 && "/plugman".equalsIgnoreCase(message[0]) && "unload".equalsIgnoreCase(message[1]) && "GalaxyCore".equalsIgnoreCase(message[2])) || (message.length > 1 && ("/coreprotect".equalsIgnoreCase(message[0]) || "/co".equalsIgnoreCase(message[0])) && "purge".equalsIgnoreCase(message[1])))) {
				e.setCancelled(true);
				PlayerUtil.messagePlayers("", CorePermissions.MANAGERS_PERMISSION);
				PlayerUtil.messagePlayers(String.format(commandAlert, player.getDisplayName(), e.getMessage()), CorePermissions.MANAGERS_PERMISSION);
				PlayerUtil.messagePlayers("", CorePermissions.MANAGERS_PERMISSION);
				this.plugin.getServer().getConsoleSender().sendMessage("");
				this.plugin.getServer().getConsoleSender().sendMessage(String.format(commandAlert, player.getDisplayName(), e.getMessage()));
				this.plugin.getServer().getConsoleSender().sendMessage("");
			} else if (!PlayerManager.getInstance().getStaffUUIDs().contains(player.getUniqueId().toString()) && !PlayerManager.getInstance().getManagerUUIDs().contains(player.getUniqueId().toString()) && (message[0].startsWith("//") || "banip".equalsIgnoreCase(message[0]) || "ban-ip".equalsIgnoreCase(message[0]) || "blacklist".equalsIgnoreCase(message[0]))) {
				e.setCancelled(true);
				PlayerUtil.messagePlayers("", CorePermissions.STAFF_TEAM_PERMISSION);
				PlayerUtil.messagePlayers(String.format(commandAlert, player.getDisplayName(), e.getMessage()), CorePermissions.STAFF_TEAM_PERMISSION);
				PlayerUtil.messagePlayers("", CorePermissions.STAFF_TEAM_PERMISSION);
				this.plugin.getServer().getConsoleSender().sendMessage("");
				this.plugin.getServer().getConsoleSender().sendMessage(String.format(commandAlert, player.getDisplayName(), e.getMessage()));
				this.plugin.getServer().getConsoleSender().sendMessage("");
			}

			if (playerData.isFrozen() && !"/authenticate".equalsIgnoreCase(message[0])) {
				e.setCancelled(true);
				player.sendMessage(ChatColor.RED + "You cannot run that command whilst frozen.");
			}
		}
	}

	/*
	 * This method checks the preconditions for the newbie protection aspect of the plugin. Similar to the pre-chat
	 * method, we want to check if the plugin should ignore the attack before continuing on the calculations for
	 * player protection. We want the plugin to ignore attacks if:
	 *  - If either of the players is in a town with pvp (player vs. player) disabled
	 *  - If the player is not in wilderness
	 *  - If the attack has already been cancelled
	 *  - If either the attacker or the victim is not a player
	 *  - If either player does not have permission to attack the other player
	 */
	private boolean handlePreEntityDamageEvent(Entity target, Entity attacker, EntityDamageByEntityEvent e) {
		// Checks that both the attacker and the target are players and the attack hasn't already been cancelled by another plugin
		if (!(target instanceof Player) || !(attacker instanceof Player) || e.isCancelled()) {
			return true;
		}

		Player targetPlayer = (Player) target;
		Player attackerPlayer = (Player) attacker;
		try {
			// Gets whether the players are standing in town claims and whether the town has pvp enabled
			TownBlock targetTownBlock = WorldCoord.parseWorldCoord(targetPlayer.getLocation()).getTownBlock();
			TownBlock damagerTownBlock = WorldCoord.parseWorldCoord(attackerPlayer.getLocation()).getTownBlock();
			if (targetTownBlock != null && damagerTownBlock != null && targetTownBlock.getPermissions().pvp && damagerTownBlock.getPermissions().pvp) {
				return false;
			}
		} catch (NotRegisteredException ignored) {}

		// Checks if either player is standing in the wilderness (no claims, no towns, hurting other players is allowed)
		// If both players are in the wilderness, then the plugin should check player protection logic
		if (!TownyAPI.getInstance().isWilderness(targetPlayer.getLocation()) && !TownyAPI.getInstance().isWilderness(attackerPlayer.getLocation())) {
			return true;
		}

		// Returns true if the attack was not allowed and false if the attack was allowed and happened
		return CombatUtil.preventDamageCall(this.plugin.getTowny(), attackerPlayer, targetPlayer);
	}

	/*
	 * Code that handles player protection logic. If either the target or the attacker is frozen, the event will be cancelled.
	 * If the attacker still has player protection enabled, the plugin cancels the attack event and their player protection forcibly
	 * removed and messages sent to both players indicating the attacker lost their player protection.
	 * If the victim still has player protection, the plugin cancels the attack event.
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDamage(EntityDamageByEntityEvent e) {
		if (!this.handlePreEntityDamageEvent(e.getEntity(), e.getDamager(), e)) {
			Player target = (Player) e.getEntity();
			Player attacker = (Player) e.getDamager();
			PlayerData targetData = PlayerManager.getInstance().getPlayerData(target);
			PlayerData attackerData = PlayerManager.getInstance().getPlayerData(attacker);
			if (targetData != null && attackerData != null) {
				if (attackerData.isFrozen()) {
					attacker.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " You cannot attack other players whilst frozen.");
					e.setCancelled(true);
					return;
				}

				if (targetData.isFrozen()) {
					attacker.sendMessage(StringUtil.PREFIX + ChatColor.GRAY + " You cannot attack " + ChatColor.WHITE + targetData.getName() + ChatColor.GRAY + " since they are frozen.");
					e.setCancelled(true);
					return;
				}

				if (attackerData.isNewbieProtection()) {
					e.setCancelled(true);
					attackerData.setNewbieProtection(false);
					attackerData.setForceLostNewbieProtection(true);
					target.sendMessage(String.format(StringUtil.PREFIX + ChatColor.WHITE + " %s" + ChatColor.GREEN + " attempted to hit you and lost their newbie protection. You can now pvp them.", attacker.getName()));
					attacker.sendMessage(String.format(StringUtil.PREFIX + ChatColor.RED + " You attempted to hit " + ChatColor.WHITE + "%s" + ChatColor.RED + " and lost your newbie protection. Other players can now damage you.", target.getName()));
				} else {
					if (targetData.isNewbieProtection()) {
						e.setCancelled(true);
						attacker.sendMessage(String.format(StringUtil.PREFIX + ChatColor.RED + " You cannot damage " + ChatColor.WHITE + "%s" + ChatColor.RED + " since they have newbie protection.", target.getName()));
						target.sendMessage(String.format(StringUtil.PREFIX + ChatColor.WHITE + " %s" + ChatColor.GREEN + " attempted to hit you.", attacker.getName()));
					}
				}
			}
		}
	}

	/*
	 * If the player is frozen, we do not want them to be able to break blocks. In theory, this shouldn't
	 * happen but it is safe to add an extra layer of precaution.
	 */
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(e.getPlayer());
		if (playerData != null && playerData.isFrozen()) {
			e.setCancelled(true);
		}
	}

	/*
	 * If the player is frozen, we do not want them to be able to place blocks. In theory, this shouldn't
	 * happen but it is safe to add an extra layer of precaution.
	 */
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(e.getPlayer());
		if (playerData != null && playerData.isFrozen()) {
			e.setCancelled(true);
		}
	}

	/*
	 * If the player is frozen, we do not want them to be able to teleport. In theory, this shouldn't
	 * happen but it is safe to add an extra layer of precaution.
	 */
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(e.getPlayer());
		if (playerData != null && playerData.isFrozen()) {
			e.setCancelled(true);
		}
	}

	/*
	 * There is a rule on the server: no farms on the server for in-game currency (iron, gold, etc.)
	 * This is to prevent players from abusing the game and gaining an infinite amount of money. As such,
	 * all currency-valued drops are automatically removed every time an entity, that isn't a player,
	 * dies.
	 */
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		if (e.getEntity() instanceof Player) {
			return;
		}

		e.getDrops().removeIf(drop -> drop.getType() == Material.GOLD_INGOT || drop.getType() == Material.IRON_INGOT || drop.getType() == Material.GOLD_NUGGET || drop.getType() == Material.GOLDEN_HELMET || drop.getType() == Material.GOLDEN_CHESTPLATE || drop.getType() == Material.GOLDEN_LEGGINGS || drop.getType() == Material.GOLDEN_BOOTS || drop.getType() == Material.IRON_CHESTPLATE || drop.getType() == Material.IRON_HELMET || drop.getType() == Material.IRON_LEGGINGS || drop.getType() == Material.IRON_BOOTS || drop.getType() == Material.IRON_NUGGET);
	}

	// Filter type - messages with filter type of pass will not be filtered, messages with filter type
	// of silent will be silently cancelled (the sender will not be notified their message was cancelled),
	// and messages with filter type of notify will be cancelled and the sender will be notified of the
	// filter.
	public enum FilterType {
		PASS,
		SILENT,
		NOTIFY;

		FilterType() {}

		public boolean isPassed() {
			return this == FilterType.PASS;
		}

		public boolean isNotify() {
			return this == FilterType.NOTIFY;
		}
	}
}
