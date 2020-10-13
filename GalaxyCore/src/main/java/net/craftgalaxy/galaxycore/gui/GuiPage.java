package net.craftgalaxy.galaxycore.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GuiPage {

	private final Map<Integer, GuiItem> items;
	private final GuiFolder folder;

	public GuiPage(GuiFolder folder) {
		this.folder = folder;
		this.items = new HashMap<>();
	}

	public void updatePage() {
		this.folder.getInventory().clear();
		this.items.forEach((slot, item) -> this.folder.getInventory().setItem(slot, item.getItemStack()));
		this.folder.getInventory().getViewers().stream().map(viewer -> (Player) viewer).forEach(Player::updateInventory);
	}

	public void fill() {
		for (int i = 0; i < this.folder.getSize(); i++) {
			if (this.getItem(i) == null) {
				this.addItem(i, new GuiFiller());
			}
		}
	}

	public GuiItem getItem(int slot) {
		return this.items.get(slot);
	}

	public void addItem(int slot, GuiItem item) {
		this.items.put(slot, item);
	}

	public Map<Integer, GuiItem> getItems() {
		return this.items;
	}
}
