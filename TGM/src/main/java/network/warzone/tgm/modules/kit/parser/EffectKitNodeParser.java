package network.warzone.tgm.modules.kit.parser;

import com.google.gson.JsonObject;
import network.warzone.tgm.modules.kit.KitNode;
import network.warzone.tgm.modules.kit.types.EffectKitNode;
import network.warzone.tgm.util.Strings;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class EffectKitNodeParser implements KitNodeParser {

    @Override
    public List<KitNode> parse(JsonObject jsonObject) {
        return Collections.singletonList(new EffectKitNode(parsePotionEffect(jsonObject)));
    }

    private PotionEffect parsePotionEffect(JsonObject jsonObject) {
        PotionEffectType type = PotionEffectType.getByName(Strings.getTechnicalName(jsonObject.get("type").getAsString()));

        int duration = 30;
        int amplifier = 0;
        boolean ambient = true;
        boolean particles = true;
        //Color color = null;

        if (jsonObject.has("duration")) { // Ticks
            duration = jsonObject.get("duration").getAsInt();
        }

        if (jsonObject.has("amplifier")) {
            amplifier = jsonObject.get("amplifier").getAsInt();
        }

        if (jsonObject.has("ambient")) {
            ambient = jsonObject.get("ambient").getAsBoolean();
        }

        if (jsonObject.has("particles")) {
            particles = jsonObject.get("particles").getAsBoolean();
        }

        //if (jsonObject.has("color")) color = Color.jsonObject.get("color")

        return new PotionEffect(type, duration, amplifier, ambient, particles, false);
    }
}
