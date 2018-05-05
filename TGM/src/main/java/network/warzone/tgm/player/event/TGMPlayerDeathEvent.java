package network.warzone.tgm.player.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by MatrixTunnel on 2/11/2018.
 */
@Getter
public class TGMPlayerDeathEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Player victim;
    private Player killer;
    private EntityDamageEvent.DamageCause cause;
    private ItemStack murderWeapon;

    private boolean cancelled = false;

    public TGMPlayerDeathEvent(Player victim, Player killer, EntityDamageEvent.DamageCause cause, ItemStack murderWeapon) {
        super(false);

        this.victim = victim;
        this.killer = killer;
        this.cause = cause;
        this.murderWeapon = murderWeapon;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
