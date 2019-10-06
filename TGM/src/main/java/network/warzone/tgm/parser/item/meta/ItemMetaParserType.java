package network.warzone.tgm.parser.item.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by Jorge on 09/14/2019
 */

@AllArgsConstructor @Getter
public enum ItemMetaParserType {

    DISPLAY_NAME(ItemDisplayNameParser.class),
    LORE(ItemLoreParser.class),
    DURABILITY(ItemDurabilityParser.class),
    ENCHANTMENTS(ItemEnchantmentsParser.class),
    UNBREAKABLE(ItemUnbreakableParser.class),
    FLAGS(ItemFlagParser.class),
    SKULL_OWNER(ItemSkullOwnerParser.class),
    POTION(ItemPotionParser.class),
    BOOK(ItemBookParser.class),
    COLOR(ItemColorParser.class);

    private Class<? extends ItemMetaParser> defaultParser;

    public ItemMetaParser newDefaultInstance() {
        try {
            return getDefaultParser().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
