package network.warzone.tgm.util.menu;

import network.warzone.tgm.modules.kit.classes.GameClassModule;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yikes on 09/27/19
 */
public class ClassMenu extends Menu {
    private static ClassMenu classMenu;
    public static ClassMenu getClassMenu() {
        if (classMenu == null) {
            classMenu = new ClassMenu(ChatColor.UNDERLINE + "Classes!", 27);
            classMenu.setup();
        }
        return classMenu;
    }

    private ClassMenu(String name, int slots) {
        super(name, slots);
    }

    private void setup() {
        int start = 0;
        for(GameClassModule.GameClassStore gameClassStore : GameClassModule.GameClassStore.values()) {
            ItemStack item = gameClassStore.getMenuItem().clone();
            appendKitTitle(item);
            setItem(start, item, ((player, event) -> player.performCommand("kit " + gameClassStore.name())));
            start++;
        }
    }

    private static void appendKitTitle(ItemStack inItem) {
        ItemMeta itemMeta = inItem.getItemMeta();
        if (itemMeta == null) return;
        String ogName = itemMeta.getDisplayName();
        inItem.getItemMeta().setDisplayName(ChatColor.GRAY + "Kit " + ogName);
        inItem.setItemMeta(itemMeta);
    }
}
