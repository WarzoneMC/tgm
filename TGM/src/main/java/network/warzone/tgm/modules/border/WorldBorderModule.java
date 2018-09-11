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

    private int startingSize = 500; // Starting size in blocks radius
    private int delay = 20; // Delay in seconds
    private int endSize = 400; // End size in blocks radius

    private BukkitTask task;

    @Override
    public void load(Match match) {
        if (!match.getMapContainer().getMapInfo().getJsonObject().has("border")) return;
        JsonObject borderJson = match.getMapContainer().getMapInfo().getJsonObject().get("border").getAsJsonObject();
        if (borderJson.has("startingSize")) startingSize = borderJson.get("startingSize").getAsInt();
        if (borderJson.has("delay")) delay = borderJson.get("delay").getAsInt();
        if (borderJson.has("endSize")) endSize = borderJson.get("endSize").getAsInt();
        World world = TGM.get().getMatchManager().getMatch().getWorld();
        WorldBorder border = world.getWorldBorder();
        if (borderJson.has("center")) border.setCenter(Parser.convertLocation(world, borderJson.get("center")));
        int amount = Math.abs(startingSize - endSize) / delay;
        int rate = (startingSize - endSize) / amount;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                border.setSize(border.getSize() - rate);
                if ((rate > 0 && border.getSize() >= endSize) || rate < 0 && border.getSize() <= endSize) {
                    cancel();
                }
            }
        }.runTaskTimer(TGM.get(), 0, delay * 20L);
    }

    @Override
    public void unload() {
        if (task != null && !task.isCancelled()) {
          task.cancel();
        }
    }
}
