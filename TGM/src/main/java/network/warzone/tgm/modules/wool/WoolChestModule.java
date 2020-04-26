package network.warzone.tgm.modules.wool;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

@Getter
public class WoolChestModule extends MatchModule implements Listener {

    private final HashMap<InventoryHolder, ItemStack> woolChests = new HashMap<>();

    private int runnableId = -1;

    @Override
    public void load(Match match) {
        runnableId = Bukkit.getScheduler().runTaskTimer(TGM.get(), () ->
                woolChests.forEach((inventory, color) ->
                        fillInventoryWithWool(inventory.getInventory(), color)
                ), 1L, 1L).getTaskId();
    }

    @Override
    public void unload() {
        Bukkit.getScheduler().cancelTask(runnableId);
        woolChests.clear();
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.getInventory().getLocation() == null) return;
        BlockState state = event.getInventory().getLocation().getBlock().getState();

        if (state instanceof Chest) {
            Chest chest = (Chest) event.getInventory().getLocation().getBlock().getState();

            if (!isWoolChest(chest)) {
                registerInventory(event.getInventory());
            }
        } else if (state instanceof DoubleChest) {
            DoubleChest chest = (DoubleChest) event.getInventory().getLocation().getBlock().getState();

            if (!isWoolChest(chest.getRightSide())) {
                registerInventory(chest.getRightSide().getInventory());
            }

            if (!isWoolChest(chest.getLeftSide())) {
                registerInventory(chest.getLeftSide().getInventory());
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.CHEST) {
            BlockState state = event.getBlock().getLocation().getBlock().getState();

            if (state instanceof Chest) {
                Chest chest = (Chest) state;

                if (isWoolChest(chest)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "You cannot break the wool chest!");
                }
            } else if (state instanceof DoubleChest) {
                DoubleChest chest = (DoubleChest) state;

                if (isWoolChest(chest.getRightSide()) || isWoolChest(chest.getLeftSide())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "You cannot break the wool chest!");
                }
            }
        }
    }

    private boolean isWoolChest(InventoryHolder holder) {
        return woolChests.getOrDefault(holder, null) != null;
    }

    private void fillInventoryWithWool(Inventory inventory, ItemStack woolFiller) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, woolFiller);
        }
    }

    private void registerInventory(Inventory inventory) {
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && itemStack.getData() != null &&
                    itemStack.getType().name().contains("WOOL")) {
                woolChests.put(inventory.getHolder(), itemStack);
                fillInventoryWithWool(inventory, itemStack);
                return;
            }
        }
    }
}
