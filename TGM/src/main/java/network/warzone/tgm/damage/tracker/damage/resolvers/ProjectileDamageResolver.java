package network.warzone.tgm.damage.tracker.damage.resolvers;

import network.warzone.tgm.damage.tracker.DamageInfo;
import network.warzone.tgm.damage.tracker.DamageResolver;
import network.warzone.tgm.damage.tracker.Lifetime;
import network.warzone.tgm.damage.tracker.damage.ProjectileDamageInfo;
import network.warzone.tgm.damage.tracker.trackers.ProjectileDistanceTracker;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ProjectileDamageResolver implements DamageResolver {
    private final ProjectileDistanceTracker projectileDistanceTracker;

    public ProjectileDamageResolver(ProjectileDistanceTracker projectileDistanceTracker) {
        this.projectileDistanceTracker = projectileDistanceTracker;
    }

    public @Nullable
    DamageInfo resolve(@Nonnull LivingEntity entity, @Nonnull Lifetime lifetime, @Nonnull EntityDamageEvent damageEvent) {
        if(damageEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) damageEvent;

            if(event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                Location launchLocation = this.projectileDistanceTracker.getLaunchLocation(projectile);
                Double projectileDistance = null;

                if(launchLocation != null) projectileDistance = event.getEntity().getLocation().distance(launchLocation);

                if(projectile.getShooter() instanceof LivingEntity) {
                    return new ProjectileDamageInfo(projectile, (LivingEntity) projectile.getShooter(), projectileDistance);
                }
            }
        }
        return null;
    }
}
