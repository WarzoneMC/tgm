package network.warzone.tgm.player.event;

import lombok.Getter;
import network.warzone.tgm.modules.death.DeathInfo;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by MatrixTunnel on 2/11/2018.
 */
@Getter
public class TGMPlayerDeathEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Player victim;
    private Location deathLocation;
    private Player killer;
    private EntityDamageEvent.DamageCause cause;
    private ItemStack murderWeapon;

    private List<ItemStack> drops;
    private DeathInfo deathInfo;

    private boolean cancelled = false;

    public TGMPlayerDeathEvent(Player victim, Location deathLocation, Player killer, EntityDamageEvent.DamageCause cause, ItemStack murderWeapon, List<ItemStack> drops, DeathInfo deathInfo) {
        super(false);

        this.victim = victim;
        this.deathLocation = deathLocation;
        this.killer = killer;
        this.cause = cause;
        this.murderWeapon = murderWeapon;
        this.drops = drops;
        this.deathInfo = deathInfo;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
