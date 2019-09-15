package network.warzone.tgm.parser.effect.tag;

import com.google.gson.JsonObject;

/**
 * Created by Jorge on 09/14/2019
 */
public interface EffectTagParser<T> {

    T parse(JsonObject object);

}
