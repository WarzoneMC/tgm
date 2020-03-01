package network.warzone.tgm.modules.knockback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
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
    // Using suppliers for debugging (Hot Swap)
    private static final Supplier<Double> knockBackFriction = () -> 2.0D;
    private static final Supplier<Double> knockBackHorizontal = () -> 0.35D;
    private static final Supplier<Double> knockBackVertical = () -> 0.35D;
    private static final Supplier<Double> knockBackVerticalLimit = () -> 0.4D;
    private static final Supplier<Double> knockBackExtraHorizontal = () -> 0.425D;
    private static final Supplier<Double> knockBackExtraVertical = () -> 0.085D * 0; // This KOHI value is too high

    private static final Supplier<Double> knockBackBowScale = () -> 0.15D;
    private static final Supplier<Double> knockBackPunchMultiplier = () -> 1D;

    private Map<Player, EntityDamageByEntityContext> queued = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
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
            else if (context.getEvent().getDamager() instanceof Arrow) {
                /* Not working */
//                applyBowKnockback(event, (Arrow) context.getEvent().getDamager());
            }
            this.queued.remove(event.getPlayer());
        }
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
                    arrowVelocity.setX(arrowVelocity.getX() * (1 + punch * knockBackPunchMultiplier.get()) * knockBackBowScale.get() / f1);
                    arrowVelocity.setY(knockBackExtraVertical.get());
                    arrowVelocity.setZ(arrowVelocity.getZ() * (1 + punch * knockBackPunchMultiplier.get()) * knockBackBowScale.get() / f1);
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
                        -Math.sin(yaw * PI / 180.0F) * (kbStrength) * knockBackExtraHorizontal.get(),
                        event.getPlayer().isOnGround() ? knockBackExtraVertical.get() + vertical : 0,
                        Math.cos(yaw * PI / 180.0F) * (kbStrength) * knockBackExtraHorizontal.get()
                );
                velocity.add(kb);
                Bukkit.broadcastMessage(ChatColor.RED.toString() + NumberFormat.getInstance().format(velocity.getX()) + " " +
                        ChatColor.GREEN + NumberFormat.getInstance().format(velocity.getY()) + " " +
                        ChatColor.BLUE + NumberFormat.getInstance().format(velocity.getZ()));
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

        velocity.setX(velocity.getX() / knockBackFriction.get());
        velocity.setY(velocity.getY() / knockBackFriction.get());
        velocity.setZ(velocity.getZ() / knockBackFriction.get());

        velocity.setX(velocity.getX() - d0 / magnitude * knockBackHorizontal.get());
        velocity.setY(velocity.getY() + knockBackVertical.get());
        velocity.setZ(velocity.getZ() - d1 / magnitude * knockBackHorizontal.get());

        if (velocity.getY() > knockBackVerticalLimit.get()) {
            velocity.setY(knockBackVerticalLimit.get());
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
