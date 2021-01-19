package network.warzone.tgm.map;

import com.google.gson.*;
import network.warzone.tgm.TGM;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RotationDeserializer implements JsonDeserializer<Rotation> {
    @Override
    public Rotation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        String name = json.get("name").getAsString();

        boolean isDefault = false;
        if (json.has("default")) {
            isDefault = json.get("default").getAsBoolean();
        }

        boolean initialShuffle = false;
        if (json.has("shuffle")) {
            initialShuffle = json.get("shuffle").getAsBoolean();
        }

        RotationRequirement requirements = new RotationRequirement(0, 999999);
        if (json.has("requirements")) {
            requirements = TGM.get().getGson().fromJson(json.get("requirements").getAsJsonObject(), RotationRequirement.class);
        }

        List<MapContainer> maps = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("maps")) {
            for (MapContainer mapContainer : TGM.get().getMatchManager().getMapLibrary().getMaps()) {
                if (mapContainer.getMapInfo().getName().equalsIgnoreCase(element.getAsString())) {
                    maps.add(mapContainer);
                }
            }
        }

        return new Rotation(name, isDefault, requirements, maps, initialShuffle);
    }
}
