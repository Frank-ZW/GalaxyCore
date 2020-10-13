package net.craftgalaxy.galaxycore.gui;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface GuiClickable extends GuiItem {
	void onInventoryClick(InventoryClickEvent e);
}
