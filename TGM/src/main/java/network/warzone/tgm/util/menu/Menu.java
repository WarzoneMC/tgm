package network.warzone.tgm.util.menu;

import lombok.Getter;
import network.warzone.tgm.TGM;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

/**
 * Created by katie on 2/14/17.
 */
public abstract class Menu implements Listener {

    @Getter private Inventory inventory;
    private String realTitle;

    private HashMap<Integer, MenuAction> actions = new HashMap<>();

    public Menu(String name, int slots) {
        this.inventory = Bukkit.createInventory(null, slots, name);
        this.realTitle = name;
        TGM.registerEvents(this);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().equals(this.inventory)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            if (this.actions.containsKey(event.getSlot())) {
                this.actions.get(event.getSlot()).run(player, event);
            }
        }
    }

    public void setItem(int slot, ItemStack itemStack, MenuAction action) {
        this.setItem(slot, itemStack);
        if (action != null) {
            this.actions.put(slot, action);
        }
    }

    public void setItem(int slot, ItemStack itemStack) {
        this.inventory.setItem(slot, itemStack);
    }

    public void clear() {
        this.inventory.clear();
        this.actions.clear();
    }

    public void disable() {
        HandlerList.unregisterAll(this);
    }
}
