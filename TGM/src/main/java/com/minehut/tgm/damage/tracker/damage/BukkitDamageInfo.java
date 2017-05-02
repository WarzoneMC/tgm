package com.minehut.tgm.damage.tracker.damage;

import com.google.common.base.Preconditions;
import com.minehut.tgm.damage.tracker.base.AbstractDamageInfo;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import javax.annotation.Nonnull;

public class BukkitDamageInfo extends AbstractDamageInfo {
    public BukkitDamageInfo(@Nonnull DamageCause cause) {
        super(null);

        Preconditions.checkNotNull(cause, "damage cause");

        this.cause = cause;
    }

    public @Nonnull
    DamageCause getCause() {
        return this.cause;
    }

    private final @Nonnull
    DamageCause cause;

    @Override
    public @Nonnull
    String toString() {
        return "BukkitDamageInfo{cause=" + this.cause + "}";
    }
}
