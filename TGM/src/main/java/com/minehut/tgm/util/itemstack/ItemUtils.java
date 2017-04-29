package com.minehut.tgm.util.itemstack;

import org.bukkit.inventory.ItemStack;

/**
 * Created by luke on 11/15/15.
 */
public class ItemUtils {
    public static boolean compare(ItemStack i1, ItemStack i2) {
        if (i1 != null && i2 != null) {
            if (i1.getItemMeta() != null && i2.getItemMeta() != null) {
                if (i1.getItemMeta().getDisplayName() != null && i2.getItemMeta().getDisplayName() != null) {
                    if (i1.getItemMeta().getDisplayName().equals(i2.getItemMeta().getDisplayName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
