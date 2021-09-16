package network.warzone.tgm.parser.effect.tag;

import com.google.gson.JsonObject;

/**
 * Created by Jorge on 09/14/2019
 */
public class EffectParticleParser implements EffectTagParser<Boolean> {

    @Override
    public Boolean parse(JsonObject object) {
        return !object.has("particles") || object.get("particles").getAsBoolean();
    }
}
