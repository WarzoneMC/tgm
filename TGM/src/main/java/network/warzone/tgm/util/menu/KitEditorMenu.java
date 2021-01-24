package network.warzone.tgm.util.menu;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.kit.Kit;
import network.warzone.tgm.modules.kit.KitNode;
import network.warzone.tgm.modules.kit.types.ItemKitNode;
import network.warzone.tgm.util.itemstack.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Atdit on 24/04/2020
 */
public class KitEditorMenu implements Listener {
    @Getter private final Inventory inventory;

    @Getter
    private Kit kit;

    private final HashMap<Integer, MenuAction> actions = new HashMap<>();

    public KitEditorMenu(List<Kit> kits, String mapName) {
        String name = ChatColor.UNDERLINE + "Kit Editor:" + ChatColor.RESET + " " + mapName;
        int slots = 5 * 9;

        this.inventory = Bukkit.createInventory(null, slots, name);
        TGM.registerEvents(this);

        kit = kits.get(0); // Only runs if there is one kit

        ItemStack saveItem = ItemFactory.createItem(Material.LIME_TERRACOTTA, ChatColor.GREEN + "Save");
        setItem(0, saveItem, (player, event) -> {
            saveKit();
            player.sendMessage(ChatColor.GREEN + "Saved kit!");
            player.closeInventory();
        });

        ItemStack cancelItem = ItemFactory.createItem(Material.RED_TERRACOTTA, ChatColor.RED + "Cancel");
        setItem(1, cancelItem, (player, event) -> {
            player.closeInventory();
        });
    }

    public void open(Player player) {
        loadKit();
        player.openInventory(inventory);
    }

    private void saveKit() {
        List<KitNode> kitNodes = new ArrayList<>();

        List<ItemKitNode> itemKitNodes = new ArrayList<>();

        for (int slot = 9; slot < 45; slot++) {
            ItemStack itemStack = inventory.getItem(slot);

            if (itemStack != null) {
                ItemKitNode itemKitNode = new ItemKitNode((slot < 36 ? slot : slot - 36), itemStack, false);
                itemKitNodes.add(itemKitNode);
            }
        }

        for (KitNode kitNode : kit.getNodes()) {
            if (!(kitNode instanceof ItemKitNode)) {
                kitNodes.add(kitNode);
            } else {
                ItemKitNode itemKitNode = (ItemKitNode) kitNode;
                if (itemKitNode.getSlot() < 0 || itemKitNode.getSlot() > 35) {
                    kitNodes.add(itemKitNode);
                }
            }
        }

        kitNodes.addAll(itemKitNodes);

        kit = new Kit(kit.getName(), kit.getDescription(), kitNodes);
    }

    private void loadKit() {
        clearKit();

        kit.getNodes().forEach(kitNode -> {
            if (kitNode instanceof ItemKitNode) {
                ItemKitNode itemKitNode = (ItemKitNode) kitNode;

                int slot = itemKitNode.getSlot();

                if (slot >= 0 && slot <= 8) {
                    setItem(slot + 36, itemKitNode.getItemStack());
                } else if (slot >= 9 && slot <= 35) {
                    setItem(slot, itemKitNode.getItemStack());
                }
            }
        });
    }

    private void setItem(int slot, ItemStack itemStack, MenuAction action) {
        this.setItem(slot, itemStack);
        if (action != null) {
            this.actions.put(slot, action);
        }
    }

    private void setItem(int slot, ItemStack itemStack) {
        this.inventory.setItem(slot, itemStack);
    }

    private void clearKit() {
        for (int slot = 9; slot < 45; slot++) {
            inventory.setItem(slot, null);
        }
    }

    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        Player player = (Player) event.getWhoClicked();

        if (event.getRawSlot() > 44) {
            event.setCancelled(true);
        }

        if (event.getRawSlot() <= 8) {
            event.setCancelled(true);

            if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) {
                if (this.actions.containsKey(event.getSlot())) {
                    this.actions.get(event.getSlot()).run(player, event);
                }
            } else {
                player.sendMessage(ChatColor.RED + "You can't place the item here.");
            }
            return;
        }

        if (event.getAction() != InventoryAction.PICKUP_ALL && event.getAction() != InventoryAction.PLACE_ALL && event.getAction() != InventoryAction.SWAP_WITH_CURSOR) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        if (event.getRawSlots().size() > 1) {
            event.setCancelled(true);
        }

        for (int slot : event.getRawSlots()) {
            if (slot < 9 || slot > 44) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        Bukkit.getScheduler().runTask(TGM.get(), () -> {
            event.getPlayer().getInventory().clear(0);
        });
    }
}
