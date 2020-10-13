package net.craftgalaxy.galaxycore.listener;

import net.craftgalaxy.galaxycore.gui.GuiClickable;
import net.craftgalaxy.galaxycore.gui.GuiFolder;
import net.craftgalaxy.galaxycore.gui.GuiItem;
import net.craftgalaxy.galaxycore.gui.manager.GuiManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Optional;

public class GuiListeners implements Listener {

	/*
	 * Note: this code is not my own - I took it from an open-sourced
	 * GitHub page.
	 */

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		e.getInventory();
		if (e.getCurrentItem() != null) {
			Optional<GuiFolder> optional = GuiManager.getInstance().getFolders().stream().filter(gui -> gui.getName().equalsIgnoreCase(e.getView().getTitle())).findFirst();
			if (optional.isPresent()) {
				GuiFolder folder = optional.get();
				GuiItem item = folder.getCurrentPage().getItem(e.getSlot());
				if (item != null) {
					e.setCancelled(true);
					if (item instanceof GuiClickable) {
						((GuiClickable) item).onInventoryClick(e);
					}
				}
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Optional<GuiFolder> optional = GuiManager.getInstance().getFolders().stream().filter(gui -> gui.getName().equalsIgnoreCase(e.getView().getTitle())).findFirst();
		if (optional.isPresent()) {
			GuiFolder folder = optional.get();
			GuiManager.getInstance().removeFolder(folder);
		}
	}
}
