package network.warzone.tgm.modules.killstreak;

import com.google.gson.*;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.parser.effect.EffectDeserializer;
import network.warzone.tgm.parser.item.ItemDeserializer;
import network.warzone.tgm.util.Parser;
import network.warzone.tgm.util.Strings;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Jorge on 09/21/2019
 */
public class KillstreakDeserializer implements JsonDeserializer<Killstreak> {

    @Override
    public Killstreak deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        assert json.isJsonObject();
        Match match = TGM.get().getMatchManager().getMatch();
        Killstreak killstreak = new Killstreak();
        List<KillstreakAction> killstreakActions = new ArrayList<>();

        JsonObject streakJson = json.getAsJsonObject();

        if (streakJson.has("count")) {
            killstreak.setCount(streakJson.get("count").getAsInt());
        }

        if (streakJson.has("message")) {
            killstreak.setMessage(streakJson.get("message").getAsString());
        }

        if (streakJson.has("commands")) {
            killstreak.setCommands(new ArrayList<String>() {{
                streakJson.getAsJsonArray("commands").forEach(jsonElement -> add(jsonElement.getAsString()));
            }});
        }

        if (streakJson.has("actions")) {
            JsonObject actionObj = streakJson.get("actions").getAsJsonObject();
            if (actionObj.has("fireworks")) {
                for (JsonElement jsonElem : actionObj.getAsJsonArray("fireworks")) {
                    JsonObject fireworkObj = jsonElem.getAsJsonObject();
                    String fireworkType = fireworkObj.has("type") ? Strings.getTechnicalName(fireworkObj.get("type").getAsString()) : "BALL";
                    boolean shouldTrail = fireworkObj.has("trail") && fireworkObj.get("trail").getAsBoolean();
                    boolean shouldFlicker = fireworkObj.has("flicker") && fireworkObj.get("flicker").getAsBoolean();
                    Set<Color> fireworkColors = new HashSet<>();
                    if (fireworkObj.has("colors")) {
                        for (JsonElement elem : fireworkObj.getAsJsonArray("colors")) {
                            fireworkColors.add(Color.fromRGB(elem.getAsInt()));
                        }
                    }
                    Set<Color> fadeColors = new HashSet<>();
                    if (fireworkObj.has("fadeColors")) {
                        for (JsonElement elem : fireworkObj.getAsJsonArray("fadeColors")) {
                            fadeColors.add(Color.fromRGB(elem.getAsInt()));
                        }
                    }
                    int fireworkLifetime = fireworkObj.has("lifetime") ? fireworkObj.get("lifetime").getAsInt() : 0;
                    Location locationOffset = fireworkObj.has("locationOffset") ? Parser.convertLocation(match.getWorld(), fireworkObj.get("locationOffset")) : new Location(match.getWorld(), 0.0, 0.0, 0.0);
                    killstreakActions.add(new FireworkKillstreakAction(locationOffset, FireworkEffect.builder().with(FireworkEffect.Type.valueOf(fireworkType)).trail(shouldTrail).flicker(shouldFlicker).withColor(fireworkColors).withFade(fadeColors).build(), fireworkLifetime));
                }
            }
            if (actionObj.has("items")) {
                Set<ItemStack> items = new HashSet<>();
                for(JsonElement jsonElem : actionObj.getAsJsonArray("items")) {
                    items.add(ItemDeserializer.parse(jsonElem));
                }
                killstreakActions.add(new ItemKillstreakAction(items));
            }
            if (actionObj.has("sounds")) {
                for(JsonElement jsonElem : actionObj.getAsJsonArray("sounds")) {
                    JsonObject soundObj = jsonElem.getAsJsonObject();
                    Sound theSound = Sound.valueOf(soundObj.get("sound").getAsString().toUpperCase().replace(".", "_"));
                    float volume = soundObj.has("volume") ? soundObj.get("volume").getAsFloat() : 1.0F;
                    float pitch = soundObj.has("pitch") ? soundObj.get("pitch").getAsFloat() : 1.0F;
                    SoundKillstreakAction.SoundTarget soundTarget = soundObj.has("target") ? SoundKillstreakAction.SoundTarget.valueOf(soundObj.get("target").getAsString().toUpperCase().replace(" ", "_")) : SoundKillstreakAction.SoundTarget.EVERYONE;
                    killstreakActions.add(new SoundKillstreakAction(theSound, soundTarget, volume, pitch));
                }
            }
            if (actionObj.has("effects")) {
                Set<PotionEffect> potionEffects = new HashSet<>();
                for(JsonElement jsonElem : actionObj.getAsJsonArray("effects")) {
                    if (!jsonElem.isJsonObject()) continue;
                    potionEffects.add(EffectDeserializer.parse(jsonElem.getAsJsonObject()));
                }
                killstreakActions.add(new EffectKillstreakAction(potionEffects));
            }
        }

        if (streakJson.has("repeat")) {
            killstreak.setRepeat(streakJson.get("repeat").getAsBoolean());
        }

        killstreak.setActions(killstreakActions);
        return killstreak;
    }
}
