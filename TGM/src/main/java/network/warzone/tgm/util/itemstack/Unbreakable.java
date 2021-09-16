package network.warzone.tgm.util.itemstack;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Unbreakable {

	public static void setUnbreakable(ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		meta.setUnbreakable(true);
		stack.setItemMeta(meta);
	}
	
}
