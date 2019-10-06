package network.warzone.tgm.parser.item.meta;

import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Jorge on 09/14/2019
 */
public class ItemUnbreakableParser implements ItemMetaParser {

    @Override
    public void parse(ItemStack itemStack, ItemMeta meta, JsonObject object) {
        if (object.has("unbreakable"))
            meta.setUnbreakable(object.get("unbreakable").getAsBoolean());
    }
}
