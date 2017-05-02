package com.minehut.tgm.modules.kit.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.minehut.tgm.modules.kit.KitNode;
import com.minehut.tgm.modules.kit.types.ItemKitNode;
import com.minehut.tgm.util.Strings;
import com.minehut.tgm.util.itemstack.ItemFactory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemKitNodeParser implements KitNodeParser {

    @Override
    public List<KitNode> parse(JsonObject jsonObject) {
        if (jsonObject.has("type")) {
            if (!jsonObject.get("type").getAsString().equalsIgnoreCase("item")) {
                return null; //not this type of kit node
            }
        }

        ItemStack itemStack = parseItemStack(jsonObject);
        int slot = jsonObject.get("slot").getAsInt();

        return Arrays.asList(new ItemKitNode(slot, itemStack));
    }

    public static ItemStack parseItemStack(JsonObject jsonObject) {
        Material material = Material.valueOf(Strings.getTechnicalName(jsonObject.get("material").getAsString()));
        ItemStack itemStack = ItemFactory.createItem(material);

        if (jsonObject.has("amount")) {
            itemStack.setAmount(jsonObject.get("amount").getAsInt());
        }

        if (jsonObject.has("enchantments")) {
            for (JsonElement element : jsonObject.getAsJsonArray("enchantments")) {
                String[] split = element.getAsString().split(":");
                int level = Integer.valueOf(split[1]);
                Enchantment enchantment = Enchantment.getByName(split[0]);
                if (enchantment != null) {
                    itemStack.addEnchantment(enchantment, level);
                }
            }
        }

        return itemStack;
    }
}
