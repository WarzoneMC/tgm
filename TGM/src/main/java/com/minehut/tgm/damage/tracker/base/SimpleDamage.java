package com.minehut.tgm.damage.tracker.base;

import com.google.common.base.Preconditions;
import com.minehut.tgm.damage.tracker.Damage;
import com.minehut.tgm.damage.tracker.DamageInfo;
import org.bukkit.Location;
import org.joda.time.Instant;

import javax.annotation.Nonnull;

public class SimpleDamage implements Damage {
    public SimpleDamage(int damage, @Nonnull Location location, @Nonnull Instant time, @Nonnull DamageInfo info) {
        Preconditions.checkArgument(damage >= 0, "damage must be greater than or equal to zero");
        Preconditions.checkNotNull(location, "location");
        Preconditions.checkNotNull(time, "time");
        Preconditions.checkNotNull(info, "info");

        this.damage = damage;
        this.location = location;
        this.time = time;
        this.info = info;
    }

    public int getDamage() {
        return this.damage;
    }

    public @Nonnull
    Location getLocation() {
        return this.location;
    }

    public @Nonnull
    Instant getTime() {
        return this.time;
    }

    public @Nonnull
    DamageInfo getInfo() {
        return this.info;
    }

    private final int damage;
    private final @Nonnull
    Location location;
    private final @Nonnull
    Instant time;
    private final @Nonnull
    DamageInfo info;
}
