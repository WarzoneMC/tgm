package network.warzone.tgm.util.itemstack;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by luke on 11/15/15.
 */
public class ItemUtils {
    public static final Set<Material> BANNER_MATERIALS = new HashSet<>();
    public static final Set<Material> UNBREAKABLE_MATERIALS = new HashSet<>();

    static {
        for (Material material : Material.values()) {
            if (material.name().contains("_BANNER") && !material.name().contains("LEGACY"))
                BANNER_MATERIALS.add(material);
        }

        Set<String> unbreakableItemTypes = new HashSet<>(Arrays.asList("SWORD", "PICKAXE", "AXE", "SHOVEL", "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"));
        for (Material material : Material.values()) {
            final String materialName = material.name();
            int underscore_position = materialName.lastIndexOf("_");
            if (underscore_position == -1) continue;
            String item_class = materialName.substring(underscore_position + 1);
            if (unbreakableItemTypes.contains(item_class)) UNBREAKABLE_MATERIALS.add(material);
        }
    }

    public static boolean compare(ItemStack i1, ItemStack i2) {
        if (i1 != null && i2 != null && i1.getItemMeta() != null && i2.getItemMeta() != null) {
            return i1.getItemMeta().getDisplayName().equals(i2.getItemMeta().getDisplayName());
        }
        return false;
    }
    
    public static boolean isPotion(Material material) {
        return material.equals(Material.POTION) || material.equals(Material.SPLASH_POTION) || material.equals(Material.LINGERING_POTION);
    }

    public static String itemToString(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) {
            return "their hands";
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) stringBuilder.append("Enchanted ");
        stringBuilder.append(materialToString(item.getType()));
        return stringBuilder.toString().trim();
    }

    public static String materialToString(Material material) {
        StringBuilder stringBuilder = new StringBuilder();

        String materialName = material.toString();
        for (String word : materialName.split("_")) {
            word = word.toLowerCase();
            word = word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
            stringBuilder.append(word);
        }
        return stringBuilder.toString().trim();
    }
}
