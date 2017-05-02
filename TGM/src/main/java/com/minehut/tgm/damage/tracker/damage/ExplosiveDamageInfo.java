package com.minehut.tgm.damage.tracker.damage;

import com.google.common.base.Preconditions;
import com.minehut.tgm.damage.tracker.base.AbstractDamageInfo;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExplosiveDamageInfo extends AbstractDamageInfo {

    public ExplosiveDamageInfo(@Nonnull Explosive explosive, @Nullable LivingEntity resolvedDamager) {
        super(resolvedDamager);
        this.explosive = Preconditions.checkNotNull(explosive);
    }

    public @Nonnull
    Explosive getExplosive() {
        return this.explosive;
    }

    protected Explosive explosive;

    @Override
    public @Nonnull
    String toString() {
        return "ExplosiveDamageInfo{explosive=" + this.explosive + ",damager=" + this.resolvedDamager + "}";
    }
}
