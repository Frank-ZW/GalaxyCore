package net.craftgalaxy.galaxycore.util.bukkit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ItemUtil {

    public static ItemStack createItemStack(Material material, String displayName, int amount, String ... lore) {
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(displayName);
            itemMeta.setLore(Arrays.asList(lore));
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }
}
