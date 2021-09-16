package network.warzone.tgm.parser.item.meta;

import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Jorge on 09/14/2019
 */
public interface ItemMetaParser {

    void parse(ItemStack itemStack, ItemMeta meta, JsonObject object);

}
