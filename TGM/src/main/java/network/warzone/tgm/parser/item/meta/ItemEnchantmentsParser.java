package network.warzone.tgm.parser.item.meta;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Jorge on 09/14/2019
 */
public class ItemEnchantmentsParser implements ItemMetaParser {

    @Override
    public void parse(ItemStack itemStack, ItemMeta meta, JsonObject object) {
        if (object.has("enchantments")) {
            for (JsonElement element : object.getAsJsonArray("enchantments")) {
                String[] split = element.getAsString().split(":");
                String id = split[0];
                int level = Integer.parseInt(split[1]);
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(id));
                if (enchantment != null) {
                    meta.addEnchant(enchantment, level, true);
                }
            }
        }
    }
}
