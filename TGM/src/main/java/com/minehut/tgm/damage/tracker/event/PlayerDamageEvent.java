package com.minehut.tgm.damage.tracker.event;

import com.minehut.tgm.damage.tracker.DamageInfo;
import com.minehut.tgm.damage.tracker.Lifetime;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.joda.time.Instant;

import javax.annotation.Nonnull;

public class PlayerDamageEvent extends EntityDamageEvent<Player> {
    public PlayerDamageEvent(@Nonnull Player player, @Nonnull Lifetime lifetime, int damage, @Nonnull Location location, @Nonnull Instant time, @Nonnull DamageInfo info) {
        super(player, lifetime, damage, location, time, info);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
