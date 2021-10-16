package org.craftgalaxy.galaxycore.proxy;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.util.CaseInsensitiveMap;
import org.craftgalaxy.galaxycore.compat.Callback;
import org.craftgalaxy.galaxycore.proxy.command.*;
import org.craftgalaxy.galaxycore.proxy.config.ConfigSettings;
import org.craftgalaxy.galaxycore.proxy.database.DatabaseConnection;
import org.craftgalaxy.galaxycore.proxy.listener.PlayerListeners;
import org.craftgalaxy.galaxycore.proxy.player.manager.PlayerManager;
import org.craftgalaxy.galaxycore.proxy.punishment.IPunishmentSystem;
import org.craftgalaxy.galaxycore.proxy.punishment.impl.AdvancedBanSystem;
import org.craftgalaxy.galaxycore.proxy.punishment.impl.DefaultPunishmentSystem;
import org.craftgalaxy.galaxycore.proxy.runnable.ShutdownRunnable;
import org.craftgalaxy.galaxycore.proxy.runnable.Shutdownable;
import org.craftgalaxy.galaxycore.proxy.server.manager.ServerManager;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class CoreProxyPlugin extends Plugin implements Shutdownable {

    private final List<Command> commands = Arrays.asList(
            new ServerStatusCommand(this),
            new PasswordCommand(this),
            new FreezeCommand(this),
            new UnfreezeCommand(this),
            new ShutdownCommand(this),
            new AuthenticateCommand(this),
            new AltsCommand(this),
            new IPLookupCommand(this)
    );

    private final Map<String, Plugin> dependencies = new CaseInsensitiveMap<>();
    private IPunishmentSystem punishmentSystem;
    private ConfigSettings settings;
    private ScheduledTask shutdown;
    private ShutdownRunnable runnable;
    private ServerManager serverManager;
    private PlayerManager playerManager;
    private DatabaseConnection database;
    private static CoreProxyPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        if (!this.getDataFolder().exists() && !this.getDataFolder().mkdir()) {
            this.getProxy().getLogger().log(Level.SEVERE, "Unable to create plugin main directory");
            this.onDisable();
        } else {
            this.database = (new DatabaseConnection(this)).loadDatabase();
            if (!this.database.isConnected()) {
                this.getProxy().getLogger().log(Level.WARNING, "Failed to connect to the embedded SQLite database. Contact the developer if this occurs.");
                this.onDisable();
            } else {
                this.scanDependencies();
                this.settings = new ConfigSettings(this);
                if (!this.settings.isConnected()) {
                    this.onDisable();
                } else {
                    this.punishmentSystem = this.getProxy().getPluginManager().getPlugin("AdvancedBan") == null ? new AdvancedBanSystem(this) : new DefaultPunishmentSystem(this);
                    this.serverManager = new ServerManager(this);
                    this.playerManager = new PlayerManager(this);
                    this.registerCommands();
                    this.getProxy().getPluginManager().registerListener(this, new PlayerListeners(this));
                }
            }
        }
    }

    @Override
    public void onDisable() {
        this.getProxy().getPluginManager().unregisterListeners(this);
        this.getProxy().getPluginManager().unregisterCommands(this);
        this.serverManager.disable();
        this.playerManager.disable();
        this.database.disconnect();
        instance = null;
    }

    public void scanDependencies() {
        Set<String> dependencies = this.getDescription().getSoftDepends();
        for (String s : dependencies) {
            Plugin plugin = this.getProxy().getPluginManager().getPlugin(s);
            this.dependencies.put(s, plugin);
            if (plugin != null) {
                this.getProxy().getLogger().info("Detected " + plugin.getDescription().getName() + " as a dependency on this server... utilizing it!");
            }
        }
    }

    public boolean isDependencyPresent(String name) {
        return this.dependencies.get(name.replaceAll("\\s+", "_")) != null;
    }

    public void registerCommands() {
        this.commands.forEach((command) -> {
            this.getProxy().getPluginManager().registerCommand(this, command);
        });
    }

    public void cancelShutdownNow() {
        if (this.shutdown != null && this.runnable != null) {
            this.shutdown.cancel();
            this.shutdown = null;
            this.runnable = null;
        }
    }

    @Override
    public void cancelShutdown(String who) {
        if (this.shutdown != null) {
            this.shutdown.cancel();
            this.runnable = null;
            this.shutdown = null;
            this.getProxy().broadcast(StringUtil.EMPTY_STRING);
            this.getProxy().broadcast(new TextComponent(StringUtil.SERVER_PREFIX + ChatColor.YELLOW + " The proxy shutdown has been cancelled by " + ChatColor.LIGHT_PURPLE + who + ChatColor.YELLOW + "."));
            this.getProxy().broadcast(StringUtil.EMPTY_STRING);
        }
    }

    public void scheduleShutdown(int duration) {
        this.getProxy().broadcast(StringUtil.EMPTY_STRING);
        if (this.shutdown != null) {
            this.shutdown.cancel();
            this.getProxy().broadcast(new TextComponent(StringUtil.SERVER_PREFIX + ChatColor.YELLOW + " The network shutdown has been rescheduled to " + ChatColor.LIGHT_PURPLE + duration + " second" + (duration == 1 ? "" : "s") + ChatColor.YELLOW + "."));
        } else {
            this.getProxy().broadcast(new TextComponent(StringUtil.SERVER_PREFIX + ChatColor.YELLOW + " The network has been scheduled to shutdown in " + ChatColor.LIGHT_PURPLE + duration + " second" + (duration == 1 ? "" : "s") + ChatColor.YELLOW + "."));
        }

        this.getProxy().broadcast(StringUtil.EMPTY_STRING);
        this.runnable = new ShutdownRunnable(this, duration);
        this.shutdown = this.getProxy().getScheduler().schedule(this, this.runnable, 1L, 1L, TimeUnit.SECONDS);
    }

    public void shutdownStatus(String name, Callback<Integer> callback) {
        callback.done(this.shutdown != null && this.runnable != null ? this.runnable.getCountdown() : -1, null);
    }

    public IPunishmentSystem getPunishmentSystem() {
        return this.punishmentSystem;
    }

    public ConfigSettings getSettings() {
        return this.settings;
    }

    public ServerManager getServerManager() {
        return this.serverManager;
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public DatabaseConnection getDatabase() {
        return this.database;
    }

    public static CoreProxyPlugin getInstance() {
        return instance;
    }
}
