package com.minehut.tgm.modules.scoreboard;

import com.minehut.tgm.TGM;
import com.minehut.tgm.join.MatchJoinEvent;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.ModuleData;
import com.minehut.tgm.match.ModuleLoadTime;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamChangeEvent;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
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
    @Getter private HashMap<Player, SimpleScoreboard> scoreboards = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamChange(TeamChangeEvent event) {
        for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
            for (PlayerContext playerContext : matchTeam.getMembers()) {
                SimpleScoreboard simpleScoreboard = getScoreboard(playerContext.getPlayer());

                if (simpleScoreboard == null) {
                    simpleScoreboard = initScoreboard(playerContext);
                }

                Team to = simpleScoreboard.getScoreboard().getTeam(event.getTeam().getId());
                to.addEntry(event.getPlayerContext().getPlayer().getName());

                if (event.getOldTeam() != null) {
                    Team old = simpleScoreboard.getScoreboard().getTeam(event.getOldTeam().getId());
                    old.removeEntry(playerContext.getPlayer().getName());
                }
            }
        }
    }

    private SimpleScoreboard initScoreboard(PlayerContext playerContext) {
        SimpleScoreboard simpleScoreboard = new SimpleScoreboard(ChatColor.AQUA + "Objectives");

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

        Bukkit.getPluginManager().callEvent(new ScoreboardInitEvent(playerContext.getPlayer(), simpleScoreboard));

        simpleScoreboard.send(playerContext.getPlayer());
        scoreboards.put(playerContext.getPlayer(), simpleScoreboard);

        simpleScoreboard.update();

        return simpleScoreboard;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.scoreboards.remove(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        this.scoreboards.remove(event.getPlayer());
    }

    public SimpleScoreboard getScoreboard(Player player) {
        return scoreboards.get(player);
    }
}
