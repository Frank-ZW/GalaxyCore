package net.craftgalaxy.galaxycore.gui;

import net.craftgalaxy.galaxycore.CorePlugin;
import net.craftgalaxy.galaxycore.gui.manager.GuiManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class GuiFolder {

	private final Inventory inventory;
	private final String name;
	private final int size;
	private GuiPage currentPage;

	public GuiFolder(String name, int size) {
		this.name = name;
		this.size = size;
		this.inventory = CorePlugin.getInstance().getServer().createInventory(null, size, name);
		GuiManager.getInstance().addFolder(this);
	}

	public void openGui(Player player) {
		player.closeInventory();
		player.openInventory(this.inventory);
	}

	public void setCurrentPage(GuiPage currentPage) {
		this.currentPage = currentPage;
		this.currentPage.updatePage();
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public String getName() {
		return this.name;
	}

	public int getSize() {
		return this.size;
	}

	public GuiPage getCurrentPage() {
		return this.currentPage;
	}
}
