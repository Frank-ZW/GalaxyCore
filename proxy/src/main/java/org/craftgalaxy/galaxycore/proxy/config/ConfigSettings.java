package org.craftgalaxy.galaxycore.proxy.config;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;

public class ConfigSettings {

    private final CoreProxyPlugin plugin;
    private List<String> fallbackServers;
    private boolean connected = true;

    public ConfigSettings(CoreProxyPlugin plugin) {
        this.plugin = plugin;
        File file = new File(plugin.getDataFolder(), "configuration.yml");
        if (!file.exists()) {
            try (InputStream input = plugin.getResourceAsStream("configuration.yml")) {
                Files.copy(input, file.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create configuration.yml file", e);
                this.connected = false;
            }
        }

        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            this.fallbackServers = config.getStringList("fallback-servers");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get configuration provider loader", e);
            this.connected = false;
        }

    }

    @Nullable
    public ServerInfo fallbackServer(String exclusion) {
        for (String s : this.fallbackServers) {
            ServerInfo serverInfo = this.plugin.getProxy().getServerInfo(s);
            if (serverInfo != null) {
                return serverInfo;
            }
        }

        return null;
    }

    public boolean isConnected() {
        return this.connected;
    }
}