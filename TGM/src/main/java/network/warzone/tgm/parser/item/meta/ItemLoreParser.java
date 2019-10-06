package network.warzone.tgm.parser.item.meta;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.util.ColorConverter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 09/14/2019
 */
public class ItemLoreParser implements ItemMetaParser {

    @Override
    public void parse(ItemStack itemStack, ItemMeta meta, JsonObject object) {
        if (object.has("lore")) {
            List<String> lore = new ArrayList<>();
            for (JsonElement element : object.getAsJsonArray("lore")) {
                lore.add(ColorConverter.filterString(element.getAsString()));
            }
            meta.setLore(lore);
        }
    }
}
