package network.warzone.tgm.parser.item.tag;

import com.google.gson.JsonObject;

/**
 * Created by Jorge on 09/14/2019
 */
public interface ItemTagParser<T> {

    T parse(JsonObject object);

}
