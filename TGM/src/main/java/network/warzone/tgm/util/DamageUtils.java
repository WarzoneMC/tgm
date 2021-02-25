package network.warzone.tgm.util;

import network.warzone.tgm.TGM;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumMap;
import java.util.stream.Collectors;

public class DamageUtils {

    private static EnumMap<Material, Double> damages;
    private static boolean enabled;
    private static boolean loaded;

    static {
        if (isEnabled()) {
            ConfigurationSection damagesConfig = TGM.get().getConfig().getConfigurationSection("legacy.damages");

            if (damagesConfig != null) {
                damages = damagesConfig.getKeys(false).stream()
                        .filter(s -> damagesConfig.isDouble(s) || damagesConfig.isInt(s))
                        .collect(
                                Collectors.toMap(
                                        Material::valueOf,
                                        damagesConfig::getDouble,
                                        (keyA, keyB) -> {
                                            throw new IllegalArgumentException(
                                                    String.format("Duplicate item damage definition in config.yml. (%s, %s)", keyA, keyB)
                                            );
                                        },
                                        () -> new EnumMap<>(Material.class)
                                )
                        );
            } else {
                loadDefaultDamages();
            }
        }
    }

    public static boolean isEnabled() {
        if (!loaded) {
            ConfigurationSection legacyConfig = TGM.get().getConfig().getConfigurationSection("legacy");
            enabled = legacyConfig != null && legacyConfig.getBoolean("damage");
            loaded = true;
        }
        return enabled;
    }

    public static double getDamage(Material m) {
        return damages.getOrDefault(m, -1.0);
    }

    public static boolean hasEntry(Material m) {
        return damages.containsKey(m);
    }

    public static boolean isCriticalHit(Player p) {
        return isPseudoCriticalHit(p) && !p.isSprinting();
    }

    public static boolean isPseudoCriticalHit(Player p) {
        return !p.isOnGround() && p.getFallDistance() > 0 &&
                !p.getLocation().getBlock().isLiquid() &&
                !p.isInsideVehicle() &&
                p.getActivePotionEffects().stream().noneMatch(potionEffect -> potionEffect.getType() == PotionEffectType.BLINDNESS);
    }

    public static double getOldSharpnessDamage(int level) {
        return level >= 1 ? level * 1.25 : 0;
    }

    private static void loadDefaultDamages() {
        damages = new EnumMap<Material, Double>(Material.class) {{
            put(Material.GOLDEN_AXE, 4.0);
            put(Material.WOODEN_AXE, 4.0);
            put(Material.STONE_AXE, 5.0);
            put(Material.IRON_AXE, 6.0);
            put(Material.DIAMOND_AXE, 7.0);
            put(Material.NETHERITE_AXE, 8.0);
            put(Material.GOLDEN_SHOVEL, 2.0);
            put(Material.WOODEN_SHOVEL, 2.0);
            put(Material.STONE_SHOVEL, 3.0);
            put(Material.IRON_SHOVEL, 4.0);
            put(Material.DIAMOND_SHOVEL, 5.0);
            put(Material.NETHERITE_SHOVEL, 6.0);
            put(Material.GOLDEN_SWORD, 5.0);
            put(Material.WOODEN_SWORD, 5.0);
            put(Material.STONE_SWORD, 6.0);
            put(Material.IRON_SWORD, 7.0);
            put(Material.DIAMOND_SWORD, 8.0);
            put(Material.NETHERITE_SWORD, 9.0);
            put(Material.GOLDEN_PICKAXE, 3.0);
            put(Material.WOODEN_PICKAXE, 3.0);
            put(Material.STONE_PICKAXE, 4.0);
            put(Material.IRON_PICKAXE, 5.0);
            put(Material.DIAMOND_PICKAXE, 6.0);
            put(Material.NETHERITE_PICKAXE, 7.0);
            put(Material.GOLDEN_HOE, 1.0);
            put(Material.WOODEN_HOE, 1.0);
            put(Material.STONE_HOE, 1.0);
            put(Material.IRON_HOE, 1.0);
            put(Material.DIAMOND_HOE, 1.0);
            put(Material.NETHERITE_HOE, 1.0);
        }};
    }

}
