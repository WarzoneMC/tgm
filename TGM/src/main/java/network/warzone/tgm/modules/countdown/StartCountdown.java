package network.warzone.tgm.modules.countdown;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.team.event.TeamUpdateMinimumEvent;
import network.warzone.tgm.util.BossBarUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;

import java.util.List;

public class StartCountdown extends BossBarCountdown {
    @Getter
    protected int startTime = 20;
    protected int requiredPlayers = 2;

    @Getter private TeamManagerModule teamManagerModule;

    public StartCountdown() {
        this.bossBar = initBossBar();
    }

    @Override
    public void load(Match match) {
        startTime = TGM.get().getConfig().getInt("map.start-countdown", 20);
        teamManagerModule = match.getModule(TeamManagerModule.class);
        start(startTime);
        requiredPlayers = getRequiredPlayers();
    }

    @Override
    public BossBar initBossBar() {
        BossBar bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        bossBar.setVisible(false);
        return bossBar;
    }

    @EventHandler
    public void onTeamMinUpdate(TeamUpdateMinimumEvent event) {
        requiredPlayers = getRequiredPlayers();
    }

    @Override
    protected void onStart() {
        getBossBar().setVisible(true);
    }

    protected int getRequiredPlayers() {
        List<MatchTeam> teams = teamManagerModule.getTeamsParticipating();
        return Math.max(
                2,
                teams.stream()
                        .mapToInt(MatchTeam::getMin)
                        .sum()
        );
    }

    @Override
    protected void onTick() {
        if (isCancelled()) return;

        int amountParticipating = teamManagerModule.getAmountParticipating();
        if (amountParticipating < requiredPlayers) {
            int needed = requiredPlayers - amountParticipating;
            getBossBar().setProgress(1);
            getBossBar().setTitle(ChatColor.RED + "Waiting for " + ChatColor.AQUA + needed +
                    ChatColor.RED + " more player" + (needed == 1 ? "" : "s") + " to join");
            getBossBar().setColor(BarColor.RED);

            setTimeLeft(getTimeMax());
            BossBarUtil.displayForOldVersions(getBossBar());
            return;
        }

        getBossBar().setProgress((getTimeMax() - getTimeLeft()) / getTimeMax());

        if (getTimeLeft() % 20 == 0) {
            getBossBar().setColor(BarColor.GREEN);
            getBossBar().setTitle(ChatColor.GREEN + "Match starting in " + ChatColor.DARK_RED + getTimeLeftSeconds() +
                    ChatColor.GREEN + " second" + (getTimeLeftSeconds() > 1 ? "s" : ""));
            BossBarUtil.displayForOldVersions(getBossBar());
            if (getTimeLeftSeconds() <= 3) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.playSound(player.getLocation().clone().add(0.0, 100.0, 0.0), Sound.BLOCK_NOTE_BLOCK_PLING, 1000, 1);
                    if (!TGM.get().getModule(TeamManagerModule.class).getSpectators().containsPlayer(player)) {
                        player.sendTitle(ChatColor.YELLOW.toString() + getTimeLeftSeconds(), "", 0, 5, 15);
                    }
                });
            }
        }
    }

    @Override
    protected void onFinish() {
        getBossBar().setVisible(false);
        TGM.get().getMatchManager().startMatch();

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.playSound(player.getLocation().clone().add(0.0, 100.0, 0.0), Sound.BLOCK_NOTE_BLOCK_PLING, 1000f, 2f);
            if (!TGM.get().getModule(TeamManagerModule.class).getSpectators().containsPlayer(player)) {
                player.sendTitle(ChatColor.GREEN + "GO!", "", 0, 5, 15);
            }
        });
    }

    @Override
    protected void onCancel() {
        getBossBar().setVisible(false);
    }
}
