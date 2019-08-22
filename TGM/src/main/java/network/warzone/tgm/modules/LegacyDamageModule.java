package network.warzone.tgm.modules;

import network.warzone.tgm.match.MatchModule;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

/**
 * Reverts 1.9's damage values to those of 1.8
 */
public class LegacyDamageModule extends MatchModule implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if ((event.getDamager() instanceof Player)) {
            Player player = (Player) event.getDamager();
            switch (player.getInventory().getItemInMainHand().getType()) {
                case DIAMOND_AXE:
                    event.setDamage(event.getDamage() - 6.0D + 3.0D);
                    break;
                case IRON_AXE:
                    event.setDamage(event.getDamage() - 6.0D + 2.5D);
                    break;
                case STONE_AXE:
                    event.setDamage(event.getDamage() - 6.0D + 2.0D);
                    break;
                case GOLDEN_AXE:
                case WOODEN_AXE:
                    event.setDamage(event.getDamage() - 4.0D + 1.5D);
                    break;
                case DIAMOND_SHOVEL:
                    event.setDamage(event.getDamage() - 2.5D + 2.0D);
                    break;
                case STONE_SHOVEL:
                    event.setDamage(event.getDamage() - 0.75D + 1.25D);
                    break;
            }
        }
        if (event.getDamager() instanceof Arrow) {
            Entity e = event.getEntity();
            Arrow arrow = (Arrow) event.getDamager();
            event.setDamage(5);
            e.setVelocity(arrow.getVelocity().normalize().multiply(1f));

        }

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getDamager() instanceof LivingEntity && event.getEntity() instanceof Player) {
            applyKnockback((LivingEntity) event.getDamager(), (Player) event.getEntity());
        }
    }

    private void applyKnockback(LivingEntity attacker, Player victim) {
        Vector normal = victim.getLocation().subtract(attacker.getLocation()).toVector();
        normal = normal.normalize();

        Vector victimNormal = magicKnockbackFunction(normal);

        final boolean ground = attacker.isOnGround();
        final double attackSpeed = Math.max(0, attacker.getVelocity().dot(normal));
        final boolean sprint = ground && attackSpeed > 9;

        victim.setVelocity(victimNormal.multiply(sprint ? 1 : 0.85));
    }

    private Vector magicKnockbackFunction(Vector delta) {
        delta = delta.clone();
        delta.setY(0);
        delta.normalize();
        final double theta = Math.toRadians(0);
        final double cos = Math.cos(theta);
        delta.setX(cos * delta.getX());
        delta.setY(Math.sin(theta));
        delta.setZ(cos * delta.getZ());
        return delta;
    }
}
