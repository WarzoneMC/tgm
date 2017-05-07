package com.minehut.tgm.modules.wool;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WoolChestModule extends MatchModule implements Listener {
    @Getter
    private final List<Inventory> scannedChests = new ArrayList<>();

    @Getter
    private final HashMap<Inventory, DyeColor> woolChests = new HashMap<>();

    private int runnableId = -1;

    @Override
    public void load(Match match) {
        runnableId = Bukkit.getScheduler().scheduleSyncRepeatingTask(TGM.get(), new Runnable() {
            @Override
            public void run() {
                for (Inventory inventory : woolChests.keySet()) {
                    fillInventoryWithWool(inventory, woolChests.get(inventory));
                }
            }
        }, 10 * 20L, 10 * 20L);
    }

    @Override
    public void unload() {
        Bukkit.getScheduler().cancelTask(runnableId);
    }


    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        registerInventory(event.getInventory());
    }

    private void fillInventoryWithWool(Inventory inventory, DyeColor dyeColor) {
        ItemStack itemStack = new ItemStack(Material.WOOL);
        itemStack.setData(new Wool(dyeColor));

        for(int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, itemStack);
        }
    }

    private void registerInventory(Inventory inventory) {
        if (scannedChests.contains(inventory)) {
            return;
        }
        scannedChests.add(inventory);

        for (ItemStack itemStack : inventory) {
            if (itemStack.getType() == Material.WOOL) {
                DyeColor dyeColor = ((Wool) itemStack.getData()).getColor();
                woolChests.put(inventory, dyeColor);
                fillInventoryWithWool(inventory, dyeColor);
            }
        }
    }
}
