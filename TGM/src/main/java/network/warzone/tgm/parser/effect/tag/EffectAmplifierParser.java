package network.warzone.tgm.parser.effect.tag;

import com.google.gson.JsonObject;

/**
 * Created by Jorge on 09/14/2019
 */
public class EffectAmplifierParser implements EffectTagParser<Integer> {

    @Override
    public Integer parse(JsonObject object) {
        return object.has("amplifier") ? object.get("amplifier").getAsInt() : 0;
    }
}
