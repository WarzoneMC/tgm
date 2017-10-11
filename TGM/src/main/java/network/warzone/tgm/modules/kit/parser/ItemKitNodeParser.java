package network.warzone.tgm.modules.kit.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.modules.kit.KitNode;
import network.warzone.tgm.modules.kit.types.ItemKitNode;
import network.warzone.tgm.util.ColorConverter;
import network.warzone.tgm.util.Strings;
import network.warzone.tgm.util.itemstack.ItemFactory;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
        Material material = Material.valueOf(Strings.getTechnicalName(jsonObject.get("material").getAsString()));
        ItemStack itemStack = ItemFactory.createItem(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        hasColor = false;

        if (jsonObject.has("display_name")) {
            itemMeta.setDisplayName(ColorConverter.filterString(jsonObject.get("display_name").getAsString()));
        }

        if (jsonObject.has("lore")) {
            List<String> lore = new ArrayList<>();
            for (JsonElement element : jsonObject.getAsJsonArray("lore")) {
                lore.add(ColorConverter.filterString(element.getAsString()));
            }
            itemMeta.setLore(lore);
        }

        if (jsonObject.has("amount")) {
            itemStack.setAmount(jsonObject.get("amount").getAsInt());
        }

        if (jsonObject.has("unbreakable")) {
            itemMeta.setUnbreakable(jsonObject.get("unbreakable").getAsBoolean());
        }

        if (jsonObject.has("flags")) {
            for (JsonElement element : jsonObject.getAsJsonArray("flags")) {
                itemMeta.addItemFlags(ItemFlag.valueOf(Strings.getTechnicalName(element.getAsString())));
            }
        }

        // https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html
        if (jsonObject.has("enchantments")) {
            for (JsonElement element : jsonObject.getAsJsonArray("enchantments")) {
                String[] split = element.getAsString().split(":");
                int level = Integer.valueOf(split[1]);
                Enchantment enchantment = Enchantment.getByName(split[0]);
                if (enchantment != null) {
                    itemStack.addUnsafeEnchantment(enchantment, level);
                }
            }
        }

        if (jsonObject.has("durability")) {
            itemStack.setDurability(jsonObject.get("durability").getAsShort());
        }

        if (material.equals(Material.SKULL_ITEM) && jsonObject.has("skullOwner")) {
            SkullMeta skullMeta = (SkullMeta) itemMeta;
            skullMeta.setOwner(jsonObject.get("skullOwner").getAsString());
            itemStack.setItemMeta(skullMeta);
        }

        //TODO Clean this up with new kit module
        if (material.equals(Material.WRITTEN_BOOK)) {
            BookMeta bookMeta = (BookMeta) itemMeta;

            bookMeta.setTitle(jsonObject.has("title") ? ColorConverter.filterString(jsonObject.get("title").getAsString()) : "Empty Book");
            bookMeta.setAuthor(jsonObject.has("author") ? ColorConverter.filterString(jsonObject.get("author").getAsString()) : "Mojang");
            bookMeta.setGeneration(jsonObject.has("generation") ?
                    BookMeta.Generation.valueOf(Strings.getTechnicalName(jsonObject.get("generation").getAsString())) : BookMeta.Generation.ORIGINAL);

            if (jsonObject.has("pages")) { // Json pages
                try {
                    Field pagesField = Class.forName("org.bukkit.craftbukkit.v1_12_R1.inventory.CraftMetaBook").getDeclaredField("pages");
                    pagesField.setAccessible(true);

                    List<IChatBaseComponent> pages = (List<IChatBaseComponent>) pagesField.get(bookMeta);
                    jsonObject.getAsJsonArray("pages").forEach(jsonElement -> pages.add(IChatBaseComponent.ChatSerializer.a(ColorConverter.filterString(jsonElement.getAsString()))));

                    pagesField.setAccessible(false);
                } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                bookMeta.addPage("null");
            }

            itemStack.setItemMeta(bookMeta);
        }

        if (jsonObject.has("color")) {
            String[] rgb = jsonObject.get("color").getAsString().split(", ");

            if (material.name().contains("LEATHER_")) { // Leather armor
                LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
                leatherArmorMeta.setColor(Color.fromRGB(Integer.valueOf(rgb[0]), Integer.valueOf(rgb[1]), Integer.valueOf(rgb[2])));
                itemStack.setItemMeta(leatherArmorMeta);
                hasColor = true;
            }
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}
