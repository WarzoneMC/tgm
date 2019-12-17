package network.warzone.tgm.modules.ctf.objective;

import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.ctf.CTFModule;
import network.warzone.tgm.modules.flag.MatchFlag;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.time.TimeLimitService;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.modules.time.TimeSubscriber;
import network.warzone.tgm.util.Strings;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.*;

/**
 * Created by yikes on 12/15/2019
 */
public class CTFTimeController extends CTFController implements TimeLimitService, TimeSubscriber {
    private Map<MatchTeam, Integer> teamScores = new HashMap<>();
    private Set<MatchTeam> currentFlagHolders = new HashSet<>();
    private TimeModule timeModule;
    private int timeLimit;
    private int taskID;

    public CTFTimeController(CTFControllerSubscriber subscriber, List<MatchFlag> allFlags, int timeLimit) {
        super(subscriber, allFlags);
        this.timeLimit = timeLimit;
        this.timeModule = TGM.get().getModule(TimeModule.class);
        timeModule.getTimeSubscribers().add(this);
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(TGM.get(), () -> {
            if (currentFlagHolders.size() == 0) return;
            for (MatchTeam currentFlagHolder : currentFlagHolders) {
                int currentScore = teamScores.getOrDefault(currentFlagHolder, 0);
                teamScores.put(currentFlagHolder, ++currentScore);
            }
        }, 20L, 20L);
    }

    @Override
    public void pickup(MatchFlag flag, Player stealer) {
        super.pickup(flag, stealer);
        currentFlagHolders.add(flag.getTeam());
        updateAllScoreboards(getFormattedTime());
    }

    @Override
    public void drop(MatchFlag flag, Player stealer, Player attacker) {
        super.drop(flag, stealer, attacker);
        currentFlagHolders.remove(flag.getTeam());
        updateAllScoreboards(getFormattedTime());
    }

    @Override
    public void unload() {
        super.unload();
        Bukkit.getScheduler().cancelTask(taskID);
    }

    @Override
    public MatchTeam getWinnerTeam() {
        int tieCount = 0;
        MatchTeam winningTeam = null;
        int mostPoints = -1;
        for (Map.Entry<MatchTeam, Integer> teamEntry : teamScores.entrySet()) {
            if (teamEntry.getValue() > mostPoints) {
                tieCount = 0;
                winningTeam = teamEntry.getKey();
                mostPoints = teamEntry.getValue();
            } else if (teamEntry.getValue() == mostPoints) {
                tieCount++;
            }
        }
        return (tieCount > 0) ? null : winningTeam;
    }

    private int getTeamPoints(MatchTeam team) {
        return teamScores.getOrDefault(team, 0);
    }

    @EventHandler
    public void onScoreboardInit(ScoreboardInitEvent event) {
        updateScoreboard(event.getSimpleScoreboard());
    }

    private void updateScoreboard(SimpleScoreboard scoreboard) {
        updateScoreboard(scoreboard, getFormattedTime());
    }

    private void updateScoreboard(SimpleScoreboard scoreboard, String formattedRemainingTime) {
        scoreboard.removeAll(ScoreboardManagerModule.getReservedExclusions());
        int spaceCount = 1;
        int positionOnScoreboard = 1;
        scoreboard.add("Time: " + ChatColor.GREEN + formattedRemainingTime, ++positionOnScoreboard);
        for (MatchTeam team : teamManagerModule.getTeams()) {
            if (team.isSpectator()) continue;
            scoreboard.add(StringUtils.repeat(" ", ++spaceCount), ++positionOnScoreboard);
            scoreboard.add(ChatColor.LIGHT_PURPLE.toString() + getTeamPoints(team) + " points", ++positionOnScoreboard);
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

    private void updateAllScoreboards(String remainingTime) {
        for (SimpleScoreboard simpleScoreboard : scoreboardManagerModule.getScoreboards().values()) {
            updateScoreboard(simpleScoreboard, remainingTime);
        }
    }

    @Override
    public void processSecond(int elapsed) {
        String remainingTime = getFormattedTime(elapsed);
        updateAllScoreboards(remainingTime);
    }

    private String getFormattedTime() {
        return getFormattedTime((int) timeModule.getTimeElapsed());
    }

    private String getFormattedTime(int elapsed) {
        return Strings.formatTime(timeLimit - elapsed);
    }
}
