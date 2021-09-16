package network.warzone.tgm.parser.banner;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import network.warzone.tgm.util.Strings;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 11/08/2019
 */
public class BannerPatternsDeserializer implements JsonDeserializer<List<Pattern>> {

    public static List<Pattern> parse(JsonElement jsonElement) {
        Preconditions.checkArgument(jsonElement.isJsonArray(), "Banner patterns must be a JSON array");
        List<Pattern> patterns = new ArrayList<>();
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        for (JsonElement element : jsonArray) {
            if (element.isJsonPrimitive()) {
                try {
                    String in = element.getAsString();
                    String[] args = in.split(":");
                    DyeColor dyeColor = DyeColor.valueOf(Strings.getTechnicalName(args[0]));
                    PatternType patternType;
                    try {
                        patternType = PatternType.valueOf(Strings.getTechnicalName(args[1]));
                    } catch (IllegalArgumentException ignored) {
                        patternType = PatternType.getByIdentifier(args[1]);
                    }
                    if (patternType != null) patterns.add(new Pattern(dyeColor, patternType));
                } catch (Exception ignored) {}
            } else if (element.isJsonObject()) {
                try {
                    JsonObject jsonObject = element.getAsJsonObject();
                    DyeColor dyeColor = DyeColor.valueOf(Strings.getTechnicalName(jsonObject.get("color").getAsString()));
                    String type = jsonObject.get("type").getAsString();
                    PatternType patternType;
                    try {
                        patternType = PatternType.valueOf(Strings.getTechnicalName(type));
                    } catch (IllegalArgumentException ignored) {
                        patternType = PatternType.getByIdentifier(type);
                    }
                    if (patternType != null) patterns.add(new Pattern(dyeColor, patternType));
                } catch (Exception ignored) {}
            }
        }
        return patterns;
    }

    @Override
    public List<Pattern> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return parse(json);
    }

}
