package network.warzone.tgm.parser.effect;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.parser.effect.tag.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Type;

/**
 * Created by Jorge on 09/14/2019
 */
public class EffectDeserializer implements JsonDeserializer<PotionEffect> {

    @Getter @Setter private static EffectTagParser<PotionEffectType> typeParser = new EffectTypeParser();
    @Getter @Setter private static EffectTagParser<Integer> durationParser = new EffectDurationParser();
    @Getter @Setter private static EffectTagParser<Integer> amplifierParser = new EffectAmplifierParser();
    @Getter @Setter private static EffectTagParser<Boolean> ambientParser = new EffectAmbientParser();
    @Getter @Setter private static EffectTagParser<Boolean> particleParser = new EffectParticleParser();

    public static PotionEffect parse(JsonObject object) {
        PotionEffectType type = typeParser.parse(object);
        int duration = durationParser.parse(object);
        int amplifier = amplifierParser.parse(object);
        boolean ambient = ambientParser.parse(object);
        boolean particles = particleParser.parse(object);
        return new PotionEffect(type, duration, amplifier, ambient, particles);
    }

    @Override
    public PotionEffect deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        assert json.isJsonObject() : "JSON element is not a valid object for effect deserializing.";
        return parse(json.getAsJsonObject());
    }
}
