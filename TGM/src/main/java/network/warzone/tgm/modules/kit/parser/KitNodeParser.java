package network.warzone.tgm.modules.kit.parser;

import com.google.gson.JsonObject;
import network.warzone.tgm.modules.kit.KitNode;

import java.util.List;

public interface KitNodeParser {
    List<KitNode> parse(JsonObject jsonObject);
}
