package network.warzone.tgm.modules.legacy;

import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class LegacyArmorModule extends MatchModule implements Listener {

    private static final double REDUCTION_PER_ARMOR_POINT = 0.04;

    private static final Set<EntityDamageEvent.DamageCause> NON_REDUCED_CAUSES = EnumSet.of(
            EntityDamageEvent.DamageCause.FIRE_TICK,
            EntityDamageEvent.DamageCause.VOID,
            EntityDamageEvent.DamageCause.SUFFOCATION,
            EntityDamageEvent.DamageCause.DROWNING,
            EntityDamageEvent.DamageCause.STARVATION,
            EntityDamageEvent.DamageCause.FALL,
            EntityDamageEvent.DamageCause.MAGIC,
            EntityDamageEvent.DamageCause.LIGHTNING
    );

    private static final boolean globalEnabled;

    private Boolean mapOverride;

    static {
        ConfigurationSection legacyConfig = TGM.get().getConfig().getConfigurationSection("legacy");
        globalEnabled = legacyConfig != null && legacyConfig.getBoolean("armor");
    }

    @Override
    public void load(Match match) {
        JsonObject matchConfig = match.getMapContainer().getMapInfo().getJsonObject();
        if (!matchConfig.has("legacy")) return;

        JsonObject matchLegacyConfig = matchConfig.get("legacy").getAsJsonObject();
        if (matchLegacyConfig.has("damage")) mapOverride = matchLegacyConfig.get("armor").getAsBoolean();
        else mapOverride = null;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent e) {
        if (!isEnabled()) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player damaged = (Player) e.getEntity();

        if (!e.isApplicable(EntityDamageEvent.DamageModifier.MAGIC)) return;

        double armourPoints = damaged.getAttribute(Attribute.GENERIC_ARMOR).getValue();
        double reductionPercentage = armourPoints * REDUCTION_PER_ARMOR_POINT;

        double reducedDamage = e.getDamage() * reductionPercentage;
        EntityDamageEvent.DamageCause cause = e.getCause();

        if (!NON_REDUCED_CAUSES.contains(cause) && e.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
            e.setDamage(EntityDamageEvent.DamageModifier.ARMOR, -reducedDamage);
        }

        if (damaged.getEquipment() == null) return;
        double enchantmentReductionPercentage = calculateEnchantmentReductionPercentage(damaged.getEquipment(), cause);

        if (enchantmentReductionPercentage > 0) {
            e.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0);
            e.setDamage(EntityDamageEvent.DamageModifier.MAGIC, -e.getFinalDamage() * enchantmentReductionPercentage);
        }
    }

    private double calculateEnchantmentReductionPercentage(EntityEquipment equipment, EntityDamageEvent.DamageCause cause) {
        int totalEpf = 0;
        for (ItemStack armorItem : equipment.getArmorContents()) {
            if (armorItem != null && armorItem.getType() != Material.AIR) {
                for (EnchantmentType enchantmentType : EnchantmentType.values()) {
                    if (!enchantmentType.protectsAgainst(cause)) continue;
                    int enchantmentLevel = armorItem.getEnchantmentLevel(enchantmentType.getEnchantment());

                    if (enchantmentLevel > 0) totalEpf += enchantmentType.getEpf(enchantmentLevel);
                }
            }
        }

        // EPF capped at 25 pre rng
        totalEpf = Math.min(25, totalEpf);

        totalEpf = (int) Math.ceil(totalEpf * ThreadLocalRandom.current().nextDouble(0.5, 1));

        // EPF capped at 20 post rng
        totalEpf = Math.min(20, totalEpf);

        return REDUCTION_PER_ARMOR_POINT * totalEpf;
    }

    private boolean isEnabled() {
        if (mapOverride != null) return mapOverride;
        return globalEnabled;
    }

    private enum EnchantmentType {
        PROTECTION(() -> EnumSet.allOf(EntityDamageEvent.DamageCause.class), 0.75, Enchantment.PROTECTION_ENVIRONMENTAL),
        FIRE_PROTECTION(() -> EnumSet.of(
                EntityDamageEvent.DamageCause.FIRE,
                EntityDamageEvent.DamageCause.FIRE_TICK,
                EntityDamageEvent.DamageCause.LAVA,
                EntityDamageEvent.DamageCause.HOT_FLOOR
        ), 1.25, Enchantment.PROTECTION_FIRE),
        BLAST_PROTECTION(() -> EnumSet.of(
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
                EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
        ), 1.5, Enchantment.PROTECTION_EXPLOSIONS),
        PROJECTILE_PROTECTION(() -> EnumSet.of(EntityDamageEvent.DamageCause.PROJECTILE), 1.5, Enchantment.PROTECTION_PROJECTILE),
        FALL_PROTECTION(() -> EnumSet.of(EntityDamageEvent.DamageCause.FALL), 2.5, Enchantment.PROTECTION_FALL);

        private Set<EntityDamageEvent.DamageCause> protection;
        private double typeModifier;
        private Enchantment enchantment;

        EnchantmentType(Supplier<Set<EntityDamageEvent.DamageCause>> protection, double typeModifier, Enchantment enchantment) {
            this.protection = protection.get();
            this.typeModifier = typeModifier;
            this.enchantment = enchantment;
        }

        public boolean protectsAgainst(EntityDamageEvent.DamageCause cause) {
            return protection.contains(cause);
        }

        public Enchantment getEnchantment() {
            return enchantment;
        }

        public int getEpf(int level) {
            // https://minecraft.gamepedia.com/Armor?oldid=909187
            return (int) Math.floor((6 + level * level) * typeModifier / 3);
        }
    }

}
