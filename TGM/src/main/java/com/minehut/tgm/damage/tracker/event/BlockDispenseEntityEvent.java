package com.minehut.tgm.damage.tracker.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

/**
 * Called when an entity is dispensed from a block.
 */
public class BlockDispenseEntityEvent extends BlockEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private boolean cancelled;

    public BlockDispenseEntityEvent(final Block block, final Entity entity) {
        super(block);
        this.entity = entity;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Gets the entity that is being dispensed.
     *
     * @return An Entity for the com.minehut.tabbed.generator being dispensed
     */
    public Entity getEntity() {
        return entity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}