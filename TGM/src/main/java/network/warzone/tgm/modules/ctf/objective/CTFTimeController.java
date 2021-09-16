package network.warzone.tgm.modules.ctf.objective;

import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.ctf.CTFModule;
import network.warzone.tgm.modules.flag.MatchFlag;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.modules.time.TimeSubscriber;
import network.warzone.tgm.util.Strings;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;

import java.util.*;

import static network.warzone.tgm.util.ColorConverter.format;

/**
 * Created by yikes on 12/15/2019
 */
public class CTFTimeController extends CTFController implements TimeSubscriber {
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
    public void pickup(MatchFlag flag, Player stealer, List<PotionEffect> effects) {
        stealer.sendTitle(format("&aYou are carrying &f&l"+flag.getName()), format("&eHold it to get points for your team!"), 0, 100, 20);
        super.pickup(flag, stealer, effects);
        currentFlagHolders.add(teamManagerModule.getTeam(stealer));
        updateAllScoreboards(getFormattedTime());
    }

    @Override
    public void drop(MatchFlag flag, Player stealer, Player attacker, List<PotionEffect> effects) {
        super.drop(flag, stealer, attacker, effects);
        currentFlagHolders.remove(flag.getTeamHolder());
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
        SimpleScoreboard simpleScoreboard = event.getSimpleScoreboard();
        simpleScoreboard.setTitle(ChatColor.AQUA + "King of the Flag");
        updateScoreboard(simpleScoreboard);
    }

    private void updateScoreboard(SimpleScoreboard scoreboard) {
        updateScoreboard(scoreboard, getFormattedTime());
    }

    private void updateScoreboard(SimpleScoreboard scoreboard, String formattedRemainingTime) {
        scoreboard.removeAll(ScoreboardManagerModule.getReservedExclusions());
        int spaceCount = 1;
        int positionOnScoreboard = 1;
        scoreboard.add(ChatColor.WHITE + "Time Left: " + ChatColor.GREEN + formattedRemainingTime, ++positionOnScoreboard);
        for (int j = teamManagerModule.getTeams().size() - 1; j >= 0; j--) {
            MatchTeam team = teamManagerModule.getTeams().get(j);
            if (team.isSpectator()) continue;
            scoreboard.add(StringUtils.repeat(" ", ++spaceCount), ++positionOnScoreboard);
            scoreboard.add(ChatColor.LIGHT_PURPLE.toString() + "  " + getTeamPoints(team) + " points", ++positionOnScoreboard);
            scoreboard.add(team.getColor() + team.getAlias(), ++positionOnScoreboard);
        }
        boolean addedAnyFlags = false;
        for (MatchFlag flag : allFlags) {
            if (flag.getFlagHolder() == null) continue;
            if (!addedAnyFlags) {
                scoreboard.add(StringUtils.repeat(" ", ++spaceCount), ++positionOnScoreboard);
                addedAnyFlags = true;
            }
            MatchTeam team = teamManagerModule.getTeam(flag.getFlagHolder());
            ChatColor flagOwnerColor = flag.getTeam() == null ? ChatColor.WHITE : flag.getTeam().getColor();
            scoreboard.add(flagOwnerColor +
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
