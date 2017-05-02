package com.minehut.tgm.damage.tracker.damage.resolvers;

import com.minehut.tgm.damage.tracker.DamageInfo;
import com.minehut.tgm.damage.tracker.DamageResolver;
import com.minehut.tgm.damage.tracker.Lifetime;
import com.minehut.tgm.damage.tracker.damage.TNTDamageInfo;
import com.minehut.tgm.damage.tracker.trackers.DispenserTracker;
import com.minehut.tgm.damage.tracker.trackers.ExplosiveTracker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class TNTDamageResolver implements DamageResolver {
    private final ExplosiveTracker explosiveTracker;
    private final DispenserTracker dispenserTracker;

    public TNTDamageResolver(ExplosiveTracker explosiveTracker, DispenserTracker dispenserTracker) {
        this.explosiveTracker = explosiveTracker;
        this.dispenserTracker = dispenserTracker;
    }

    public @Nullable
    DamageInfo resolve(@Nonnull LivingEntity entity, @Nonnull Lifetime lifetime, @Nonnull EntityDamageEvent damageEvent) {
        if(damageEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) damageEvent;

            if(event.getDamager() instanceof TNTPrimed) {
                TNTPrimed tnt = (TNTPrimed) event.getDamager();
                Player owner = null;
                if(this.explosiveTracker.hasOwner(tnt)) {
                    owner = this.explosiveTracker.getOwner(tnt);
                } else if(this.dispenserTracker.hasOwner(tnt)) {
                    // getPlayer() is temporary to keep owner uniform until other services use OfflinePlayer
                    owner = this.dispenserTracker.getOwner(tnt).getPlayer();
                }

                return new TNTDamageInfo(tnt, owner);
            }
        }

        return null;
    }
}
