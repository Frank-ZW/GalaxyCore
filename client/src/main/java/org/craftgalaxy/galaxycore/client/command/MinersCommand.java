package org.craftgalaxy.galaxycore.client.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MinersCommand implements CommandExecutor {

    private final HoverEventSource<Component> hoverEvent;

    public MinersCommand() {
        this.hoverEvent = Component.text(ChatColor.GREEN + "Click to teleport to this player").asHoverEvent();
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(StringUtil.MINERS_COMMAND_PERMISSION)) {
            sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
            return true;
        }

        if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + " To view players below a certain Y-level, type /miners [Y-level]");
            return true;
        }

        int yLevel = 40;
        if (args.length == 1) {
            try {
                yLevel = Integer.parseInt(args[0]);
            } catch (NumberFormatException var9) {
                sender.sendMessage(ChatColor.RED + "The Y-level entered must be a number.");
                return true;
            }

            if (yLevel < 1) {
                sender.sendMessage(ChatColor.RED + "The Y-level entered must be a positive, whole number.");
                return true;
            }
        }

        List<Player> miners = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().getBlockY() <= yLevel) {
                miners.add(player);
            }
        }

        sender.sendMessage(this.formatMiners(miners, yLevel));
        return true;
    }

    private Component formatMiners(List<Player> miners, int yLevel) {
        Component component = StringUtil.SERVER_PREFIX.append(Component.text(ChatColor.YELLOW + "The following players are below " + ChatColor.LIGHT_PURPLE + "Y = " + yLevel + ChatColor.YELLOW + ": "));
        switch(miners.size()) {
            case 0:
                return component;
            case 1:
                return component.append(miners.get(0).displayName()).clickEvent(ClickEvent.runCommand("/tp " + miners.get(0).getName())).hoverEvent(this.hoverEvent);
            case 2:
                return component.append(miners.get(0).displayName()).clickEvent(ClickEvent.runCommand("/tp " + miners.get(0).getName())).hoverEvent(this.hoverEvent).append(Component.text(ChatColor.YELLOW + " and ")).clickEvent(null).hoverEvent(null).append(miners.get(1).displayName()).clickEvent(ClickEvent.runCommand("/tp " + miners.get(1).getName())).hoverEvent(this.hoverEvent);
            default:
                for(int i = 0; i < miners.size(); ++i) {
                    Player player = miners.get(i);
                    if (i == miners.size() - 1) {
                        component = component.append(Component.text("and ")).append(Component.text(ChatColor.GREEN + player.getName())).clickEvent(ClickEvent.runCommand("/tp " + player.getName())).hoverEvent(this.hoverEvent);
                    } else {
                        component = component.append(Component.text(ChatColor.GREEN + player.getName())).clickEvent(ClickEvent.runCommand("/tp " + player.getName())).hoverEvent(this.hoverEvent).append(Component.text(ChatColor.YELLOW + ", ")).clickEvent(null).hoverEvent(null);
                    }
                }

                return component;
        }
    }
}
