package network.warzone.tgm.parser.effect.tag;

import com.google.gson.JsonObject;

/**
 * Created by Jorge on 09/14/2019
 */
public class EffectAmbientParser implements EffectTagParser<Boolean> {

    @Override
    public Boolean parse(JsonObject object) {
        return !object.has("duration") || object.get("duration").getAsBoolean();
    }
}
