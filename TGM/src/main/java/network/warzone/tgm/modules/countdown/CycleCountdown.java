package network.warzone.tgm.modules.countdown;

import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.util.BossBarUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class CycleCountdown extends BossBarCountdown {
    public static int START_TIME = TGM.get().getConfig().getInt("map.cycle-countdown");

    public CycleCountdown() {
        this.bossBar = initBossBar();
    }

    @Override
    public void disable() {
        start(START_TIME);
    }

    @Override
    public BossBar initBossBar() {
        BossBar bossBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
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

        getBossBar().setProgress((getTimeMax() - getTimeLeft()) / getTimeMax());

        if (getTimeLeft() % 20 == 0) {
            getBossBar().setTitle(ChatColor.DARK_AQUA + "Cycling to " + ChatColor.AQUA + TGM.get().getMatchManager().getNextMap().getMapInfo().getName()
                    + ChatColor.DARK_AQUA + " in " + ChatColor.DARK_RED + getTimeLeftSeconds()
                    + ChatColor.DARK_AQUA + " second" + (getTimeLeftSeconds() > 1 ? "s" : ""));
            BossBarUtil.displayForOldVersions(getBossBar());
        }
    }

    @Override
    protected void onFinish() {
        getBossBar().setVisible(false);

        TGM.get().getMatchManager().cycleNextMatch();
    }

    @Override
    protected void onCancel() {
        getBossBar().setVisible(false);
    }
}
