package network.warzone.tgm.modules;

import network.warzone.tgm.match.MatchModule;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
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
            event.setDamage(event.getDamage() / 3);
            addVelocity(e, arrow.getVelocity().normalize().multiply(0.85f));
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getDamager() instanceof LivingEntity && event.getEntity() instanceof Player) {
            applyKnockback((LivingEntity) event.getDamager(), (Player) event.getEntity());
        }
    }

    private void applyKnockback(LivingEntity attacker, Player victim) {
        //Vector kb = attacker.getLocation().getDirection().setY(0).normalize().multiply(0.65f);
        //victim.setVelocity(magicKnockbackFunction(kb));
        victim.setVelocity(magicKnockbackFunction(attacker.getLocation().getDirection()));
    }

    private void addVelocity(Entity e, Vector vel) {
        e.setVelocity(e.getVelocity().add(vel));
    }

    private void applyClijmartKnockback(LivingEntity damaged, Entity damager) {
        double f = 0.5f;
        if (damager instanceof Player) {
            f += 0.3375 * ((Player) damager).getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.KNOCKBACK);
        }
        if (damager instanceof Arrow) {
            f += 0.3375 * ((Arrow) damager).getKnockbackStrength();
        }
        f *= Math.max(0, 1 - damaged.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getValue());
        double d0 = damager instanceof Projectile ? -damager.getVelocity().getX() : damager.getLocation().getX() - damaged.getLocation().getX();
        double d1 = damager instanceof Projectile ? -damager.getVelocity().getZ() : damager.getLocation().getZ() - damaged.getLocation().getZ();

        double f1 = Math.sqrt(d0 * d0 + d1 * d1);
        double motX = damaged.getVelocity().getX();
        double motY = damaged.getVelocity().getY();
        double motZ = damaged.getVelocity().getZ();
        motX /= 2;
        motY /= 2;
        motZ /= 2;
        motX -= d0 / f1 * f;
        motY += f;
        motZ -= d1 / f1 * f;
        motY = Math.min(0.5, motY);
        damaged.setVelocity(new Vector(Double.isFinite(motX) ? motX : 0, Double.isFinite(motY) ? motY : 0, Double.isFinite(motZ) ? motZ : 0));
    }

    private Vector magicKnockbackFunction(Vector delta) {
        delta = delta.clone();
        delta.setY(0);
        delta.normalize();
        final double theta = Math.toRadians(3);
        final double cos = Math.cos(theta);
        delta.setX(cos * delta.getX());
        delta.setY(Math.sin(theta));
        delta.setZ(cos * delta.getZ());
        return delta;
    }
}
