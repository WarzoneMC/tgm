package com.minehut.tgm.damage.tracker.plugin;

import com.minehut.tgm.damage.tracker.event.BlockDispenseEntityEvent;
import com.minehut.tgm.damage.tracker.trackers.DispenserTracker;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class DispenserListener implements Listener {
    private final DispenserTracker tracker;

    public DispenserListener(DispenserTracker tracker) {
        this.tracker = tracker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if(!this.tracker.isEnabled(event.getBlock().getWorld())) return;

        if(event.getBlock().getType() == Material.DISPENSER) {
            this.tracker.setPlacer(event.getBlock(), event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(!this.tracker.isEnabled(event.getBlock().getWorld())) return;

        this.tracker.clearPlacer(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(EntityExplodeEvent event) {
        if(!this.tracker.isEnabled(event.getLocation().getWorld())) return;

        // Remove all blocks that are destroyed from explosion
        for (Block block : event.blockList()) {
            this.tracker.clearPlacer(block);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispense(BlockDispenseEntityEvent event) {
        if(!this.tracker.isEnabled(event.getEntity().getWorld())) return;

        Block block = event.getBlock();
        OfflinePlayer placer = this.tracker.getPlacer(block);
        if(placer != null) {
            this.tracker.setOwner(event.getEntity(), placer.getPlayer());
        }
    }
}
