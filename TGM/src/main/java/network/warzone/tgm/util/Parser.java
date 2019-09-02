package network.warzone.tgm.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.util.itemstack.ItemFactory;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static Location convertLocation(World world, JsonElement locationElement) {
        if (locationElement.isJsonObject()) {
            JsonObject locationJson = locationElement.getAsJsonObject();

            double x = locationJson.get("x").getAsDouble();
            double y = locationJson.get("y").getAsDouble();
            double z = locationJson.get("z").getAsDouble();
            float yaw = 0;
            if (locationJson.has("yaw")) {
                yaw = locationJson.get("yaw").getAsFloat();
            }
            float pitch = 0;
            if (locationJson.has("pitch")) {
                pitch = locationJson.get("pitch").getAsFloat();
            }
            return new Location(world, x, y, z, yaw, pitch);
        } else {
            String[] split = locationElement.getAsString().replaceAll(" ", "").split(",");

            double x = Double.valueOf(split[0].replaceAll("oo", Integer.toString(Integer.MAX_VALUE)));
            double y = Double.valueOf(split[1].replaceAll("oo", Integer.toString(Integer.MAX_VALUE)));
            double z = Double.valueOf(split[2].replaceAll("oo", Integer.toString(Integer.MAX_VALUE)));

            float yaw = 0;
            float pitch = 0;

            if (split.length >= 4) {
                yaw = Float.valueOf(split[3].replaceAll("oo", Integer.toString(Integer.MAX_VALUE)));
            }

            if (split.length >= 5) {
                pitch = Float.valueOf(split[4].replaceAll("oo", Integer.toString(Integer.MAX_VALUE)));
            }

            return new Location(world, x, y, z, yaw, pitch);
        }
    }

    public static ItemStack parseItemStack(JsonObject jsonObject) {
        Material material = Material.valueOf(Strings.getTechnicalName(jsonObject.get("material").getAsString()));
        ItemStack itemStack = ItemFactory.createItem(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

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
                Enchantment enchantment = Enchantment.getByName(Strings.getTechnicalName(split[0]));
                if (enchantment != null) {
                    itemMeta.addEnchant(enchantment, level, true);
                }
            }
        }

        if (jsonObject.has("durability")) {
            itemStack.setDurability(jsonObject.get("durability").getAsShort());
        }

        if (material.equals(Material.PLAYER_HEAD) && jsonObject.has("skullOwner")) {
            SkullMeta skullMeta = (SkullMeta) itemMeta;
            skullMeta.setOwner(jsonObject.get("skullOwner").getAsString());
            itemStack.setItemMeta(skullMeta);
        }


        if (material.name().contains("POTION") && jsonObject.has("potion")) {
            PotionMeta potionMeta = (PotionMeta) itemMeta;

            /*
            https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html
            "potion": {"effects": ["<effect>:<time> [amplifier]"]}
            */
            if (jsonObject.getAsJsonObject("potion").has("effects")) {
                for (JsonElement element : jsonObject.getAsJsonObject("potion").getAsJsonArray("effects")) {
                    String[] split = element.getAsString().replace(": ", ":").split(":");
                    PotionEffectType effectType = PotionEffectType.getByName(Strings.getTechnicalName(split[0]));
                    int duration;
                    int level = 0;
                    if (split[1].contains(" ")) {
                        duration = Integer.valueOf(split[1].split(" ")[0]); // ticks
                        level = Integer.valueOf(split[1].split(" ")[1]);
                    } else {
                        duration = Integer.valueOf(split[1]);
                    }
                    if (effectType != null) potionMeta.addCustomEffect(new PotionEffect(effectType, duration, level), true);
                }
            }

            /*
            Used to get potions that can be obtained from the Creative Inventory
            https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html
            "potion": {
                 "type": "<type>",
                 "extend": <boolean>,    Only used if "type" exists
                 "upgrade": <boolean>    Only used if "type" exists
            }
            */
            if (jsonObject.getAsJsonObject("potion").has("type")) {
                PotionType type = PotionType.valueOf(Strings.getTechnicalName(jsonObject.getAsJsonObject("potion").get("type").getAsString()));
                boolean extended = false;
                boolean upgraded = false;
                if (type == null) type = PotionType.UNCRAFTABLE;
                if (jsonObject.getAsJsonObject("potion").has("extend")) extended = jsonObject.getAsJsonObject("potion").get("type").getAsBoolean();
                if (jsonObject.getAsJsonObject("potion").has("upgrade")) upgraded = jsonObject.getAsJsonObject("potion").get("upgrade").getAsBoolean();
                potionMeta.setBasePotionData(new PotionData(type, extended, upgraded));
            }

            itemStack.setItemMeta(potionMeta);
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
                    Field pagesField = Class.forName("org.bukkit.craftbukkit.v1_14_R1.inventory.CraftMetaBook").getDeclaredField("pages");
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
            String[] rgb = jsonObject.get("color").getAsString().replace(" ", "").split(",");
            Color color = Color.fromRGB(Integer.valueOf(rgb[0]), Integer.valueOf(rgb[1]), Integer.valueOf(rgb[2]));
            if (material.name().contains("LEATHER_")) { // Leather armor
                LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
                leatherArmorMeta.setColor(color);
                itemStack.setItemMeta(leatherArmorMeta);
            } else if (material.name().contains("POTION")) {
                PotionMeta potionMeta = (PotionMeta) itemMeta;
                potionMeta.setColor(color);
                itemStack.setItemMeta(potionMeta);
            }
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static List<MatchTeam> getTeamsFromElement(TeamManagerModule teamManagerModule, JsonElement element) {
        List<MatchTeam> teams = new ArrayList<>();

        if (element.isJsonPrimitive()) {
            if (element.getAsString().equalsIgnoreCase("all")) {
                for (MatchTeam matchTeam : teamManagerModule.getTeams()) {
                    if (!matchTeam.isSpectator()) {
                        teams.add(matchTeam);
                    }
                }
            }
        } else {
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                MatchTeam matchTeam = teamManagerModule.getTeamById(jsonElement.getAsString());
                if (matchTeam != null) {
                    teams.add(matchTeam);
                }
            }
        }

        return teams;
    }

    /**
     * returns null if all materials are allowed
     */
    public static List<Material> getMaterialsFromElement(JsonElement element) {
        List<Material> materials = new ArrayList<>();

        if (element.isJsonPrimitive()) {
            if (element.getAsString().equalsIgnoreCase("all")) {
                return null;
            }
        } else {
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                materials.add(Material.valueOf(Strings.getTechnicalName(jsonElement.getAsString())));
            }
        }

        return materials;
    }
}
