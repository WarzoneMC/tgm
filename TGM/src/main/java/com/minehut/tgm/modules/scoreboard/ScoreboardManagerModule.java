package com.minehut.tgm.modules.scoreboard;

import com.minehut.tgm.TGM;
import com.minehut.tgm.join.MatchJoinEvent;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.ModuleData;
import com.minehut.tgm.match.ModuleLoadTime;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;

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

        for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
            Team team = simpleScoreboard.getScoreboard().registerNewTeam(matchTeam.getId());
            team.setPrefix(matchTeam.getColor().toString());
            team.setCanSeeFriendlyInvisibles(true);
            team.setAllowFriendlyFire(false);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

            for (PlayerContext player : matchTeam.getMembers()) {
                team.addEntry(player.getPlayer().getName());
            }
        }

        simpleScoreboard.send(event.getPlayerContext().getPlayer());
        scoreboards.add(simpleScoreboard);
    }


}
