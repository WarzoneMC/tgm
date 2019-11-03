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

    private boolean enable = false;

    @Override
    public void load(Match match) {
        if (!match.getMapContainer().getMapInfo().getJsonObject().has("border")) return;
        enable = true;
        JsonObject borderJson = match.getMapContainer().getMapInfo().getJsonObject().get("border").getAsJsonObject();
        if (borderJson.has("startingSize")) this.startingSize = borderJson.get("startingSize").getAsInt();
        if (borderJson.has("delay")) this.delay = borderJson.get("delay").getAsInt();
        if (borderJson.has("endSize")) this.endSize = borderJson.get("endSize").getAsInt();
        else this.endSize = this.startingSize;

        World world = match.getWorld();
        this.worldBorder = world.getWorldBorder();

        if (borderJson.has("center")) this.worldBorder.setCenter(Parser.convertLocation(world, borderJson.get("center")));
        if (borderJson.has("damage")) {
            if (borderJson.getAsJsonObject("damage").has("amount"))
                this.worldBorder.setDamageAmount(borderJson.getAsJsonObject("damage").get("amount").getAsDouble());
            if (borderJson.getAsJsonObject("damage").has("buffer"))
                this.worldBorder.setDamageBuffer(borderJson.getAsJsonObject("damage").get("buffer").getAsDouble());
        }
        if (borderJson.has("warning")) {
            if (borderJson.getAsJsonObject("warning").has("distance"))
                this.worldBorder.setWarningDistance(borderJson.getAsJsonObject("warning").get("distance").getAsInt());
            if (borderJson.getAsJsonObject("warning").has("time"))
                this.worldBorder.setWarningTime(borderJson.getAsJsonObject("warning").get("time").getAsInt());
        }
        this.worldBorder.setSize(this.startingSize);
    }

    @Override
    public void enable() {
        if (enable) this.worldBorder.setSize(this.endSize, this.delay);
    }
}
