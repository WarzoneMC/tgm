package com.minehut.tgm.damage.tracker.damage;

import com.minehut.tgm.damage.tracker.base.AbstractDamageInfo;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OwnedMobDamageInfo extends AbstractDamageInfo {
    public OwnedMobDamageInfo(@Nullable LivingEntity resolvedDamager, @Nullable Player mobOwner, @Nullable Projectile projectile) {
        super(resolvedDamager);

        this.mobOwner = mobOwner;
        this.projectile = projectile;
    }

    public @Nullable
    Player getMobOwner() {
        return this.mobOwner;
    }

    public @Nullable
    Projectile getProjectile() {
        return this.projectile;
    }

    protected final @Nullable
    Player mobOwner;
    protected final @Nullable
    Projectile projectile;

    @Override
    public @Nonnull
    String toString() {
        return "OwnedMobDamageInfo{damager=" + this.resolvedDamager + ",mobOwner=" + this.mobOwner + ",projectile=" + this.projectile + "}";
    }
}
