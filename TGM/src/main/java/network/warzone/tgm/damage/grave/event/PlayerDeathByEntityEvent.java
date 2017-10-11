package network.warzone.tgm.damage.grave.event;

import network.warzone.tgm.damage.tracker.Lifetime;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.joda.time.Instant;

import java.util.List;

@ToString(callSuper = true)
public class PlayerDeathByEntityEvent<T extends LivingEntity> extends PlayerDeathEvent {
    private final T cause;

    public PlayerDeathByEntityEvent(Player player, Location location, Lifetime lifetime, Instant time, List<ItemStack> drops, int droppedExp, T cause) {
        super(player, location, lifetime, time, drops, droppedExp);
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
