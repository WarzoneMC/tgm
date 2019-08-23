package network.warzone.tgm.modules;

import network.warzone.tgm.match.MatchModule;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class KnockbackModule extends MatchModule implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow) {
            Entity e = event.getEntity();
            Arrow arrow = (Arrow) event.getDamager();
            event.setDamage(event.getDamage() / 1.75);
            applyBowKnockback(arrow, e);
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getDamager() instanceof LivingEntity && event.getEntity() instanceof Player) {
            applyMeleeKnockback((LivingEntity) event.getDamager(), (Player) event.getEntity());
        }
    }

    @EventHandler
    public void onArrowsShoot(ProjectileLaunchEvent e) {
        Projectile projectile = e.getEntity();
        ProjectileSource shooter = projectile.getShooter();
        if (shooter instanceof Player) {
            Player player = (Player) shooter;
            projectile.setVelocity(player.getLocation().getDirection().normalize().multiply(projectile.getVelocity().length()));
        }
    }

    private void applyBowKnockback(Arrow a, Entity e) {
        Vector normalVelocity = a.getVelocity().normalize();
        normalVelocity.setY(normalVelocity.getY() / 3);
        e.setVelocity(normalVelocity.normalize().multiply(0.15f));
    }

    private void applyMeleeKnockback(LivingEntity attacker, Player victim) {
        Vector normalVelocity = attacker.getLocation().getDirection().normalize();
        normalVelocity.setY(normalVelocity.getY() / 3);
        victim.setVelocity(normalVelocity.normalize().multiply(0.65f));
    }

    private void addVelocity(Entity e, Vector v) {
        e.setVelocity(e.getVelocity().add(v));
    }

}
