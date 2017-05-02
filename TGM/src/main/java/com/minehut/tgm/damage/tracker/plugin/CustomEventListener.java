package com.minehut.tgm.damage.tracker.plugin;

import com.minehut.tgm.damage.tracker.event.BlockDispenseEntityEvent;
import com.minehut.tgm.damage.tracker.event.BlockFallEvent;
import com.minehut.tgm.damage.tracker.event.PlayerCoarseMoveEvent;
import com.minehut.tgm.damage.tracker.util.EventUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class CustomEventListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFallCall(EntitySpawnEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            FallingBlock fallingBlock = (FallingBlock) event.getEntity();

//            Block block = fallingBlock.getSourceLoc().getBlock();
            Block block = fallingBlock.getLocation().getBlock();
            //todo: verify this was a suitable fix.


            BlockFallEvent call = new BlockFallEvent(block, fallingBlock);

            for (EventPriority priority : EventPriority.values())
                EventUtil.callEvent(call, BlockFallEvent.getHandlerList(), priority);
            call.setCancelled(event.isCancelled());

            if (call.isCancelled()) {
                block.setType(fallingBlock.getMaterial());
                block.setData(fallingBlock.getBlockData());
                fallingBlock.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDispenseEntityCall(EntitySpawnEvent event) {
        Block block = event.getLocation().getBlock();

        if (block.getType() == Material.DISPENSER) {
            BlockDispenseEntityEvent call = new BlockDispenseEntityEvent(block, event.getEntity());
            call.setCancelled(event.isCancelled());

            for (EventPriority priority : EventPriority.values())
                EventUtil.callEvent(call, BlockDispenseEntityEvent.getHandlerList(), priority);

            event.setCancelled(call.isCancelled());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCoarseMoveCall(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX())
            if (from.getBlockY() == to.getBlockY())
                if (from.getBlockZ() == to.getBlockZ())
                    return;

        PlayerCoarseMoveEvent call = new PlayerCoarseMoveEvent(event.getPlayer(), from, to);
        call.setCancelled(event.isCancelled());

        for (EventPriority priority : EventPriority.values())
            EventUtil.callEvent(call, PlayerCoarseMoveEvent.getHandlerList(), priority);

        event.setCancelled(call.isCancelled());
        event.setFrom(call.getFrom());
        event.setTo(call.getTo());
    }
}
