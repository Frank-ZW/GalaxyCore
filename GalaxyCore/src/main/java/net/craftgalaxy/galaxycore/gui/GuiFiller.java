package net.craftgalaxy.galaxycore.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GuiFiller implements GuiItem {

	@Override
	public ItemStack getItemStack() {
		return new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
	}
}
