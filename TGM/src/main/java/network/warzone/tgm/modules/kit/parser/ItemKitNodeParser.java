package network.warzone.tgm.modules.kit.parser;

import com.google.gson.JsonObject;
import network.warzone.tgm.modules.kit.KitNode;
import network.warzone.tgm.modules.kit.types.ItemKitNode;
import network.warzone.tgm.parser.item.ItemDeserializer;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class ItemKitNodeParser implements KitNodeParser {

    private boolean hasColor;

    @Override
    public List<KitNode> parse(JsonObject jsonObject) {
        int slot = 0;

        if (jsonObject.get("slot").getAsString() != null) {
            switch (jsonObject.get("slot").getAsString()) {
                case "head":
                case "helmet":
                    slot = 103;
                    break;
                case "chest":
                case "chestplate":
                    slot = 102;
                    break;
                case "legs":
                case "leggings":
                    slot = 101;
                    break;
                case "feet":
                case "boots":
                    slot = 100;
                    break;
                case "otherhand":
                case "offhand":
                    slot = -106;
                    break;
                default:
                    slot = jsonObject.get("slot").getAsInt();
                    break;
            }
        }

        return Collections.singletonList(new ItemKitNode(slot, parseItemStack(jsonObject), hasColor));
    }

    private ItemStack parseItemStack(JsonObject jsonObject) {
        ItemStack itemStack = ItemDeserializer.parse(jsonObject);
        if (jsonObject.has("color") && itemStack.getType().name().contains("LEATHER_")) { // Leather armor
            hasColor = true;
        }
        return itemStack;
    }
}
