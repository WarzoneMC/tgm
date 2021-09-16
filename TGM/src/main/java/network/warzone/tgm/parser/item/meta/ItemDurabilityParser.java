package network.warzone.tgm.parser.item.meta;

import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Jorge on 09/14/2019
 */
public class ItemDurabilityParser implements ItemMetaParser {

    @Override
    public void parse(ItemStack itemStack, ItemMeta meta, JsonObject object) {
        if (meta instanceof Damageable && object.has("durability"))
            ((Damageable) meta).setDamage(object.get("durability").getAsShort());
    }
}
