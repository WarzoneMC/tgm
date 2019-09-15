package network.warzone.tgm.parser.item.tag;

import com.google.gson.JsonObject;
import network.warzone.tgm.util.Strings;
import org.bukkit.Material;

/**
 * Created by Jorge on 09/14/2019
 */
public class ItemMaterialParser implements ItemTagParser<Material> {

    @Override
    public Material parse(JsonObject object) {
        return Material.valueOf(Strings.getTechnicalName(object.get("material").getAsString()));
    }
}
