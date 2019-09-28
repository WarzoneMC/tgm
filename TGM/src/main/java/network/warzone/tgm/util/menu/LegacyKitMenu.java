package network.warzone.tgm.util.menu;

import network.warzone.tgm.modules.kit.legacy_kits.LegacyKitModule;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LegacyKitMenu extends Menu {
    private static LegacyKitMenu legacyKitMenu;
    public static LegacyKitMenu getLegacyKitMenu() {
        if (legacyKitMenu == null) {
            legacyKitMenu = new LegacyKitMenu(ChatColor.UNDERLINE + "Kits!", 27);
            legacyKitMenu.setup();
        }
        return legacyKitMenu;
    }

    private LegacyKitMenu(String name, int slots) {
        super(name, slots);
    }

    private void setup() {
        int start = 0;
        for(LegacyKitModule.LegacyKitStore legacyKitStore : LegacyKitModule.LegacyKitStore.values()) {
            ItemStack item = legacyKitStore.getMenuItem().clone();
            appendKitTitle(item);
            setItem(start, item, ((player, event) -> player.performCommand("kit " + legacyKitStore.name())));
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
