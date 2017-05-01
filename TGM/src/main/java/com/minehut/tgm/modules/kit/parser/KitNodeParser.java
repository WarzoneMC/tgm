package com.minehut.tgm.modules.kit.parser;

import com.google.gson.JsonObject;
import com.minehut.tgm.modules.kit.KitNode;

import java.util.List;

public interface KitNodeParser {
    List<KitNode> parse(JsonObject jsonObject);
}
