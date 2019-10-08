package network.warzone.tgm.modules.generator;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.util.Strings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;

@Getter
public class GeneratorHologram {
    private TimeDisplay timeDisplay;
    private String baseContent;
    private Location location;
    private ArmorStand holoAnchor;

    public GeneratorHologram(String baseContent, TimeDisplay timeDisplay, Location location) {
        this.baseContent = baseContent;
        this.timeDisplay = timeDisplay;
        this.location = location;
        this.holoAnchor = location.getWorld().spawn(location.clone().add(0, 1.0, 0), ArmorStand.class);
        holoAnchor.setInvulnerable(true);
        holoAnchor.setVisible(false);
        holoAnchor.setGravity(false);
        holoAnchor.setMarker(true);
        holoAnchor.setCustomNameVisible(true);
        holoAnchor.setCustomName("");
    }

    public void displayContent(String name, Material material, int remainingTicks, int generatorLevel) {
        String timeSpan;
        int parsed;
        if (timeDisplay == TimeDisplay.MINUTES) {
            parsed = remainingTicks / 1200;
            timeSpan = parsed + " minute" + (parsed == 1 ? "" : "s");
        } else if (timeDisplay == TimeDisplay.TICKS) {
            parsed = remainingTicks;
            timeSpan = remainingTicks + " tick" + (remainingTicks == 1 ? "" : "s");
        } else {
            parsed = remainingTicks / 20;
            timeSpan = parsed + " second" + (parsed == 1 ? "" : "s");
        }
        if (parsed == 0) return;
        String formatted = baseContent
            .replace("%name%", name)
            .replace("%material%",
                    Strings.capitalizeString(material.name().toLowerCase().replace("_", " ")))
            .replace("%time%", timeSpan)
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
