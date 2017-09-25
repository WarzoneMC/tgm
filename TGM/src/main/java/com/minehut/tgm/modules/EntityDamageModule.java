package com.minehut.tgm.modules;

import com.minehut.tgm.match.MatchModule;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Created by Jorge on 9/24/2017.
 */
public class EntityDamageModule extends MatchModule implements Listener {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType() != EntityType.FISHING_HOOK &&
            event.getEntityType() != EntityType.SNOWBALL &&
            event.getEntityType() != EntityType.EGG) return;
        ProjectileSource shooter = ((Projectile) event.getEntity()).getShooter();
        if (shooter != null && shooter instanceof Player && event.getHitEntity() instanceof Damageable) {
            ((Damageable) event.getHitEntity()).damage(0.01, (Player) shooter);
        }

    }

}
