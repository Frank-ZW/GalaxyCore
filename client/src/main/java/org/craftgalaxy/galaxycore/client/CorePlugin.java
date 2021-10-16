package org.craftgalaxy.galaxycore.client;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.craftgalaxy.galaxycore.client.command.*;
import org.craftgalaxy.galaxycore.client.config.ConfigSettings;
import org.craftgalaxy.galaxycore.client.connection.manager.ConnectionManager;
import org.craftgalaxy.galaxycore.client.data.manager.PlayerManager;
import org.craftgalaxy.galaxycore.client.listener.PlayerListeners;
import org.craftgalaxy.galaxycore.client.listener.impl.DefaultChatListener;
import org.craftgalaxy.galaxycore.client.listener.impl.TownyChatListener;

import java.util.HashMap;
import java.util.Map;

public final class CorePlugin extends JavaPlugin {

    private final Map<String, CommandExecutor> commands = Map.of(
            "mutechat", new MuteChatCommand(),
            "pings", new PingsCommand(),
            "slowchat", new SlowChatCommand(),
            "miners", new MinersCommand(),
            "clearchat", new ClearChatCommand()
    );

    private ConfigSettings settings;
    private final Map<String, JavaPlugin> softDependencies = new HashMap<>();
    private static CorePlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        this.settings = new ConfigSettings(this);
        if (this.settings.isInitialStartup()) {
            Bukkit.getLogger().info(ChatColor.YELLOW + "Before this plugin can connect with the Proxy, shutdown the server and enter the name of the server as it appears in Bungee's config.yml file as well as the IP address and port. Restart the server once those changes have been made.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.settings.readSettings();
        this.scanSoftDependencies();
        PlayerManager.enable(this);
        ConnectionManager.enable(this);
        this.registerListeners();
        this.registerCommands();
    }

    @Override
    public void onDisable() {
        this.settings.saveSettings();
        HandlerList.unregisterAll(this);
        ConnectionManager.disable();
        PlayerManager.disable();
        instance = null;
    }

    private void scanSoftDependencies() {
        for (String s : this.getDescription().getSoftDepend()) {
            JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(s);
            if (plugin != null && plugin.isEnabled()) {
                this.softDependencies.put(s, plugin);
                Bukkit.getLogger().info("Detected " + plugin.getName() + " as a dependency on this server... utilizing it!");
            } else {
                this.softDependencies.put(s, null);
            }
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), this);
        Bukkit.getPluginManager().registerEvents(this.isDependencyPresent("Towny") && this.isDependencyPresent("TownyChat") ? new TownyChatListener() : new DefaultChatListener(), this);
    }

    private void registerCommands() {
        for (Map.Entry<String, CommandExecutor> entry : this.commands.entrySet()) {
            PluginCommand command = this.getCommand(entry.getKey());
            if (command != null) {
                command.setExecutor(entry.getValue());
            }
        }
    }

    public boolean isDependencyPresent(String name) {
        return this.softDependencies.get(name.replace(" ", "_")) != null;
    }

    public ConfigSettings getSettings() {
        return this.settings;
    }

    public static CorePlugin getInstance() {
        return instance;
    }
}
