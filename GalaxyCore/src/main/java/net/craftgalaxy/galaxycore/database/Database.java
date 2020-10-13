package net.craftgalaxy.galaxycore.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.data.PlayerData;
import net.craftgalaxy.galaxycore.util.CorePermissions;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;

public class Database {

    /*
     * This class was based off of multiple Minecraft forum posts and Stack Overflow questions regarding
     * how to add embedded databases into Minecraft plugins. Not all of this code is entirely my own.
     */

    private final CorePlugin plugin;
    private Connection connection;

    public Database(CorePlugin plugin) {
        this.plugin = plugin;
    }

    public void disable() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
        }
    }

    /**
     * This method generates or returns the connection to the embedded database. If an exception is thrown, the
     * method returns null.
     *
     * @return The connection to the SQLite database.
     */
    @Nullable
    public Connection getConnection() {
        File databaseFile = new File(this.plugin.getDataFolder(), "database.db");
        if (!databaseFile.exists()) {
            try {
                if (databaseFile.createNewFile()) {
                    this.plugin.getLogger().info(ChatColor.GREEN + "Successfully created new SQLite database.");
                }
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to create SQLite database in the main plugin folder directory", e);
            }
        }

        try {
            if (this.connection != null && !this.connection.isClosed()) {
                return this.connection;
            }

            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            return this.connection;
        } catch (ClassNotFoundException | SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to retrieve database connection", e);
        }

        return null;
    }

    /**
     * Loads in the embedded database; creates a userdata table to store player information if one
     * does not exist.
     */
    public void loadDatabase() {
        this.connection = this.getConnection();
        Statement createDataStatement = null;
        try {
            if (this.connection != null) {
                createDataStatement = this.connection.createStatement();
                createDataStatement.executeUpdate("CREATE TABLE IF NOT EXISTS userdata (" +
                        "unique_id MESSAGE_TEXT NOT NULL," +
                        "username MESSAGE_TEXT NOT NULL," +
                        "password MESSAGE_TEXT," +
                        "receive_alerts INTEGER," +
                        "receive_pings INTEGER," +
                        "force_lost_newbie_protection INTEGER," +
                        "ip_addresses MESSAGE_TEXT NOT NULL" +
                        ")");
            }
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to load the database", e);
        } finally {
            this.close(createDataStatement);
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
            }
        }
    }

    /**
     * Fetches player information saved to the database. This method should be called asynchronously to prevent the
     * main thread from being blocked. If the player does not have new information, null is returned.
     *
     * @param player The player object to retrieve information for.
     * @return The PlayerData object for the player.
     */
    @Nullable
    public PlayerData fetchPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        Connection connection = this.getConnection();
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            if (connection != null) {
                statement = connection.prepareStatement("SELECT unique_id, password, receive_alerts, receive_pings, force_lost_newbie_protection, ip_addresses FROM userdata WHERE unique_id = ?");
                statement.setString(1, uuid.toString());
                result = statement.executeQuery();
                while (result.next()) {
                    if (uuid.toString().equals(result.getString("unique_id"))) {
                        String password = result.getString("password");
                        boolean receiveAlerts = player.hasPermission(CorePermissions.ALERTS_PERMISSION) && result.getBoolean("receive_alerts");
                        boolean receivePings = player.hasPermission(CorePermissions.PINGS_PERMISSION) && result.getBoolean("receive_pings");
                        boolean forceLostNewbieProtection = result.getBoolean("force_lost_newbie_protection");
                        Deque<String> ipAddresses = new Gson().fromJson(result.getString("ip_addresses"), new TypeToken<LinkedList<String>>() {}.getType());

                        PlayerData playerData = new PlayerData(player);
                        playerData.setReceiveAlerts(receiveAlerts);
                        playerData.setReceivePings(receivePings);
                        playerData.setPassword(password);
                        playerData.setForceLostNewbieProtection(forceLostNewbieProtection);
                        playerData.setIpAddresses(ipAddresses);
                        connection.close();
                        return playerData;
                    }
                }
            }
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to fetch player data for " + player.getName(), e);
        } finally {
            this.close(result, statement);
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
            }
        }

        return null;
    }

    /**
     * Saves player data information to the database.
     *
     * @param playerData The player data object to be saved to the database.
     */
    public void writePlayerData(PlayerData playerData) {
        UUID uuid = playerData.getUniqueId();
        Connection connection = this.getConnection();
        PreparedStatement statement;
        try {
            if (connection != null) {
                // Checks if the player has already information saved to the database or else an exception is thrown
                // Counts the number of rows in userdata with the player's unique ID; if the count is equal to one,
                // we know that the player has already played on the server before.
                PreparedStatement countStatement = connection.prepareStatement("SELECT count(*) AS playercount FROM userdata WHERE unique_id = ?");
                countStatement.setString(1, uuid.toString());
                ResultSet countResult = countStatement.executeQuery();
                while (countResult.next()) {
                    int count = countResult.getInt("playercount");
                    if (count == 1) {
                        statement = connection.prepareStatement("UPDATE userdata SET " +
                                "username = ?," +
                                "password = ?," +
                                "receive_alerts = ?, " +
                                "receive_pings = ?, " +
                                "force_lost_newbie_protection = ?, " +
                                "ip_addresses = ? " +
                                "WHERE unique_id = ?");
                        statement.setString(1, playerData.getName());
                        statement.setString(2, playerData.getPassword());
                        statement.setBoolean(3, playerData.isReceiveAlerts());
                        statement.setBoolean(4, playerData.isReceivePings());
                        statement.setBoolean(5, playerData.isForceLostNewbieProtection());
                        statement.setString(6, new Gson().toJson(playerData.getIpAddresses()));
                        statement.setString(7, uuid.toString());
                        statement.executeUpdate();
                        this.close(countResult, countStatement, statement);
                        connection.close();
                        return;
                    }
                }

                statement = connection.prepareStatement("INSERT INTO userdata(" +
                        "unique_id, " +
                        "username, " +
                        "password, " +
                        "receive_alerts, " +
                        "receive_pings, " +
                        "force_lost_newbie_protection, " +
                        "ip_addresses) " +
                        "VALUES(?, ?, ?, ?, ?, ?, ?)");
                statement.setString(1, uuid.toString());
                statement.setString(2, playerData.getName());
                statement.setString(3, playerData.getPassword());
                statement.setBoolean(4, playerData.isReceiveAlerts());
                statement.setBoolean(5, playerData.isReceivePings());
                statement.setBoolean(6, playerData.isForceLostNewbieProtection());
                statement.setString(7, new Gson().toJson(playerData.getIpAddresses()));
                statement.executeUpdate();
                this.close(countResult, statement);
            }
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save player data information for " + playerData.getName(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
                }
            }
        }
    }

    // Lazy way of closing SQLite statements
    public void close(Statement ... statements) {
        try {
            for (Statement statement : statements) {
                if (statement != null) {
                    statement.close();
                }
            }
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to close one or more statements", e);
        }
    }

    // Lazy way of closing ResultSets and statements
    public void close(ResultSet result, Statement ... statements) {
        try {
            for (Statement statement : statements) {
                if (statement != null) {
                    statement.close();
                }
            }

            if (result != null) {
                result.close();
            }
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to close one or more statements", e);
        }
    }
}
