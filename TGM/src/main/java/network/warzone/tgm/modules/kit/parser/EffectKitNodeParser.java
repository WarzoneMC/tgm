package network.warzone.tgm.modules.kit.parser;

import com.google.gson.JsonObject;
import network.warzone.tgm.modules.kit.KitNode;
import network.warzone.tgm.modules.kit.types.EffectKitNode;
import network.warzone.tgm.parser.effect.EffectParser;

import java.util.Collections;
import java.util.List;

public class EffectKitNodeParser implements KitNodeParser {

    @Override
    public List<KitNode> parse(JsonObject jsonObject) {
        return Collections.singletonList(new EffectKitNode(EffectParser.parse(jsonObject)));
    }

}
