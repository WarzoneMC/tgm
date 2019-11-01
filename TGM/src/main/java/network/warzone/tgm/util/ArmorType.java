package network.warzone.tgm.util;

import network.warzone.tgm.util.itemstack.ItemFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Luke on 11/26/14.
 */
public enum ArmorType {

    HELMET, CHESTPLATE, LEGGINGS, BOOTS;

    public static ArmorType getArmorType(ItemStack itemStack) {
        Material material = itemStack.getType();

        if (material.name().contains("_HELMET")) {
            return ArmorType.HELMET;
        } else if (material.name().contains("_CHESTPLATE")) {
            return ArmorType.CHESTPLATE;
        } else if (material.name().contains("_LEGGINGS")) {
            return ArmorType.LEGGINGS;
        } else if (material.name().contains("_BOOTS")) {
            return ArmorType.BOOTS;
        } else {
            return null;
        }
    }

    public static ArmorType getArmorType(Material material) {
        return getArmorType(ItemFactory.createItem(material));
    }
}
