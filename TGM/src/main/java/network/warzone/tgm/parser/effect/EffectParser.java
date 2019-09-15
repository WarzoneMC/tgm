package network.warzone.tgm.parser.effect;

import com.google.gson.JsonObject;
import network.warzone.tgm.parser.effect.tag.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Jorge on 09/14/2019
 */
public class EffectParser {

    public static EffectTagParser<PotionEffectType> typeParser = new EffectTypeParser();
    public static EffectTagParser<Integer> durationParser = new EffectDurationParser();
    public static EffectTagParser<Integer> amplifierParser = new EffectAmplifierParser();
    public static EffectTagParser<Boolean> ambientParser = new EffectAmbientParser();
    public static EffectTagParser<Boolean> particleParser = new EffectParticleParser();

    public static PotionEffect parse(JsonObject object) {
        PotionEffectType type = typeParser.parse(object);
        int duration = durationParser.parse(object);
        int amplifier = amplifierParser.parse(object);
        boolean ambient = ambientParser.parse(object);
        boolean particles = particleParser.parse(object);
        return new PotionEffect(type, duration, amplifier, ambient, particles);
    }

}
