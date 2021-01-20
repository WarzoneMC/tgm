package network.warzone.tgm.modules.countdown;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.util.BossBarUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class StartCountdown extends BossBarCountdown {

    public static int START_TIME = TGM.get().getConfig().getInt("map.start-countdown");
    public static final int REQUIRED_PLAYERS = 2;

    @Getter private TeamManagerModule teamManagerModule;

    public StartCountdown() {
        this.bossBar = initBossBar();
    }

    @Override
    public void load(Match match) {
        teamManagerModule = match.getModule(TeamManagerModule.class);
        start(START_TIME);
    }

    @Override
    public BossBar initBossBar() {
        BossBar bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        bossBar.setVisible(false);
        return bossBar;
    }

    @Override
    protected void onStart() {
        getBossBar().setVisible(true);
    }

    @Override
    protected void onTick() {
        if (isCancelled()) return;

        int amountParticipating = teamManagerModule.getAmountParticipating();
        if (amountParticipating < REQUIRED_PLAYERS) {
            int needed = REQUIRED_PLAYERS - amountParticipating;
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
