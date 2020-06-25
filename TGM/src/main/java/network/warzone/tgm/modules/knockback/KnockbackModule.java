package network.warzone.tgm.modules.knockback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.config.TGMConfigReloadEvent;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.PI;

/**
 * Complete override for the vanilla knockback system.
 */
public class KnockbackModule extends MatchModule implements Listener {

    private static final Set<EntityDamageEvent.DamageCause> CAUSES = new HashSet<>();

    // https://gist.github.com/YoungOG/e3265d98661957abece71594b70d6a01
    private static boolean enabled;
    private static double knockBackFriction;
    private static double knockBackHorizontal;
    private static double knockBackVertical;
    private static double knockBackVerticalLimit;
    private static double knockBackExtraHorizontal;
    private static double knockBackExtraVertical;
    private static double knockBackBowBase;
    private static double knockBackBowVertical;
    private static double knockBackPunchMultiplier;

    private Random random = new Random();
    private Map<Arrow, Vector> arrowDirection = new HashMap<>();
    private Map<Player, EntityDamageByEntityContext> queued = new HashMap<>();

    static {
        CAUSES.add(EntityDamageEvent.DamageCause.ENTITY_ATTACK);
        CAUSES.add(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK);
        CAUSES.add(EntityDamageEvent.DamageCause.PROJECTILE);
        loadValues();
    }

    private static void loadValues() {
        enabled = TGM.get().getConfig().getBoolean("custom-knockback.enabled", false);
        knockBackFriction = TGM.get().getConfig().getDouble("custom-knockback.friction");
        knockBackHorizontal = TGM.get().getConfig().getDouble("custom-knockback.horizontal");
        knockBackVertical = TGM.get().getConfig().getDouble("custom-knockback.vertical");
        knockBackVerticalLimit = TGM.get().getConfig().getDouble("custom-knockback.vertical-limit");
        knockBackExtraHorizontal = TGM.get().getConfig().getDouble("custom-knockback.horizontal-extra");
        knockBackExtraVertical = TGM.get().getConfig().getDouble("custom-knockback.vertical-extra");
        knockBackBowBase = TGM.get().getConfig().getDouble("custom-knockback.bow-base");
        knockBackBowVertical = TGM.get().getConfig().getDouble("custom-knockback.bow-vertical");
        knockBackPunchMultiplier = TGM.get().getConfig().getDouble("custom-knockback.punch-multiplier");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!enabled) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        this.queued.put(player, new EntityDamageByEntityContext(
                event,
                event.getDamager() instanceof Player && ((Player) event.getDamager()).isSprinting()
        ));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        if (!enabled) return;
        EntityDamageByEntityContext context = this.queued.get(event.getPlayer());
        if (context == null) return;
        EntityDamageByEntityEvent edEvent = context.getEvent();
        if (CAUSES.contains(edEvent.getCause())) {
            Entity damager = context.getEvent().getDamager();
            if (edEvent.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && edEvent.getDamager() instanceof LivingEntity) {
                event.setVelocity(new Vector());
                applyMeleeKnockback(event, edEvent.getDamager(), context.isSprinting());
            } else if (damager instanceof Arrow && this.arrowDirection.containsKey(damager)) {
                event.setVelocity(new Vector());
                applyBowKnockback(event, (Arrow) context.getEvent().getDamager());
            }
            // Else: Other projectiles (snowball, eggs, etc) get vanilla knockback
        }
        this.queued.remove(event.getPlayer());
    }

    @EventHandler
    public void onArrowsShoot(ProjectileLaunchEvent e) {
        if (!enabled) return;
        Projectile projectile = e.getEntity();
        ProjectileSource shooter = projectile.getShooter();
        if (projectile instanceof Arrow) {
            Vector direction;
            if (shooter instanceof Entity) {
                direction = ((Entity) shooter).getLocation().getDirection().clone();
            } else {
                direction = projectile.getLocation().getDirection().clone();
            }
            addArrow((Arrow) projectile, direction);
        }
        
        // Fixes arrow randomization
        if (shooter instanceof Player) {
            Player player = (Player) shooter;
            projectile.setVelocity(player.getLocation().getDirection().normalize().multiply(projectile.getVelocity().length()));
        }
    }

    private void addArrow(Arrow arrow, Vector direction) {
        List<Arrow> toRemove = new ArrayList<>();
        for (Arrow a : this.arrowDirection.keySet()) {
            if (!a.isValid()) toRemove.add(a);
        }
        for (Arrow a : toRemove) {
            this.arrowDirection.remove(a);
        }
        this.arrowDirection.put(arrow, direction);
    }

    @EventHandler
    public void onArrowLand(ProjectileHitEvent event) {
        if (!enabled) return;
        if (event.getHitBlock() != null && event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            this.arrowDirection.remove(arrow);
        }
    }

    @EventHandler
    public void onConfigReload(TGMConfigReloadEvent event) {
        loadValues();
        if (!enabled) {
            this.queued.clear();
            this.arrowDirection.clear();
        }
    }

    private void applyBowKnockback(PlayerVelocityEvent event, Arrow arrow) {
        try {
            double kbRes = getAttribute(event.getPlayer(), Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            if (this.random.nextDouble() >= kbRes) {
                Vector direction;
                if (this.arrowDirection.containsKey(arrow)) {
                    direction = this.arrowDirection.get(arrow);
                    this.arrowDirection.remove(arrow);
                } else {
                    direction = arrow.getLocation().getDirection().clone();
                }
                direction = direction.normalize();
                int punch = arrow.getKnockbackStrength();
                Vector velocity = event.getPlayer().getVelocity().clone();
                double distance = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
                double d0 = direction.getX() * 0.1;
                double d1 = direction.getZ() * 0.1;
                if (distance > 0) {
                    velocity.setX(velocity.getX() + (d0 * knockBackBowBase / distance) + (d0 * punch * knockBackPunchMultiplier) / distance);
                    velocity.setY(knockBackBowVertical);
                    velocity.setZ(velocity.getZ() + (d1 * knockBackBowBase / distance) + (d1 * punch * knockBackPunchMultiplier) / distance);
                }
                event.setVelocity(velocity);
            }
        } catch (Exception ignored) {}
    }

    private void applyMeleeKnockback(PlayerVelocityEvent event, Entity attacker, boolean sprinting) {
        try {
            double kbRes = getAttribute(event.getPlayer(), Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            if (this.random.nextDouble() >= kbRes) {
                Vector velocity = getBaseKnockback(event.getPlayer(), event.getVelocity(), attacker);
                double vertical = 0;
                float yaw = attacker.getLocation().getYaw();
                int kbStrength = 0;
                if (attacker instanceof Player) {
                    Player attackerP = (Player) attacker;
                    ItemStack itemStack = attackerP.getInventory().getItemInMainHand();
                    kbStrength += itemStack.getEnchantmentLevel(Enchantment.KNOCKBACK);
                    if (sprinting) {
                        kbStrength++;
                        vertical = 0.1;
                    }
                }
                Vector kb = new Vector(
                        -Math.sin(yaw * PI / 180.0F) * (kbStrength) * knockBackExtraHorizontal,
                        event.getPlayer().isOnGround() ? knockBackExtraVertical + vertical : 0,
                        Math.cos(yaw * PI / 180.0F) * (kbStrength) * knockBackExtraHorizontal
                );
                velocity.add(kb);
                event.setVelocity(velocity);
            }
        } catch (Exception ignored) {
        }
    }

    private Vector getBaseKnockback(LivingEntity victim, Vector vector, Entity attacker) {
        Vector velocity = vector.clone();
        double d0 = attacker.getLocation().getX() - victim.getLocation().getX();
        double d1;

        for (d1 = attacker.getLocation().getZ() - victim.getLocation().getZ(); d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
            d0 = (Math.random() - Math.random()) * 0.01D;
        }
        double magnitude = Math.sqrt(d0 * d0 + d1 * d1);

        velocity.setX(velocity.getX() / knockBackFriction);
        velocity.setY(velocity.getY() / knockBackFriction);
        velocity.setZ(velocity.getZ() / knockBackFriction);

        velocity.setX(velocity.getX() - d0 / magnitude * knockBackHorizontal);
        velocity.setY(velocity.getY() + knockBackVertical);
        velocity.setZ(velocity.getZ() - d1 / magnitude * knockBackHorizontal);

        if (velocity.getY() > knockBackVerticalLimit) {
            velocity.setY(knockBackVerticalLimit);
        }
        return velocity;
    }

    public double getAttribute(LivingEntity entity, Attribute attribute) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance == null) return 0;
        Collection<AttributeModifier> modifiers = attributeInstance.getModifiers();
        double value = 0;
        modifiers = modifiers.stream()
                .sorted(Comparator.comparingInt(mod -> mod.getOperation().ordinal()))
                .collect(Collectors.toList());
        boolean f1 = false;
        boolean f2 = false;
        double scalar = 0;
        double multiplyScalar1 = 0;
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER) {
                value += modifier.getAmount();
            } else if (modifier.getOperation() == AttributeModifier.Operation.ADD_SCALAR) {
                f1 = true;
                if (scalar == 0) {
                    scalar++;
                }
                scalar += modifier.getAmount();
            } else if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_SCALAR_1) {
                f2 = true;
                multiplyScalar1 *= 1 + modifier.getAmount();
            }
        }
        if (f1) value *= scalar;
        if (f2) value *= multiplyScalar1;
        return attributeInstance.getBaseValue() + value;
    }

    @AllArgsConstructor @Getter
    private static class EntityDamageByEntityContext {
        private EntityDamageByEntityEvent event;
        private boolean sprinting; // Required since event always returns false after being fired
    }

}
