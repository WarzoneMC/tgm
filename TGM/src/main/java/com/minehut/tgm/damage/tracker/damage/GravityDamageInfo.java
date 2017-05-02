package com.minehut.tgm.damage.tracker.damage;

import com.google.common.base.Preconditions;
import com.minehut.tgm.damage.tracker.base.AbstractDamageInfo;
import com.minehut.tgm.damage.tracker.trackers.base.gravity.Fall;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GravityDamageInfo extends AbstractDamageInfo {
    public GravityDamageInfo(@Nullable LivingEntity resolvedDamager, @Nonnull Fall.Cause cause, @Nonnull Fall.From from) {
        super(resolvedDamager);

        Preconditions.checkNotNull(resolvedDamager, "damager");
        Preconditions.checkNotNull(cause, "cause");
        Preconditions.checkNotNull(from, "from");

        this.cause = cause;
        this.from = from;
    }

    public @Nonnull
    Fall.Cause getCause() {
        return this.cause;
    }

    public @Nonnull
    Fall.From getFrom() {
        return this.from;
    }

    private final @Nonnull
    Fall.Cause cause;
    private final @Nonnull
    Fall.From from;

    @Override
    public @Nonnull
    String toString() {
        return "GravityDamageInfo{damager=" + this.resolvedDamager + ",cause=" + this.cause + ",from=" + this.from + "}";
    }
}
