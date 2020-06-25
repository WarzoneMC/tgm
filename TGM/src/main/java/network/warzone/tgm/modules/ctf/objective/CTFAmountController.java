package network.warzone.tgm.modules.ctf.objective;

import network.warzone.tgm.modules.ctf.CTFModule;
import network.warzone.tgm.modules.flag.MatchFlag;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yikes on 12/15/2019
 */
public class CTFAmountController extends CTFController {
    private Map<MatchTeam, Integer> teamScores = new HashMap<>();
    private int captureAmount;

    public CTFAmountController(CTFControllerSubscriber subscriber, List<MatchFlag> allFlags, int captureAmount) {
        super(subscriber, allFlags);
        this.captureAmount = captureAmount;
    }

    @Override
    public void pickup(MatchFlag flag, Player stealer) {
        super.pickup(flag, stealer);
        updateAllScoreboards();
    }

    @Override
    public void drop(MatchFlag flag, Player stealer, Player attacker) {
        super.drop(flag, stealer, attacker);
        updateAllScoreboards();
    }

    @Override
    public void capture(MatchFlag flag, Player capturer) {
        super.capture(flag, capturer);
        MatchTeam capturingTeam = teamManagerModule.getTeam(capturer);
        int currentScore = teamScores.getOrDefault(capturingTeam, 0);
        teamScores.put(capturingTeam, ++currentScore);

        updateAllScoreboards();
        checkGameOver();
    }

    private int getTeamPoints(MatchTeam team) {
        return teamScores.getOrDefault(team, 0);
    }

    @EventHandler
    public void onScoreboardInit(ScoreboardInitEvent event) {
        updateScoreboard(event.getSimpleScoreboard());
    }

    private void updateAllScoreboards() {
        for (SimpleScoreboard scoreboard : scoreboardManagerModule.getScoreboards().values()) {
            updateScoreboard(scoreboard);
        }
    }

    private void updateScoreboard(SimpleScoreboard scoreboard) {
        scoreboard.removeAll(ScoreboardManagerModule.getReservedExclusions());
        int spaceCount = 1;
        int positionOnScoreboard = 1;
        for (MatchTeam team : teamManagerModule.getTeams()) {
            if (team.isSpectator()) continue;
            scoreboard.add(StringUtils.repeat(" ", ++spaceCount), ++positionOnScoreboard);
            scoreboard.add(getTeamPoints(team) + "/" + captureAmount + " captures", ++positionOnScoreboard);
            scoreboard.add(team.getColor() + team.getAlias(), ++positionOnScoreboard);
        }
        scoreboard.add(StringUtils.repeat(" ", ++spaceCount), ++positionOnScoreboard);
        boolean addedAnyFlags = false;
        for (MatchFlag flag : allFlags) {
            if (flag.getFlagHolder() == null) continue;
            if (!addedAnyFlags) addedAnyFlags = true;
            MatchTeam team = teamManagerModule.getTeam(flag.getFlagHolder());
            scoreboard.add(flag.getTeam().getColor() +
                    CTFModule.RIGHT_ARROW + " " + team.getColor() + flag.getFlagHolder().getName(), ++positionOnScoreboard);
        }
        if (addedAnyFlags) scoreboard.add(StringUtils.repeat(" ", ++spaceCount), ++positionOnScoreboard);
        scoreboard.update();
    }

    private void checkGameOver() {
        MatchTeam teamWhoWon = null;
        for (Map.Entry<MatchTeam, Integer> entry : teamScores.entrySet()) {
            if (entry.getValue() >= captureAmount) {
                teamWhoWon = entry.getKey();
                break;
            }
        }
        if (teamWhoWon == null) return;
        super.gameOver(teamWhoWon);
    }
}
