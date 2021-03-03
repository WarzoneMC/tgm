package network.warzone.tgm.modules.legacy;

import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.config.TGMConfigReloadEvent;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.util.DamageUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Reverts 1.9's damage values to those of 1.8
 */
public class LegacyDamageModule extends MatchModule implements Listener {

    private boolean mapOverride;

    @EventHandler
    public void onConfigReload(TGMConfigReloadEvent event) {
        DamageUtils.loadConfig();
    }

    @Override
    public void load(Match match) {
        mapOverride = DamageUtils.isEnabled();

        JsonObject matchConfig = match.getMapContainer().getMapInfo().getJsonObject();
        if (!matchConfig.has("legacy")) return;

        JsonObject matchLegacyConfig = matchConfig.get("legacy").getAsJsonObject();
        if (matchLegacyConfig.has("damage")) mapOverride = matchLegacyConfig.get("damage").getAsBoolean();
    }

    @EventHandler(priority= EventPriority.LOWEST) // Make sure this event is called before the knockback
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;

        if ((event.getDamager() instanceof Player)) {
            Player player = (Player) event.getDamager();

            ItemStack weaponItem = player.getInventory().getItemInMainHand();
            // Potential bug with Paper -> could be null if empty hand
            if (weaponItem == null) return;
            if (!DamageUtils.hasEntry(weaponItem.getType())) return;

            double weaponDamage = DamageUtils.getDamage(weaponItem.getType());

            // Weakness - fixed decrease of 4HP damage (1.9+ weakness, 1.8 would be 1HP)
            if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) weaponDamage -= 4;

            // Strength
            PotionEffect strengthEffect = player.getPotionEffect(PotionEffectType.INCREASE_DAMAGE);
            // -1 -> 0 in next part so strength modifier is 0 if no strength.
            int amplifier = strengthEffect != null ? strengthEffect.getAmplifier() : -1;

            // 0 -> 1, 1 -> 2
            int strengthLevel = ++amplifier;
            // 3HP of damage per level of strength (1.9 strength+, 1.8 strength is way too OP)
            double strengthModifier = strengthLevel * 3;
            weaponDamage += strengthModifier;

            // Critical - add crits while sprinting (removed in 1.9)
            if (DamageUtils.isPseudoCriticalHit(player)) weaponDamage *= 1.5;
            if (!DamageUtils.isCriticalHit(player) && DamageUtils.isPseudoCriticalHit(player)) {
                ThreadLocalRandom rand = ThreadLocalRandom.current();
                Entity e = event.getEntity();
                // Adapted from 1.8.8 source (should replicate crit particles)
                for (int i = 0; i < 16; ++i) {
                    double xOff = rand.nextFloat() * 2.0F - 1.0F;
                    double yOff = rand.nextFloat() * 2.0F - 1.0F;
                    double zOff = rand.nextFloat() * 2.0F - 1.0F;
                    if (xOff * xOff + yOff * yOff + zOff * zOff <= 1.0D) {
                        double x = e.getLocation().getX() + xOff * e.getWidth() / 4.0D;
                        double y = e.getBoundingBox().getMinY() + (e.getHeight() / 2.0F) + yOff * e.getHeight() / 4.0D;
                        double z = e.getLocation().getZ() + zOff * e.getWidth() / 4.0D;
                        e.getWorld().spawnParticle(Particle.CRIT, x, y, z, 1, xOff, yOff + 0.2D, zOff);
                    }
                }
                e.getWorld().playSound(e.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
            }

            // Sharpness - 1.8 sharpness
            int sharpnessLevel = weaponItem.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
            double sharpnessDamage = DamageUtils.getOldSharpnessDamage(sharpnessLevel);
            weaponDamage += sharpnessDamage;

            event.setDamage(weaponDamage);
        } else if (event.getDamager() instanceof Arrow) {
            event.setDamage(event.getDamage() / 1.3);
        }
    }

    private boolean isEnabled() {
        return mapOverride;
    }


}
