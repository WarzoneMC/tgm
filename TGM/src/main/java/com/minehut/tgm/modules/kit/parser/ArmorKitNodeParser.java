package com.minehut.tgm.modules.kit.parser;

import com.google.gson.JsonObject;
import com.minehut.tgm.modules.kit.KitNode;
import com.minehut.tgm.modules.kit.types.ArmorKitNode;
import com.minehut.tgm.modules.kit.types.ArmorType;
import com.minehut.tgm.modules.kit.types.ItemKitNode;
import com.minehut.tgm.util.ColorConverter;
import com.minehut.tgm.util.Strings;
import com.minehut.tgm.util.itemstack.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;
import java.util.List;

public class ArmorKitNodeParser implements KitNodeParser {

    @Override
    public List<KitNode> parse(JsonObject jsonObject) {
        ArmorType armorType = null;
        if (jsonObject.has("type")) {
            try {
                armorType = ArmorType.valueOf(Strings.getTechnicalName(jsonObject.get("type").getAsString()));
                if (armorType == null) {
                    return null;
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Unable to parse Kit Armor Type \"" + jsonObject.get("type").getAsString() + "\"");
                return null;
            }
        } else {
            return null;
        }

        ItemStack itemStack = ItemKitNodeParser.parseItemStack(jsonObject);

        return Arrays.asList(new ArmorKitNode(armorType, itemStack));
    }
}
