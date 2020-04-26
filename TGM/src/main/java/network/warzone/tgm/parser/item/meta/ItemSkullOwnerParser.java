package network.warzone.tgm.parser.item.meta;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

/**
 * Created by Jorge on 09/14/2019
 */
public class ItemSkullOwnerParser implements ItemMetaParser {

    @Override
    public void parse(ItemStack itemStack, ItemMeta meta, JsonObject object) {
        if (itemStack.getType().equals(Material.PLAYER_HEAD) && object.has("skullOwner")) {
            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(object.get("skullOwner").getAsString())));
        }
    }
}
