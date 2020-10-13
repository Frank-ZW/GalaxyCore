package net.craftgalaxy.galaxycore;

import com.palmergames.bukkit.towny.Towny;
import net.craftgalaxy.galaxycore.command.*;
import net.craftgalaxy.galaxycore.command.passwords.AuthenticateCommand;
import net.craftgalaxy.galaxycore.command.passwords.SetPasswordCommand;
import net.craftgalaxy.galaxycore.command.troll.TrollCommand;
import net.craftgalaxy.galaxycore.data.manager.PlayerManager;
import net.craftgalaxy.galaxycore.database.Database;
import net.craftgalaxy.galaxycore.gui.manager.GuiManager;
import net.craftgalaxy.galaxycore.listener.GuiListeners;
import net.craftgalaxy.galaxycore.listener.PlayerListeners;
import net.craftgalaxy.galaxycore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public final class CorePlugin extends JavaPlugin {

    public static boolean SETUP;

    // The singleton instance of the GalaxyCore plugin.
    private static CorePlugin instance;

    // The listener class for player actions and events.
    private PlayerListeners playerListeners;

    // The listener class for GUI events.
    private GuiListeners guiListeners;

    // The plugin's embedded database.
    private Database database;

    // The Towny dependency object.
    private Towny towny;

    // All the commands GalaxyCore adds to the server.
    private final List<Class<? extends Command>> commands = Arrays.asList(
            FreezeCommand.class,
            UnfreezeCommand.class,
            AdminChatCommand.class,
            PingsCommand.class,
            AlertsCommand.class,
            AuthenticateCommand.class,
            SetPasswordCommand.class,
            MuteChatCommand.class,
            SlowChatCommand.class,
            AltsCommand.class,
            TrollCommand.class,
            ClearChatCommand.class,
            MinersCommand.class,
            ReportCommand.class,
            ShutdownCommand.class,
            GalaxyAdminCommand.class,
            AddressesCommand.class
    );

    @Override
    public void onEnable() {
        // This method is called when the plugin starts up. In the plugin.yml file, I have listed the dependencies the plugin
        // must have in order to run, meaning Minecraft will load this plugin after its dependencies have loaded.
        SETUP = false;
        instance = this;

        /*
         * Determines whether the plugin should disable itself after starting up. This
         * should be set to true if a dependency has not been found. The plugin disables
         * itself after starting up to ensure all the necessary files are loaded in.
         */
        boolean shutdown = false;

        /*
         * Checks to see if the plugin's main data folder (the folder that holds all the
         * plugin's information) already exists, and creates one if it does not exist.
         */
        if (!this.getDataFolder().exists()) {
            if (!this.getDataFolder().mkdir()) {
                this.getLogger().log(Level.SEVERE, "Failed to create main plugin directory.");
                this.getServer().shutdown();
                return;
            }
        }

        // Creates database object and loads in the database.
        this.database = new Database(this);
        this.database.loadDatabase();
        try {
            this.towny = (Towny) this.getServer().getPluginManager().getPlugin("Towny");
        } catch (Throwable t) {
            this.getLogger().log(Level.SEVERE, "Failed to retrieve Towny dependency", t);
            shutdown = true;
        }

        // Enables player manager and gui manager class
        // Creates single instance
        GuiManager.enable();
        PlayerManager.enable(this);

        // Listens for player and gui events
        this.playerListeners = new PlayerListeners(this);
        this.getServer().getPluginManager().registerEvents(this.playerListeners, this);
        this.guiListeners = new GuiListeners();
        this.getServer().getPluginManager().registerEvents(this.guiListeners, this);
        this.registerCommands();

        SETUP = true;
        if (shutdown) {
            this.getLogger().info(StringUtil.PREFIX + ChatColor.RED + " Disabled core plugin due to incomplete startup.");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // This method is called when the plugin is disabled, either when the server stops or when the plugin is manually disabled.
        SETUP = false;
        this.playerListeners.disable();

        /*
         * Unregisters the gui and player listeners to prevent
         * memory leaks
         */
        HandlerList.unregisterAll(this.guiListeners);
        HandlerList.unregisterAll(this.playerListeners);
        HandlerList.unregisterAll(this);
        PlayerManager.disable();
        GuiManager.disable();
        this.database.disable();
        this.getServer().getScheduler().cancelTasks(this);
        instance = null;
    }

    /**
     * This method registers all the commands in the plugin and adds it to the server's CommandMap, which allows the
     * commands to be tabbed and automatically filled in-game. Much neater and efficient than using Spigot's built-in
     * CommandExecutor class.
     */
    public void registerCommands() {
        for (Class<? extends Command> clazz : this.commands) {
            try {
                Command command = clazz.getConstructor(CorePlugin.class).newInstance(this);
                this.registerCommand(command, this.getName());
            } catch (ReflectiveOperationException e) {
                this.getLogger().log(Level.WARNING, "Failed to instantiate " + clazz.getSimpleName() + " command", e);
            }
        }
    }

    /**
     * This method registers command.
     *
     * @param command The command object to be added to the command map.
     * @param fallbackPrefix The name of the plugin.
     */
    public void registerCommand(Command command, String fallbackPrefix) {
        ((CraftServer) this.getServer()).getCommandMap().register(command.getName(), fallbackPrefix, command);
    }

    /**
     * Returns true if the server is running on BungeeCord, which is a plugin that allows for cross-server linkage through
     * a single proxy. Players can join different servers from a single proxy. This method currently is not used.
     *
     * @return True if the server is on BungeeCord and false otherwise.
     */
    public boolean isBungeeEnabled() {
        if (this.getServer().getVersion().contains("CraftBukkit")) {
            this.getLogger().severe("This plugin is not compatible with CraftBukkit implementations. Please update the server to Spigot, Paper, Tuinity, etc.");
            return false;
        }

        ConfigurationSection settings = this.getServer().spigot().getConfig().getConfigurationSection("settings");
        if (settings == null) {
            this.getLogger().severe("Failed to read server configuration files... disabling GalaxyCore.");
            return false;
        }

        if (settings.getBoolean("settings.bungeecord")) {
            this.getLogger().severe("This server is only compatible with BungeeCord. If the server is already hooked into BungeeCord, update the setting in the spigot.yml file.");
            return false;
        }

        return true;
    }

    /**
     * Returns the Towny object dependency. This will never be null since
     * the plugin automatically disables itself if the dependency is not found.
     *
     * @return The Towny dependency.
     */
    public Towny getTowny() {
        return this.towny;
    }

    /**
     * @return The database object the plugin uses to store player information.
     */
    public Database getDatabase() {
        return this.database;
    }

    /**
     * Due to the way the plugin runs during startup, we only want one instance of each manager class. As a result, we
     * can use a singleton to handle only one instance per manager class. The other option would be to instantiate an
     * instance in CorePlugin during startup and have getter methods to retrieve a specific instance, but I got lazy.
     *
     * @return An instance of this plugin.
     */
    public static CorePlugin getInstance() {
        return CorePlugin.instance;
    }
}
