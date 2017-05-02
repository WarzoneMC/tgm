package com.minehut.tgm.damage.tracker.event;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerSpawnEntityEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel;
    private final Entity what;
    private final ItemStack item;
    private Location location;

    public PlayerSpawnEntityEvent(final Player who, final Entity what, final Location location, final ItemStack item) {
        super(who);
        this.cancel = false;
        this.what = what;
        this.item = item;
        this.location = location;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * Gets the entity the player is spawning.
     *
     * @return the entity the player is spawning
     */
    public Entity getEntity() {
        return what;
    }

    /**
     * Gets the com.minehut.tabbed.generator that is being used to spawn an entity.
     * Modifying the returned com.minehut.tabbed.generator will have no effect.
     *
     * @return an ItemStack for the com.minehut.tabbed.generator being used
     */
    public ItemStack getItem() {
        return item.clone();
    }

    /**
     * Returns the location and rotation of where the entity is being spawned.
     *
     * @return The location and rotation of the entity
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location and rotation of where the entity is being spawned.
     *
     * @param location The location and rotation of the entity
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}