package network.warzone.tgm.modules.generator;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.util.Strings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;

@Getter
public class GeneratorHologram {
    private TimeDisplay timeDisplay;
    @Setter private String baseContent;
    private Location location;
    private ArmorStand holoAnchor;

    public GeneratorHologram(String baseContent, TimeDisplay timeDisplay, Location location) {
        this.baseContent = baseContent;
        this.timeDisplay = timeDisplay;
        this.location = location;
        this.holoAnchor = location.getWorld().spawn(location.clone().add(0, 1.6, 0), ArmorStand.class);
        holoAnchor.setInvulnerable(true);
        holoAnchor.setVisible(false);
        holoAnchor.setGravity(false);
        holoAnchor.setMarker(true);
        holoAnchor.setCustomNameVisible(false);
        holoAnchor.setCustomName("");
    }

    public void makeVisible() {
        holoAnchor.setCustomNameVisible(true);
    }

    public void displayContent(Material material, int remainingTicks, int generatorLevel) {
        int parsed;
        String timeSpan;
        if (timeDisplay == TimeDisplay.MINUTES) {
            parsed = remainingTicks / 1200;
            timeSpan = "minute" + (parsed == 1 ? "" : "s");
        } else if (timeDisplay == TimeDisplay.TICKS) {
            parsed = remainingTicks;
            timeSpan = "minute" + (parsed == 1 ? "" : "s");

        } else {
            parsed = remainingTicks / 20;
            timeSpan = "second" + (parsed == 1 ? "" : "s");
        }
        String formatted = baseContent
            .replace("%material%",
                    Strings.capitalizeString(material.name().toLowerCase().replace("_", " ")))
            .replace("%time%", Integer.toString(parsed))
            .replace("%span%", timeSpan)
            .replace("%level%", Integer.toString(generatorLevel));
        holoAnchor.setCustomName(formatted);
    }

    // create GeneratorHologram from JSON Object
    public static GeneratorHologram deserialize(JsonObject generatorHologramObject, Location holoLocation) {
        assert generatorHologramObject.has("content") : "GeneratorHologram needs content";
        String hologramContent = ChatColor.translateAlternateColorCodes('&', generatorHologramObject.get("content").getAsString());
        TimeDisplay hologramTimeDisplay = TimeDisplay.SECONDS;
        if (generatorHologramObject.has("timeUnit")) hologramTimeDisplay = TimeDisplay.valueOf(Strings.getTechnicalName(generatorHologramObject.get("timeUnit").getAsString()));
        return new GeneratorHologram(hologramContent, hologramTimeDisplay, holoLocation);
    }
}
