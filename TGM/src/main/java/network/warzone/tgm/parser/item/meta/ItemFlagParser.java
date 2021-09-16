package network.warzone.tgm.parser.item.meta;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.util.Strings;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Jorge on 09/14/2019
 */
public class ItemFlagParser implements ItemMetaParser {

    // https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemFlag.html

    @Override
    public void parse(ItemStack itemStack, ItemMeta meta, JsonObject object) {
        if (object.has("flags"))
            for (JsonElement element : object.getAsJsonArray("flags"))
                meta.addItemFlags(ItemFlag.valueOf(Strings.getTechnicalName(element.getAsString())));
    }
}
