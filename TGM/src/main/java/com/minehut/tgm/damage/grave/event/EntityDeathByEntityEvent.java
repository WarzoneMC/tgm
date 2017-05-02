package com.minehut.tgm.damage.grave.event;

import com.minehut.tgm.damage.tracker.Lifetime;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.joda.time.Instant;

import java.util.List;

@ToString(callSuper = true)
public class EntityDeathByEntityEvent<T extends LivingEntity> extends EntityDeathEvent {
    private final T cause;

    public EntityDeathByEntityEvent(Entity entity, Location location, Lifetime lifetime, Instant time, List<ItemStack> drops, int droppedExp, T cause) {
        super(entity, location, lifetime, time, drops, droppedExp);
        this.cause = cause;
    }

    public T getCause() {
        return this.cause;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
