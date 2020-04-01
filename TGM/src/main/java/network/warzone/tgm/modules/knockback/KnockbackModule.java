package network.warzone.tgm.modules.knockback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.config.TGMConfigReloadEvent;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.text.NumberFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.Math.PI;

/**
 * Complete override for the vanilla knockback system.
 */
public class KnockbackModule extends MatchModule implements Listener {

    private Random random = new Random();

    // https://gist.github.com/YoungOG/e3265d98661957abece71594b70d6a01
    
    private static boolean enabled = false;
    
    private static double knockBackFriction;
    private static double knockBackHorizontal;
    private static double knockBackVertical;
    private static double knockBackVerticalLimit;
    private static double knockBackExtraHorizontal;
    private static double knockBackExtraVertical;

    // Not currently working
    private static double knockBackBowScale = 0.15D;
    private static double knockBackPunchMultiplier = 1D;

    static {
        loadValues();
    }
    
    private static void loadValues() {
        enabled = TGM.get().getConfig().getBoolean("custom-knockback.enabled");
        knockBackFriction = TGM.get().getConfig().getDouble("custom-knockback.friction");
        knockBackHorizontal = TGM.get().getConfig().getDouble("custom-knockback.horizontal");
        knockBackVertical = TGM.get().getConfig().getDouble("custom-knockback.vertical");
        knockBackVerticalLimit = TGM.get().getConfig().getDouble("custom-knockback.vertical-limit");
        knockBackExtraHorizontal = TGM.get().getConfig().getDouble("custom-knockback.horizontal-extra");
        knockBackExtraVertical = TGM.get().getConfig().getDouble("custom-knockback.vertical-extra");
    }
    
    private Map<Player, EntityDamageByEntityContext> queued = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!enabled) {
            // Default to previous knockback code until optimal values are fully figured out
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getDamager() instanceof LivingEntity) {
                meleeKnockback((LivingEntity) event.getDamager(), player);
            } else if (event.getDamager() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getDamager();
                event.setDamage(event.getDamage() / 1.75);
                bowKnockback(arrow, player);
            }
            return;
        }
        if (event.getDamager() instanceof Arrow) return; // TODO: Apply custom bow knockback
        this.queued.put((Player) event.getEntity(), new EntityDamageByEntityContext(
                event,
                event.getDamager() instanceof Player && ((Player) event.getDamager()).isSprinting()
        ));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(PlayerVelocityEvent event) {
        if (this.queued.containsKey(event.getPlayer())) {
            event.setVelocity(new Vector());
            EntityDamageByEntityContext context = this.queued.get(event.getPlayer());
            if (context.getEvent().getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && context.getEvent().getDamager() instanceof LivingEntity) {
                applyMeleeKnockback(event, context.getEvent().getDamager(), context.isSprinting());
            }
            else if (false && context.getEvent().getDamager() instanceof Arrow) { // Disabled (not working)
                applyBowKnockback(event, (Arrow) context.getEvent().getDamager());
            }
            this.queued.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onArrowsShoot(ProjectileLaunchEvent e) {
        Projectile projectile = e.getEntity();
        ProjectileSource shooter = projectile.getShooter();
        if (shooter instanceof Player) {
            Player player = (Player) shooter;
            projectile.setVelocity(player.getLocation().getDirection().clone().normalize().multiply(projectile.getVelocity().length()));
        }
    }

    @EventHandler
    public void onConfigReload(TGMConfigReloadEvent event) {
        loadValues();
    }

    // TODO: Fix weird arrow direction issue
    private void applyBowKnockback(PlayerVelocityEvent event, Arrow arrow) {
        try {
            double kbRes = getAttribute(event.getPlayer(), Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            if (this.random.nextDouble() >= kbRes) {
                Vector velocity = getBaseKnockback(event.getPlayer(), event.getVelocity(), arrow);
                int punch = arrow.getKnockbackStrength();
                Vector arrowVelocity = arrow.getVelocity().clone();
                double f1 = Math.sqrt(arrowVelocity.getX() * arrowVelocity.getX() + arrowVelocity.getZ() * arrowVelocity.getZ());
                if (f1 > 0) {
                    arrowVelocity.setX(arrowVelocity.getX() * (1 + punch * knockBackPunchMultiplier) * knockBackBowScale / f1);
                    arrowVelocity.setY(knockBackExtraVertical);
                    arrowVelocity.setZ(arrowVelocity.getZ() * (1 + punch * knockBackPunchMultiplier) * knockBackBowScale / f1);
                    velocity.add(arrowVelocity);
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

    private Vector getBaseKnockback(LivingEntity victim, Vector velocity, Entity attacker) {
        velocity = velocity.clone();
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

    @Deprecated
    private void meleeKnockback(LivingEntity attacker, LivingEntity victim) {
        Vector kb = attacker.getLocation().clone().getDirection().setY(victim.isOnGround() ? 0.4 : 0).normalize().multiply(0.25f);
        victim.setVelocity(kb);
    }

    @Deprecated
    private void bowKnockback(Arrow a, Entity e) {
        Vector normalVelocity = a.getVelocity();
        if (e.isOnGround()) normalVelocity.setY(0.3);
        else normalVelocity.setY(0);
        e.setVelocity(normalVelocity.normalize().multiply(0.2f));
    }

    @AllArgsConstructor @Getter
    private static class EntityDamageByEntityContext {
        private EntityDamageByEntityEvent event;
        private boolean sprinting; // Required since event always returns false after being fired
    }

}
