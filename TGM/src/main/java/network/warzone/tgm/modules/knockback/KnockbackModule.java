package network.warzone.tgm.modules.knockback;

import network.warzone.tgm.match.MatchModule;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class KnockbackModule extends MatchModule implements Listener {

    @EventHandler(priority= EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            p.damage(event.getFinalDamage());
            applyKnockback(event.getDamager(), p);
            event.setCancelled(true);
        }
//        if (event.getDamager() instanceof Arrow) {
//            Entity e = event.getEntity();
//            Arrow arrow = (Arrow) event.getDamager();
//            event.setDamage(event.getDamage() / 1.75);
//            applyBowKnockback(arrow, e);
//        }
//
//        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getDamager() instanceof LivingEntity && event.getEntity() instanceof Player) {
//            applyMeleeKnockback((LivingEntity) event.getDamager(), (Player) event.getEntity());
//        }
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

    private void applyKnockback(Entity attacker, Player victim) {
        double d0 = attacker.getLocation().getX() - victim.getLocation().getX();

        double d1;

        for (d1 = attacker.getLocation().getZ() - victim.getLocation().getZ(); d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
            d0 = (Math.random() - Math.random()) * 0.01D;
        }

        double f1 = Math.sqrt(d0 * d0 + d1*d1);
        float f2 = 0.4F;

        Vector vel = victim.getVelocity();

        victim.setVelocity(new Vector((vel.getX() / 2.0F) - d0 / f1 * f2, (vel.getY() / 2.0F) + f2, (vel.getZ() / 2.0F) - d1 / f1 * f2));

        if (victim.getVelocity().getY() > 0.4000000059604645D) {
            victim.setVelocity(victim.getVelocity().setY(0.4000000059604645D));
        }
    }

    private void applyBowKnockback(Arrow a, Entity e) {
        Vector normalVelocity = a.getVelocity().normalize();
        normalVelocity.setY(normalVelocity.getY() / 3);
        e.setVelocity(normalVelocity.normalize().multiply(0.15f));
    }

    private void applyMeleeKnockback(LivingEntity attacker, Player victim) {
        Vector kb = attacker.getLocation().getDirection().setY(0).normalize().multiply(0.65f);
//        Vector normalVelocity = attacker.getLocation().getDirection().normalize();
//        normalVelocity.setY(normalVelocity.getY() / 3);
        victim.setVelocity(kb);
    }

}
