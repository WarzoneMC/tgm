package com.minehut.tgm.modules.scoreboard;

import com.minehut.tgm.join.MatchJoinEvent;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.ModuleData;
import com.minehut.tgm.match.ModuleLoadTime;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes and keeps track of player scoreboards.
 *
 * Game specific modules should tap into ScoreboardInitEvent and
 * direct access to SimpleScoreboard objects through TGM.get().getModule(ScoreboardManagerModule.class)
 * to control scoreboards as needed.
 */
@ModuleData(load = ModuleLoadTime.EARLIEST)
public class ScoreboardManagerModule extends MatchModule implements Listener {
    private List<SimpleScoreboard> scoreboards = new ArrayList<>();

    @EventHandler
    public void onJoin(MatchJoinEvent event) {
        SimpleScoreboard simpleScoreboard = new SimpleScoreboard(ChatColor.AQUA + "Objectives");
        Bukkit.getPluginManager().callEvent(new ScoreboardInitEvent(event.getPlayerContext().getPlayer(), simpleScoreboard));
        simpleScoreboard.send(event.getPlayerContext().getPlayer());
        scoreboards.add(simpleScoreboard);
    }


}
