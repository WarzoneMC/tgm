package network.warzone.tgm.parser.item.meta;

import com.google.gson.JsonObject;
import network.warzone.tgm.util.ColorConverter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Jorge on 09/14/2019
 */
public class ItemDisplayNameParser implements ItemMetaParser {

    @Override
    public void parse(ItemStack itemStack, ItemMeta meta, JsonObject object) {
        if (object.has("display_name"))
            meta.setDisplayName(ColorConverter.filterString(object.get("display_name").getAsString()));
    }
}
