package network.warzone.tgm.parser.item.meta;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.parser.effect.EffectDeserializer;
import network.warzone.tgm.util.Strings;
import network.warzone.tgm.util.itemstack.ItemUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

/**
 * Created by Jorge on 09/14/2019
 */
public class ItemPotionParser implements ItemMetaParser {

    @Override
    public void parse(ItemStack itemStack, ItemMeta meta, JsonObject object) {
        if (!ItemUtils.isPotion(itemStack.getType())) return;
        if (!object.has("potion")) return;
        PotionMeta potionMeta = (PotionMeta) meta;

        /*
        Used to get potions that can be obtained from the Creative Inventory
        https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html
        "potion": {
             "type": "<type>",
             "extend": <boolean>,    Only used if "type" exists
             "upgrade": <boolean>    Only used if "type" exists
        }
        */

        if (object.getAsJsonObject("potion").has("type")) {
            PotionType type = PotionType.valueOf(Strings.getTechnicalName(object.getAsJsonObject("potion").get("type").getAsString()));
            boolean extended = false;
            boolean upgraded = false;
            if (object.getAsJsonObject("potion").has("extend"))
                extended = object.getAsJsonObject("potion").get("type").getAsBoolean();
            if (object.getAsJsonObject("potion").has("upgrade"))
                upgraded = object.getAsJsonObject("potion").get("upgrade").getAsBoolean();
            potionMeta.setBasePotionData(new PotionData(type, extended, upgraded));
        }

        /*
        "potion": {
            "effects": [
                {
                    "type": "<effect ID>",
                    "duration": <int>,
                    "amplifier": <int>,
                    "ambient": <boolean>,
                    "particles": <boolean>,
                }
            ]}
        */

        if (object.getAsJsonObject("potion").has("effects")) {
            for (JsonElement element : object.getAsJsonObject("potion").getAsJsonArray("effects")) {
                if (!element.isJsonObject()) continue;
                PotionEffect effect = EffectDeserializer.parse(element.getAsJsonObject());
                potionMeta.addCustomEffect(effect, true);
            }
        }
    }
}
