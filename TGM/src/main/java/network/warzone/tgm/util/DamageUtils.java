package network.warzone.tgm.util;

import network.warzone.tgm.TGM;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DamageUtils {

    private static Map<String, Double> damages;
    private static Boolean enabled;

    static {
        if (isEnabled()) {
            ConfigurationSection damagesConfig = TGM.get().getConfig().getConfigurationSection("legacy.damages");

            if (damagesConfig != null) {
                damages = damagesConfig.getKeys(false).stream()
                        .filter(s -> damagesConfig.isDouble(s) || damagesConfig.isInt(s))
                        .collect(Collectors.toMap(key -> key, damagesConfig::getDouble));
            } else {
                loadDefaultDamages();
            }
        }
    }

    public static boolean isEnabled() {
        if (enabled == null) {
            ConfigurationSection legacyConfig = TGM.get().getConfig().getConfigurationSection("legacy");
            enabled = legacyConfig != null && legacyConfig.getBoolean("damage");
        }
        return enabled;
    }

    public static double getDamage(Material m) {
        return damages.getOrDefault(getFormattedName(m), -1.0);
    }

    public static boolean hasEntry(Material m) {
        return damages.containsKey(getFormattedName(m));
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

    private static String getFormattedName(Material m) {
        return m.name()
                .replace("GOLDEN", "GOLD")
                .replace("WOODEN", "WOOD")
                .replace("SHOVEL", "SPADE");
    }

    private static void loadDefaultDamages() {
        damages = new HashMap<>() {{
            put("GOLD_AXE", 4.0);
            put("WOOD_AXE", 4.0);
            put("STONE_AXE", 5.0);
            put("IRON_AXE", 6.0);
            put("DIAMOND_AXE", 7.0);
            put("NETHERITE_AXE", 8.0);
            put("GOLD_SPADE", 2.0);
            put("WOOD_SPADE", 2.0);
            put("STONE_SPADE", 3.0);
            put("IRON_SPADE", 4.0);
            put("DIAMOND_SPADE", 5.0);
            put("NETHERITE_SPADE", 6.0);
            put("GOLD_SWORD", 5.0);
            put("WOOD_SWORD", 5.0);
            put("STONE_SWORD", 6.0);
            put("IRON_SWORD", 7.0);
            put("DIAMOND_SWORD", 8.0);
            put("NETHERITE_SWORD", 9.0);
            put("GOLD_PICKAXE", 3.0);
            put("WOOD_PICKAXE", 3.0);
            put("STONE_PICKAXE", 4.0);
            put("IRON_PICKAXE", 5.0);
            put("DIAMOND_PICKAXE", 6.0);
            put("NETHERITE_PICKAXE", 7.0);
            put("GOLD_HOE", 1.0);
            put("WOOD_HOE", 1.0);
            put("STONE_HOE", 1.0);
            put("IRON_HOE", 1.0);
            put("DIAMOND_HOE", 1.0);
            put("NETHERITE_HOE", 1.0);
        }};
    }

}
