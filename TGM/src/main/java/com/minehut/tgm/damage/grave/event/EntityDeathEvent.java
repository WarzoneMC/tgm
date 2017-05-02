package com.minehut.tgm.damage.grave.event;

import com.minehut.tgm.damage.tracker.Lifetime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;
import org.joda.time.Instant;

import java.util.List;

@ToString(callSuper = true)
public class EntityDeathEvent extends EntityEvent {
    @Getter
    private final Location location;
    @Getter
    private final Lifetime lifetime;
    @Getter
    private final Instant time;
    @Getter
    @Setter
    private List<ItemStack> drops;
    @Getter
    @Setter
    private int droppedExp;

    public EntityDeathEvent(Entity entity, Location location, Lifetime lifetime, Instant time, List<ItemStack> drops, int droppedExp) {
        super(entity);
        this.entity = entity;
        this.location = location;
        this.lifetime = lifetime;
        this.time = time;
        this.drops = drops;
        this.droppedExp = droppedExp;
    }

    protected static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

}
