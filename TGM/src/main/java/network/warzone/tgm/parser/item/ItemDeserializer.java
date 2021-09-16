package network.warzone.tgm.parser.item;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.parser.item.meta.ItemMetaParser;
import network.warzone.tgm.parser.item.meta.ItemMetaParserType;
import network.warzone.tgm.parser.item.tag.ItemAmountParser;
import network.warzone.tgm.parser.item.tag.ItemMaterialParser;
import network.warzone.tgm.parser.item.tag.ItemTagParser;
import network.warzone.tgm.util.Strings;
import network.warzone.tgm.util.itemstack.ItemFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jorge on 09/14/2019
 */
public class ItemDeserializer implements JsonDeserializer<ItemStack> {

    @Getter @Setter private static ItemTagParser<Material> materialParser = new ItemMaterialParser();
    @Getter @Setter private static ItemTagParser<Integer> amountParser = new ItemAmountParser();

    private static Map<ItemMetaParserType, ItemMetaParser> metaParsers = new HashMap<ItemMetaParserType, ItemMetaParser>() {{
        put(ItemMetaParserType.DISPLAY_NAME, ItemMetaParserType.DISPLAY_NAME.newDefaultInstance());
        put(ItemMetaParserType.LORE,         ItemMetaParserType.LORE.newDefaultInstance());
        put(ItemMetaParserType.DURABILITY,   ItemMetaParserType.DURABILITY.newDefaultInstance());
        put(ItemMetaParserType.ENCHANTMENTS, ItemMetaParserType.ENCHANTMENTS.newDefaultInstance());
        put(ItemMetaParserType.UNBREAKABLE,  ItemMetaParserType.UNBREAKABLE.newDefaultInstance());
        put(ItemMetaParserType.FLAGS,        ItemMetaParserType.FLAGS.newDefaultInstance());
        put(ItemMetaParserType.SKULL_OWNER,  ItemMetaParserType.SKULL_OWNER.newDefaultInstance());
        put(ItemMetaParserType.POTION,       ItemMetaParserType.POTION.newDefaultInstance());
        put(ItemMetaParserType.BOOK,         ItemMetaParserType.BOOK.newDefaultInstance());
        put(ItemMetaParserType.COLOR,        ItemMetaParserType.COLOR.newDefaultInstance());
        put(ItemMetaParserType.CAN_PLACE_ON, ItemMetaParserType.CAN_PLACE_ON.newDefaultInstance());
        put(ItemMetaParserType.CAN_DESTROY,  ItemMetaParserType.CAN_DESTROY.newDefaultInstance());
    }};

    private static List<ItemMetaParser> extraParsers = new ArrayList<>();

    public static ItemMetaParser getItemMetaParser(ItemMetaParserType type) {
        return metaParsers.get(type);
    }

    public static void setItemMetaParser(ItemMetaParserType type, ItemMetaParser parser) {
        metaParsers.put(type, parser);
    }

    public static void addExtraParser(ItemMetaParser itemMetaParser) {
        extraParsers.add(itemMetaParser);
    }

    public static void removeExtraParser(ItemMetaParser itemMetaParser) {
        extraParsers.remove(itemMetaParser);
    }

    public static ItemStack parse(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            Material material = Material.valueOf(Strings.getTechnicalName(jsonElement.getAsString()));
            return ItemFactory.createItem(material);
        } else {
            Material material = materialParser.parse(jsonElement.getAsJsonObject());
            int amount = amountParser.parse(jsonElement.getAsJsonObject());
            ItemStack itemStack = ItemFactory.createItem(material, amount);
            ItemMeta meta = itemStack.getItemMeta();
            for (ItemMetaParser itemMetaParser : metaParsers.values()) {
                itemMetaParser.parse(itemStack, meta, jsonElement.getAsJsonObject());
            }
            itemStack.setItemMeta(meta);
            return itemStack;
        }
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return parse(json);
    }

}
