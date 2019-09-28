package network.warzone.tgm.util.menu;

import network.warzone.tgm.modules.kit.legacy_kits.LegacyKitModule;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yikes on 09/27/19
 */
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
            appendCost(item, legacyKitStore.getCost());
            setItem(start, item, ((player, event) -> player.performCommand("kit " + legacyKitStore.name())));
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

    private static void appendCost(ItemStack inItem, int cost) {
        ItemMeta itemMeta = inItem.getItemMeta();
        if (itemMeta == null) return;
        List<String> ogLore = itemMeta.getLore();
        if (ogLore == null) ogLore = new ArrayList<>();
        ogLore.add("");
        ogLore.add(ChatColor.GRAY + "Cost: " + cost + " coins!");
        inItem.getItemMeta().setLore(ogLore);
        inItem.setItemMeta(itemMeta);
    }
}
