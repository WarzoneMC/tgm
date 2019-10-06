package network.warzone.tgm.parser.effect.tag;

import com.google.gson.JsonObject;
import network.warzone.tgm.util.Strings;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Jorge on 09/14/2019
 */
public class EffectTypeParser implements EffectTagParser<PotionEffectType> {

    @Override
    public PotionEffectType parse(JsonObject object) {
        return PotionEffectType.getByName(Strings.getTechnicalName(object.get("type").getAsString()));
    }
}
