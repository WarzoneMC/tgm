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
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static network.warzone.tgm.util.ColorConverter.format;

/**
 * Created by yikes on 12/15/2019
 */
public class CTFAmountController extends CTFController implements TimeSubscriber {
    private Map<MatchTeam, Integer> teamScores = new HashMap<>();
    private int captureAmount;
    private boolean fancyScoreboard;
    private boolean typedScoreboard;
    private TimeModule timeModule;

    public CTFAmountController(CTFControllerSubscriber subscriber, List<MatchFlag> allFlags, int captureAmount) {
        super(subscriber, allFlags);
        this.timeModule = TGM.get().getModule(TimeModule.class);
        timeModule.getTimeSubscribers().add(this);
        this.typedScoreboard = false;
        this.captureAmount = captureAmount;
    }

    @Override
    public void pickup(MatchFlag flag, Player stealer, List<PotionEffect> effects) {
        stealer.sendTitle(format("&aYou are carrying &f&l"+flag.getName()), format("&eBring it back to your base!"), 0, 100, 20);
        super.pickup(flag, stealer, effects);
    }

    @Override
    public void drop(MatchFlag flag, Player stealer, Player attacker, List<PotionEffect> effects) {
        super.drop(flag, stealer, attacker, effects);
    }

    @Override
    public void capture(MatchFlag flag, Player capturer, List<PotionEffect> effects) {
        super.capture(flag, capturer, effects);
        MatchTeam capturingTeam = teamManagerModule.getTeam(capturer);
        int currentScore = teamScores.getOrDefault(capturingTeam, 0);
        teamScores.put(capturingTeam, ++currentScore);
        updateAllScoreboards(0);
        checkGameOver();
    }

    private int getTeamPoints(MatchTeam team) {
        return teamScores.getOrDefault(team, 0);
    }

    @EventHandler
    public void onScoreboardInit(ScoreboardInitEvent event) {
        if (!typedScoreboard) {
            typedScoreboard = true;
            this.fancyScoreboard = true;
            if (teamManagerModule.getTeams().size() != 3) {
                this.fancyScoreboard = false;
            }
            if (this.fancyScoreboard) {
                for (MatchFlag flag : allFlags) {
                    if (flag.getCapturer() == null) {
                        this.fancyScoreboard = false;
                        break;
                    }
                }
            }
        }
        updateScoreboard(event.getSimpleScoreboard(), 0);
    }

    private void updateAllScoreboards(int elapsed) {
        for (SimpleScoreboard scoreboard : scoreboardManagerModule.getScoreboards().values()) {
            updateScoreboard(scoreboard, elapsed);
        }
    }

    private void updateScoreboard(SimpleScoreboard scoreboard, int elapsed) {
        scoreboard.removeAll(ScoreboardManagerModule.getReservedExclusions());

        // If the map allows for it, use a fancier PGM-style scoreboard. Otherwise default to the more basic one.
        if (this.fancyScoreboard) {
            int spaceCount = 1;
            int positionOnScoreboard = 1;
            List<MatchTeam> capturingTeams = new ArrayList<>();
            for (MatchFlag flag : allFlags) {
                MatchTeam team = flag.getTeam();
                MatchTeam capturer = flag.getCapturer();
                capturingTeams.add(capturer);
                StringBuilder flagString = new StringBuilder(team.getColor() + " ");
                if (flag.isWillRespawn()) {
                    if (flag.getSecondsUntilRespawn() < 0) {
                        flagString.append(team.getColor() + CTFModule.EMPTY_FLAG);
                    } else {
                        flagString.append(ChatColor.GRAY.toString() + flag.getSecondsUntilRespawn());
                    }
                } else if (flag.getFlagHolder() != null) {
                    if (elapsed % 2 == 0){
                        flagString.append(team.getColor());
                    } else {
                        flagString.append(ChatColor.BLACK.toString());
                    }
                    flagString.append(CTFModule.RIGHT_ARROW);
                } else {
                    flagString.append(team.getColor() + CTFModule.FULL_FLAG);
                }
                flagString.append(ChatColor.WHITE + " " + flag.getName());
                scoreboard.add(flagString.toString(),++positionOnScoreboard);
                scoreboard.add(capturer.getColor() + capturer.getAlias(), ++positionOnScoreboard);
                scoreboard.add(StringUtils.repeat(" ", ++spaceCount), ++positionOnScoreboard);
            }
            for (MatchTeam team : capturingTeams) {
                scoreboard.add(ChatColor.WHITE.toString() + getTeamPoints(team) + ChatColor.DARK_GRAY.toString() + "/" + ChatColor.GRAY.toString() + captureAmount + " " + team.getColor() + team.getAlias(), ++positionOnScoreboard);
            }
        } else {
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
        }
        scoreboard.update();
    }

    @Override
    public void processSecond(int elapsed) {
        updateAllScoreboards(elapsed);
    }

    @Override
    public MatchTeam getWinnerTeam() {
        MatchTeam teamWhoWon = null;
        int maxPoints = -1;
        for (Map.Entry<MatchTeam, Integer> entry : teamScores.entrySet()) {
            if (teamWhoWon == null) {
                teamWhoWon = entry.getKey();
                maxPoints = entry.getValue();
            } else {
                if (entry.getValue() >= captureAmount) {
                    teamWhoWon = entry.getKey();
                    break;
                }
                if (entry.getValue() >= maxPoints) {
                    teamWhoWon = entry.getKey();
                }
            }
        }
        return teamWhoWon;
    }

    private void checkGameOver() {
        MatchTeam teamWhoWon = getWinnerTeam();
        if (teamWhoWon == null || getTeamPoints(teamWhoWon) < captureAmount) return;
        super.gameOver(teamWhoWon);
    }
}
