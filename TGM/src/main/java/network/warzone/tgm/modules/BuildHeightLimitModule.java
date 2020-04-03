package network.warzone.tgm.modules;

import com.google.gson.JsonObject;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Created by Jorge on 11/17/2019
 */
public class BuildHeightLimitModule extends MatchModule implements Listener {

    private int limit = 255;

    @Override
    public void load(Match match) {
        JsonObject mapInfo = match.getMapContainer().getMapInfo().getJsonObject();
        if (mapInfo.has("buildHeight")) {
            this.limit = mapInfo.get("buildHeight").getAsInt();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getY() > this.limit) {
            event.getPlayer().sendMessage(ChatColor.RED + "You have reached the build height limit.");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getY() > this.limit) {
            event.getPlayer().sendMessage(ChatColor.RED + "You have reached the build height limit.");
            event.setCancelled(true);
        }
    }

}
