package com.minehut.tgm.modules.tdm;

import com.google.gson.JsonObject;
import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.controlpoint.ControlPoint;
import com.minehut.tgm.modules.points.PointsModule;
import com.minehut.tgm.modules.scoreboard.ScoreboardInitEvent;
import com.minehut.tgm.modules.scoreboard.ScoreboardManagerModule;
import com.minehut.tgm.modules.scoreboard.SimpleScoreboard;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Jorge on 9/4/2017.
 */
public class TDMModule extends MatchModule implements Listener {
    
    @Getter private Match match;
    @Getter private PointsModule pointsModule;
    @Getter private TeamManagerModule teamManager;

    @Getter private final HashMap<MatchTeam, Integer> teamScoreboardLines = new HashMap<>();

    @Override
    public void load(Match match) {
        this.match = match;
        teamManager = TGM.get().getModule(TeamManagerModule.class);
        pointsModule = TGM.get().getModule(PointsModule.class);
        pointsModule.addService(matchTeam -> TGM.get().getMatchManager().endMatch(matchTeam));
    }

    @EventHandler
    public void onScoreboardInit(ScoreboardInitEvent event) {
        List<MatchTeam> teams = TGM.get().getModule(TeamManagerModule.class).getTeams();


        SimpleScoreboard simpleScoreboard = event.getSimpleScoreboard();

        int i = 0;
        for (MatchTeam matchTeam : teams) {
            if (matchTeam.isSpectator()) continue;
            simpleScoreboard.add(matchTeam.getColor() + getTeamScoreLine(matchTeam), i);
            teamScoreboardLines.put(matchTeam, i);
            simpleScoreboard.add(matchTeam.getColor() + matchTeam.getAlias(), i++);
            if (teams.indexOf(matchTeam) < teams.size() - 1) {
                simpleScoreboard.add(matchTeam.getColor() + " ", i++);
                i++;
            }
        }
    }

    private String getTeamScoreLine(MatchTeam matchTeam) {
        return "  " + ChatColor.RESET + pointsModule.getPoints(matchTeam) + ChatColor.GRAY + "/" + pointsModule.getTarget(matchTeam) + ChatColor.RESET + " Kills";
    }

    public void incrementPoints(MatchTeam matchTeam, int amount) {
        pointsModule.incrementPoints(matchTeam, amount);
        updateScoreboardTeamLine(matchTeam);
    }

    public void updateScoreboardTeamLine(MatchTeam matchTeam) {
        for (SimpleScoreboard simpleScoreboard : TGM.get().getModule(ScoreboardManagerModule.class).getScoreboards().values()) {
            int line = teamScoreboardLines.get(matchTeam);
            simpleScoreboard.remove(line);
            simpleScoreboard.add(getTeamScoreLine(matchTeam), line);
            simpleScoreboard.update();
        }
    }

    @EventHandler
    public void onDamage(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == null || !(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Player killer = (Player) event.getEntity().getKiller();
        MatchTeam team = teamManager.getTeam(killer);
        incrementPoints(team, 1);
    }

}
