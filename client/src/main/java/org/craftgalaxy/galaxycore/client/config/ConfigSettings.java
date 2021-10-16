package org.craftgalaxy.galaxycore.client.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.craftgalaxy.galaxycore.client.CorePlugin;

import java.io.File;
import java.util.List;

public class ConfigSettings {

    private final CorePlugin plugin;
    private final FileConfiguration config;
    private String name;
    private String host;
    private int port;
    private String pluginHost;
    private int pluginPort;
    private ConfigSettings.ChatFilterInfo chatFilterInfo;
    private boolean initialStartup;

    public ConfigSettings(CorePlugin plugin) {
        this.plugin = plugin;
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            this.initialStartup = true;
            plugin.saveDefaultConfig();
        }

        this.config = plugin.getConfig();
    }

    public void readSettings() {
        this.name = this.config.getString("connection-settings.proxy-server.name");
        this.host = this.config.getString("connection-settings.proxy-server.host");
        this.port = this.config.getInt("connection-settings.proxy-server.port");
        this.pluginHost = this.config.getString("connection-settings.plugin-connection.host");
        this.pluginPort = this.config.getInt("connection-settings.plugin-connection.port");
        this.chatFilterInfo = new ConfigSettings.ChatFilterInfo(this.config.getStringList("chat-filter-settings.blacklisted-words"), this.config.getStringList("chat-filter-settings.foreign-words"), this.config.getInt("chat-filter-settings.slow-mode.donator-duration"), this.config.getInt("chat-filter-settings.slow-mode.default-duration"));
    }

    public void saveSettings() {
        this.plugin.saveConfig();
    }

    public String getName() {
        return this.name;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getPluginHost() {
        return this.pluginHost;
    }

    public int getPluginPort() {
        return this.pluginPort;
    }

    public ConfigSettings.ChatFilterInfo getChatFilterInfo() {
        return this.chatFilterInfo;
    }

    public boolean isInitialStartup() {
        return this.initialStartup;
    }

    public static record ChatFilterInfo(List<String> blacklisted, List<String> foreignWords, int donatorCooldown, int defaultCooldown) {

        public ChatFilterInfo(List<String> blacklisted, List<String> foreignWords, int donatorCooldown, int defaultCooldown) {
            this.blacklisted = blacklisted;
            this.foreignWords = foreignWords;
            this.donatorCooldown = donatorCooldown;
            this.defaultCooldown = defaultCooldown;
        }

        public List<String> getBlacklisted() {
            return this.blacklisted;
        }

        public List<String> getForeignWords() {
            return this.foreignWords;
        }

        public int getDonatorCooldown() {
            return this.donatorCooldown;
        }

        public int getDefaultCooldown() {
            return this.defaultCooldown;
        }

        public List<String> blacklisted() {
            return this.blacklisted;
        }

        public List<String> foreignWords() {
            return this.foreignWords;
        }

        public int donatorCooldown() {
            return this.donatorCooldown;
        }

        public int defaultCooldown() {
            return this.defaultCooldown;
        }
    }
}
