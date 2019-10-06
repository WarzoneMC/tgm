package network.warzone.tgm.parser.item.tag;

import com.google.gson.JsonObject;

/**
 * Created by Jorge on 09/14/2019
 */
public class ItemAmountParser implements ItemTagParser<Integer> {

    @Override
    public Integer parse(JsonObject object) {
        return object.has("amount") ? object.get("amount").getAsInt() : 1;
    }
}
