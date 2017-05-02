package com.minehut.tgm.damage.tracker.trackers.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.minehut.tgm.damage.tracker.base.AbstractTracker;
import com.minehut.tgm.damage.tracker.trackers.ProjectileDistanceTracker;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Projectile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleProjectileDistanceTracker extends AbstractTracker implements ProjectileDistanceTracker {
    private final HashMap<Projectile, Location> projectileLaunchLocations = Maps.newHashMap();

    public boolean hasLaunchLocation(@Nonnull Projectile projectile) {
        Preconditions.checkNotNull(projectile, "projectile entity");

        return this.projectileLaunchLocations.containsKey(projectile);
    }

    public @Nullable
    Location getLaunchLocation(@Nonnull Projectile projectile) {
        Preconditions.checkNotNull(projectile, "projectile entity");

        return this.projectileLaunchLocations.get(projectile);
    }

    public @Nullable
    Location setLaunchLocation(@Nonnull Projectile projectile, @Nullable Location location) {
        Preconditions.checkNotNull(projectile, "projectile entity");

        if(location != null) {
            return this.projectileLaunchLocations.put(projectile, location);
        } else {
            return this.projectileLaunchLocations.remove(projectile);
        }
    }

    public void clear(World world) {
        // clear information about projectile launch locations in that world
        Iterator<Map.Entry<Projectile, Location>> projectileIt = this.projectileLaunchLocations.entrySet().iterator();
        while(projectileIt.hasNext()) {
            Projectile projectile = projectileIt.next().getKey();
            if(projectile.getWorld().equals(world)) {
                projectileIt.remove();
            }
        }
    }
}
