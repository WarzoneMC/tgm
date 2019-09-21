package network.warzone.tgm.parser.item;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.parser.item.meta.*;
import network.warzone.tgm.parser.item.tag.ItemAmountParser;
import network.warzone.tgm.parser.item.tag.ItemMaterialParser;
import network.warzone.tgm.parser.item.tag.ItemTagParser;
import network.warzone.tgm.util.itemstack.ItemFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jorge on 09/14/2019
 */
public class ItemParser implements JsonDeserializer<ItemStack> {

    @Getter @Setter static ItemTagParser<Material> materialParser = new ItemMaterialParser();
    @Getter @Setter static ItemTagParser<Integer> amountParser = new ItemAmountParser();

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
    }};

    public static ItemMetaParser getItemMetaParser(ItemMetaParserType type) {
        return metaParsers.get(type);
    }

    public static void setItemMetaParser(ItemMetaParserType type, ItemMetaParser parser) {
        metaParsers.put(type, parser);
    }

    public static ItemStack parse(JsonObject jsonObject) {
        Material material = materialParser.parse(jsonObject);
        int amount = amountParser.parse(jsonObject);
        ItemStack itemStack = ItemFactory.createItem(material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        for (ItemMetaParser itemMetaParser : metaParsers.values()) {
            itemMetaParser.parse(itemStack, meta, jsonObject);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        assert json.isJsonObject() : "JSON element is not a valid object for item deserializing.";
        return parse(json.getAsJsonObject());
    }

}
