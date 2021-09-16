package network.warzone.tgm.util.itemstack;

import org.bukkit.potion.PotionEffectType;

/**
 * Created by Jorge on 10/19/2019
 */
public class Effects {

    public static String toMinecraftID(PotionEffectType type) {
        switch (type.getName()) {
            case "SPEED":
                return "speed";
            case "SLOW":
                return "slowness";
            case "FAST_DIGGING":
                return "haste";
            case "SLOW_DIGGING":
                return "mining_fatigue";
            case "INCREASE_DAMAGE":
                return "strength";
            case "HEAL":
                return "instant_health";
            case "HARM":
                return "instant_damage";
            case "JUMP":
                return "jump_boost";
            case "CONFUSION":
                return "nausea";
            case "REGENERATION":
                return "regeneration";
            case "DAMAGE_RESISTANCE":
                return "resistance";
            case "FIRE_RESISTANCE":
                return "fire_resistance";
            case "WATER_BREATHING":
                return "water_breathing";
            case "INVISIBILITY":
                return "invisibility";
            case "BLINDNESS":
                return "blindness";
            case "NIGHT_VISION":
                return "night_vision";
            case "HUNGER":
                return "hunger";
            case "WEAKNESS":
                return "weakness";
            case "POISON":
                return "poison";
            case "WITHER":
                return "wither";
            case "HEALTH_BOOST":
                return "health_boost";
            case "ABSORPTION":
                return "absorption";
            case "SATURATION":
                return "saturation";
            case "GLOWING":
                return "glowing";
            case "LEVITATION":
                return "levitation";
            case "LUCK":
                return "luck";
            case "UNLUCK":
                return "unluck";
            case "SLOW_FALLING":
                return "slow_falling";
            case "CONDUIT_POWER":
                return "conduit_power";
            case "DOLPHINS_GRACE":
                return "dolphins_grace";
            case "BAD_OMEN":
                return "bad_omen";
            case "HERO_OF_THE_VILLAGE":
                return "hero_of_the_village";
            default:
                return null;
        }
    }

}
