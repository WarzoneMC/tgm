package network.warzone.tgm.modules.ctf.objective;

import network.warzone.tgm.modules.ctf.CTFModule;
import network.warzone.tgm.modules.flag.MatchFlag;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static network.warzone.tgm.util.ColorConverter.format;

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
    public void pickup(MatchFlag flag, Player stealer, List<PotionEffect> effects) {
        stealer.sendTitle(format("&aYou are carrying &f&l"+flag.getName()), format("&eBring it back to your base!"), 0, 100, 20);
        super.pickup(flag, stealer, effects);
        updateAllScoreboards();
    }

    @Override
    public void drop(MatchFlag flag, Player stealer, Player attacker, List<PotionEffect> effects) {
        super.drop(flag, stealer, attacker, effects);
        updateAllScoreboards();
    }

    @Override
    public void capture(MatchFlag flag, Player capturer, List<PotionEffect> effects) {
        super.capture(flag, capturer, effects);
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
            if (positionOnScoreboard != 1) scoreboard.add(StringUtils.repeat(" ", ++spaceCount), ++positionOnScoreboard);
            scoreboard.add(ChatColor.WHITE.toString() + "  " + getTeamPoints(team) + ChatColor.DARK_GRAY.toString() + "/" + ChatColor.GRAY.toString() + captureAmount + ChatColor.WHITE.toString() + " Captures", ++positionOnScoreboard);
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
