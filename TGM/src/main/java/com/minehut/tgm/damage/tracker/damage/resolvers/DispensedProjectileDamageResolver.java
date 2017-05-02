package com.minehut.tgm.damage.tracker.damage.resolvers;

import com.minehut.tgm.damage.tracker.DamageInfo;
import com.minehut.tgm.damage.tracker.DamageResolver;
import com.minehut.tgm.damage.tracker.Lifetime;
import com.minehut.tgm.damage.tracker.damage.DispensedProjectileDamageInfo;
import com.minehut.tgm.damage.tracker.trackers.DispenserTracker;
import com.minehut.tgm.damage.tracker.trackers.ProjectileDistanceTracker;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DispensedProjectileDamageResolver implements DamageResolver {
    private final ProjectileDistanceTracker projectileDistanceTracker;
    private final DispenserTracker dispenserTracker;

    public DispensedProjectileDamageResolver(ProjectileDistanceTracker projectileDistanceTracker, DispenserTracker dispenserTracker) {
        this.projectileDistanceTracker = projectileDistanceTracker;
        this.dispenserTracker = dispenserTracker;
    }

    public @Nullable
    DamageInfo resolve(@Nonnull LivingEntity entity, @Nonnull Lifetime lifetime, @Nonnull EntityDamageEvent damageEvent) {
        if(damageEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) damageEvent;

            if(event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                Location launchLocation = this.projectileDistanceTracker.getLaunchLocation(projectile);
                Double projectileDistance = null;
                OfflinePlayer dispenserOwner = dispenserTracker.getOwner(projectile);


                if(launchLocation != null) projectileDistance = event.getEntity().getLocation().distance(launchLocation);

                if(projectile.getShooter() instanceof LivingEntity) {
                    return new DispensedProjectileDamageInfo(projectile, (LivingEntity) projectile.getShooter(), projectileDistance, dispenserOwner);
                }
            }
        }
        return null;
    }
}
