package net.craftgalaxy.galaxycore.command;

import net.craftgalaxy.galaxycore.CorePlugin;
import org.bukkit.entity.Player;

public abstract class SubCommand {

    protected final CorePlugin plugin;

    public SubCommand(CorePlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void accept(Player sender, Player target, String[] args);
}
