package network.warzone.tgm.parser.item.meta;

import com.google.gson.JsonObject;
import network.warzone.tgm.util.ColorConverter;
import network.warzone.tgm.util.Strings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Jorge on 09/14/2019
 */
public class ItemBookParser implements ItemMetaParser {

    //private static Class classIChatBaseComponent;
    private static Class classChatSerializer;
    private static Method methodA;

    private static Class classCraftMetaBook;
    private static Field fieldPages;

    static {
        try {
            //classIChatBaseComponent = Class.forName("net.minecraft.server.v1_16_R3.IChatBaseComponent");
            classChatSerializer = Class.forName("net.minecraft.server.v1_16_R3.IChatBaseComponent$ChatSerializer");
            methodA = classChatSerializer.getDeclaredMethod("a", String.class);

            classCraftMetaBook = Class.forName("org.bukkit.craftbukkit.v1_16_R3.inventory.CraftMetaBook");
            fieldPages = classCraftMetaBook.getDeclaredField("pages");

        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(ItemStack itemStack, ItemMeta meta, JsonObject object) {
        if (!itemStack.getType().equals(Material.WRITTEN_BOOK)) return;
        BookMeta bookMeta = (BookMeta) meta;

        bookMeta.setTitle(object.has("title") ? ColorConverter.filterString(object.get("title").getAsString()) : "Empty Book");
        bookMeta.setAuthor(object.has("author") ? ColorConverter.filterString(object.get("author").getAsString()) : "Mojang");
        bookMeta.setGeneration(object.has("generation") ?
                BookMeta.Generation.valueOf(Strings.getTechnicalName(object.get("generation").getAsString())) : BookMeta.Generation.ORIGINAL);

        if (object.has("pages")) { // Json pages
            try {
                fieldPages.setAccessible(true);

                List<Object> pages = (List<Object>) fieldPages.get(bookMeta);
                object.getAsJsonArray("pages").forEach(
                        jsonElement -> {
                            try {
                                String page = (String) methodA.invoke(ColorConverter.filterString(jsonElement.getAsString()));
                                pages.add(page);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                );

                fieldPages.setAccessible(false);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            bookMeta.addPage("null");
        }

        itemStack.setItemMeta(bookMeta);
    }
}
