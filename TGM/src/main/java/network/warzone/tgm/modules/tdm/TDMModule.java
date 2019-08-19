package network.warzone.tgm.modules.tdm;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.points.PointsModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.time.TimeModule;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Jorge on 9/4/2017.
 */
@Getter
public class TDMModule extends MatchModule implements Listener {
    
    private Match match;
    private PointsModule pointsModule;
    private TeamManagerModule teamManager;
    private TDMObjective tdmObjective = TDMObjective.KILLS;

    private final HashMap<MatchTeam, Integer> teamScoreboardLines = new HashMap<>();

    @Override
    public void load(Match match) {
        this.match = match;
        teamManager = TGM.get().getModule(TeamManagerModule.class);

        if (match.getMapContainer().getMapInfo().getJsonObject().has("tdm")) {
            JsonObject tdmJson = match.getMapContainer().getMapInfo().getJsonObject().get("tdm").getAsJsonObject();
            if (tdmJson.has("objective")) {
                TDMObjective objective = TDMObjective.valueOf(tdmJson.get("objective").getAsString().toUpperCase());
                if (objective != null) tdmObjective = objective;
            }
        }

        pointsModule = TGM.get().getModule(PointsModule.class);
        pointsModule.addService(matchTeam -> TGM.get().getMatchManager().endMatch(matchTeam));

        TGM.get().getModule(TimeModule.class).setTimeLimitService(this::getHighestPointsTeam);
    }

    @Override
    public void unload() {
        teamScoreboardLines.clear();
    }

    private MatchTeam getHighestPointsTeam() {
        Map.Entry<MatchTeam, Integer> highest = null;
        for (Map.Entry<MatchTeam, Integer> entry : pointsModule.getPoints().entrySet()) {
            if (highest == null) {
                highest = entry;
                continue;
            }
            if (entry.getValue() > highest.getValue()) {
                highest = entry;
            }
        }
        if (highest != null) {
            final Map.Entry<MatchTeam, Integer> entry = highest;
            int amount = pointsModule.getPoints().entrySet().stream().filter(en -> entry.getValue() == en.getValue()).collect(Collectors.toList()).size();
            if (amount > 1) return null;
            else return entry.getKey();
        }
        return null;
    }

    @EventHandler
    public void onScoreboardInit(ScoreboardInitEvent event) {
        List<MatchTeam> teams = TGM.get().getModule(TeamManagerModule.class).getTeams();

        SimpleScoreboard simpleScoreboard = event.getSimpleScoreboard();

        int i = 0;
        for (MatchTeam matchTeam : teams) {
            if (matchTeam.isSpectator()) continue;
            simpleScoreboard.add(matchTeam.getColor() + getTeamScoreLine(matchTeam), i);
            teamScoreboardLines.put(matchTeam, i++);
            simpleScoreboard.add(matchTeam.getColor() + matchTeam.getAlias(), i++);
            if (teams.indexOf(matchTeam) < teams.size() - 1) {
                simpleScoreboard.add(matchTeam.getColor() + " ", i++);
            }
        }
    }

    private String getTeamScoreLine(MatchTeam matchTeam) {
        return "  " + ChatColor.RESET + pointsModule.getPoints(matchTeam) + ChatColor.GRAY + "/" + pointsModule.getTarget(matchTeam) + ChatColor.WHITE + " Kills";
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
    public void onDeath(PlayerDeathEvent event) {
        if (tdmObjective.equals(TDMObjective.DEATHS)) {
            MatchTeam team = teamManager.getTeam(event.getEntity());
            for (MatchTeam matchTeam : teamManager.getTeams()) {
                if (!matchTeam.equals(team) && !matchTeam.isSpectator()) {
                    incrementPoints(matchTeam, 1);
                }
            }
            return;
        }

        if (event.getEntity().getKiller() == null || !(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Player killer = event.getEntity().getKiller();
        MatchTeam team = teamManager.getTeam(killer);
        incrementPoints(team, 1);
    }

}
