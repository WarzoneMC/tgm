package network.warzone.tgm.util.menu;

import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.kit.classes.GameClassModule;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by yikes on 09/27/19
 */
public class ClassMenu extends Menu {

    private static ClassMenu classMenu;

    private ClassMenu(String name, int slots) {
        super(name, slots);
    }

    public static ClassMenu getClassMenu() {
        if (classMenu == null) {
            classMenu = new ClassMenu(ChatColor.UNDERLINE + "Classes!", 27);
            classMenu.setup();
        }
        return classMenu;
    }

    public static void destroyInstance() {
        classMenu = null;
    }

    // it can be assumed the current match is one that uses classes, due to when this is called
    @SuppressWarnings("unchecked")
    private void setup() {
        GameClassModule gameClassModule = TGM.get().getModule(GameClassModule.class);
        int start = 0;
        for (GameClassModule.GameClassStore gameClassStore : GameClassModule.GameClassStore.values()) {
            if (!gameClassModule.classSetHasInstance(gameClassStore.getHostGameClass())) continue;
            ItemStack item = gameClassStore.getMenuItem().clone();
            appendKitTitle(item);
            setItem(start, item, ((player, event) -> player.performCommand("class " + gameClassStore.name())));
            start++;
        }
    }

    private static void appendKitTitle(ItemStack inItem) {
        ItemMeta itemMeta = inItem.getItemMeta();
        if (itemMeta == null) return;
        String ogName = itemMeta.getDisplayName();
        inItem.getItemMeta().setDisplayName(ChatColor.GRAY + "Class " + ogName);
        inItem.setItemMeta(itemMeta);
    }

}
