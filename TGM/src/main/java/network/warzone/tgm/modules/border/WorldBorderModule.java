package network.warzone.tgm.modules.border;

import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.util.Parser;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class WorldBorderModule extends MatchModule {

    private int startingSize = 60_000_000; // Starting size in length of one side
    private int delay = 20; // Delay in seconds
    private int endSize; // End size in length of one side

    private WorldBorder worldBorder;

    @Override
    public void load(Match match) {
        if (!match.getMapContainer().getMapInfo().getJsonObject().has("border")) return;
        JsonObject borderJson = match.getMapContainer().getMapInfo().getJsonObject().get("border").getAsJsonObject();
        if (borderJson.has("startingSize")) this.startingSize = borderJson.get("startingSize").getAsInt();
        if (borderJson.has("delay")) this.delay = borderJson.get("delay").getAsInt();
        if (borderJson.has("endSize")) this.endSize = borderJson.get("endSize").getAsInt();
        else this.endSize = this.startingSize;

        World world = match.getWorld();
        this.worldBorder = world.getWorldBorder();

        if (borderJson.has("center")) this.worldBorder.setCenter(Parser.convertLocation(world, borderJson.get("center")));
        this.worldBorder.setSize(this.startingSize);
    }

    @Override
    public void enable() {
        this.worldBorder.setSize(this.endSize, this.delay);
    }
}
