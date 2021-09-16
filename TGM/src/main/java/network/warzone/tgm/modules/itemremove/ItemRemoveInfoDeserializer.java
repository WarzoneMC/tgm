package network.warzone.tgm.modules.itemremove;

import com.google.gson.*;
import network.warzone.tgm.util.Strings;
import org.bukkit.Material;

import java.lang.reflect.Type;

/**
 * Created by Jorge on 02/27/2021
 */
public class ItemRemoveInfoDeserializer implements JsonDeserializer<ItemRemoveInfo> {
    @Override
    public ItemRemoveInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            try {
                return new ItemRemoveInfo(Material.valueOf(Strings.getTechnicalName(json.getAsString())), true, false, false);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Unknown material: " + json.getAsString());
            }
        }
        JsonObject object = json.getAsJsonObject();
        try {
            if (!object.has("type")) throw new JsonParseException("material not set.");
            String type = object.get("type").getAsString();
            Material material = Material.valueOf(Strings.getTechnicalName(type));
            boolean death = !object.has("death") || object.get("death").getAsBoolean(); // Default: true
            boolean drop = object.has("drop") && object.get("drop").getAsBoolean(); // Default: false
            boolean spawn = object.has("spawn") && object.get("spawn").getAsBoolean(); // Default: false
            return new ItemRemoveInfo(material, death, drop, spawn);
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Unknown material: " + object.get("type").getAsString());
        }
    }
}
