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
            event.setDamage(event.getDamage() / 4);
            addVelocity(e, arrow.getVelocity().normalize().multiply(0.65f));
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getDamager() instanceof LivingEntity && event.getEntity() instanceof Player) {
            applyKnockback((LivingEntity) event.getDamager(), (Player) event.getEntity());
        }
    }

    @EventHandler
    public void onArrowsShoot(ProjectileLaunchEvent e) {
        Projectile projectile = e.getEntity(); //Getting the projectile
        ProjectileSource shooter = projectile.getShooter(); //Getting the shooter
        if (shooter instanceof Player) { //If the shooter was a player
            Player player = (Player) shooter;
            //Here we get a unit vector of the direction the player is looking in and multiply it by the projectile's vector's magnitude
            //We then assign this to the projectile as its new velocity
            projectile.setVelocity(player.getLocation().getDirection().normalize().multiply(projectile.getVelocity().length()));
        }
    }

    private void applyKnockback(LivingEntity attacker, Player victim) {
        Vector kb = attacker.getLocation().getDirection().setY(0).normalize().multiply(0.65f);
        victim.setVelocity(kb.setY(0.111));
    }

    private void addVelocity(Entity e, Vector v) {
        e.setVelocity(e.getVelocity().add(v));
    }

}
