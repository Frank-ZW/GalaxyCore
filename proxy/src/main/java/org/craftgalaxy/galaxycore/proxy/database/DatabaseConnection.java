package org.craftgalaxy.galaxycore.proxy.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.craftgalaxy.galaxycore.proxy.CoreProxyPlugin;
import org.craftgalaxy.galaxycore.proxy.player.PlayerData;
import org.craftgalaxy.galaxycore.proxy.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseConnection {

    private final CoreProxyPlugin plugin;
    private Connection connection;
    private boolean connected = true;
    private final Gson gson = new Gson();

    public DatabaseConnection(CoreProxyPlugin plugin) {
        this.plugin = plugin;
    }

    public void disconnect() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
        } finally {
            this.connected = false;
        }

    }

    public Connection loadConnection() {
        File file = new File(this.plugin.getDataFolder(), "database.db");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    this.connected = false;
                    this.plugin.getLogger().info(ChatColor.RED + "Failed to create database.db file... automatically disabling GalaxyCore");
                }
            } catch (IOException e) {
                this.connected = false;
                this.plugin.getLogger().log(Level.SEVERE, "Failed to create SQLite database in the main plugin folder directory", e);
                return null;
            }
        }

        try {
            if (this.connection != null && !this.connection.isClosed()) {
                return this.connection;
            }

            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + file);
            return this.connection;
        } catch (SQLException | ClassNotFoundException var3) {
            this.connected = false;
            this.plugin.getLogger().log(Level.SEVERE, "Failed to retrieve database connection", var3);
            return null;
        }
    }

    public DatabaseConnection loadDatabase() {
        this.loadConnection();
        Statement statement = null;

        try {
            if (this.connection != null) {
                statement = this.connection.createStatement();
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS userdata (unique_id MESSAGE_TEXT NOT NULL, username MESSAGE_TEXT NOT NULL, password MESSAGE_TEXT, ip_addresses MESSAGE_TEXT NOT NULL, recent_address MESSAGE_TEXT NOT NULL, receive_pings INTEGER, frozen INTEGER)");
            }
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to load the database", e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }

                if (this.connection != null) {
                    this.connection.close();
                }
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
            }
        }

        return this;
    }

    @SuppressWarnings("UnstableApiUsage")
    public PlayerData fetchPlayerData(ProxiedPlayer player) {
        this.loadConnection();
        UUID uuid = player.getUniqueId();
        PreparedStatement statement = null;
        ResultSet result = null;
        PlayerData playerData = new PlayerData(player);
        if (this.connection == null) {
            return playerData;
        } else {
            try {
                statement = this.connection.prepareStatement("SELECT unique_id, password, ip_addresses, recent_address, receive_pings, frozen FROM userdata WHERE unique_id = ?");
                statement.setString(1, uuid.toString());
                result = statement.executeQuery();

                while(result.next()) {
                    if (uuid.toString().equals(result.getString("unique_id"))) {
                        String password = result.getString("password");
                        String lastHost = result.getString("recent_address");
                        boolean frozen = result.getBoolean("frozen");
                        boolean receivePings = player.hasPermission(StringUtil.TOGGLE_PINGS_PERMISSION) && result.getBoolean("receive_pings");
                        List<String> addresses = this.gson.fromJson(result.getString("ip_addresses"), new TypeToken<ArrayList<String>>() {}.getType());

                        this.connection.close();
                        playerData.setPassword(password, false);
                        playerData.setAddresses(addresses);
                        playerData.setReceivePings(receivePings);
                        playerData.setFrozen(frozen);
                        playerData.setLastHost(lastHost);
                        break;
                    }
                }
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to fetch player data for " + player.getName(), e);
            } finally {
                try {
                    if (result != null) {
                        result.close();
                    }

                    if (statement != null) {
                        statement.close();
                    }

                    this.connection.close();
                } catch (SQLException ignored) {}
            }

            return playerData;
        }
    }

    public void writePlayerData(PlayerData playerData) {
        this.loadConnection();
        if (this.connection != null) {
            UUID uniqueId = playerData.getUniqueId();

            try {
                PreparedStatement countStatement = this.connection.prepareStatement("SELECT count(*) AS playercount FROM userdata WHERE unique_id = ?");
                countStatement.setString(1, uniqueId.toString());
                ResultSet countResult = countStatement.executeQuery();

                PreparedStatement statement;
                while(countResult.next()) {
                    int count = countResult.getInt("playercount");
                    if (count == 1) {
                        statement = this.connection.prepareStatement("UPDATE userdata SET password = ?, username = ?, ip_addresses = ?, recent_address = ?, receive_pings = ?, frozen = ?WHERE unique_id = ?");
                        statement.setString(1, playerData.getPassword());
                        statement.setString(2, playerData.getName());
                        statement.setString(3, this.gson.toJson(playerData.getAddresses()));
                        statement.setString(4, playerData.getCurrentHost() == null ? playerData.getLastHost() : playerData.getCurrentHost());
                        statement.setBoolean(5, playerData.isReceivePings());
                        statement.setBoolean(6, playerData.isFrozen());
                        statement.setString(7, uniqueId.toString());
                        statement.executeUpdate();
                        countResult.close();
                        countStatement.close();
                        statement.close();
                        return;
                    }
                }

                statement = this.connection.prepareStatement("INSERT INTO userdata(unique_id, username, password, ip_addresses, recent_address, receive_pings, frozen) VALUES(?, ?, ?, ?, ?, ?, ?)");
                statement.setString(1, uniqueId.toString());
                statement.setString(2, playerData.getName());
                statement.setString(3, playerData.getPassword());
                statement.setString(4, this.gson.toJson(playerData.getAddresses() == null ? new ArrayList<>() : playerData.getAddresses()));
                statement.setString(5, playerData.getCurrentHost() == null ? playerData.getLastHost() : playerData.getCurrentHost());
                statement.setBoolean(6, playerData.isReceivePings());
                statement.setBoolean(7, playerData.isFrozen());
                statement.executeUpdate();
                countResult.close();
                countStatement.close();
                statement.close();
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to write player data " + playerData.getName() + " to the database", e);
            }
        }
    }

    public boolean isConnected() {
        return this.connected;
    }
}
