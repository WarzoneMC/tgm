package network.warzone.tgm.modules.countdown;

import com.sk89q.minecraft.util.commands.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class CycleCountdown extends BossBarCountdown {
    public static int START_TIME = 20;

    @Override
    public void load(Match match) {

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
        if(isCancelled()) return;

        getBossBar().setProgress((getTimeMax() - getTimeLeft()) / getTimeMax());

        if (getTimeLeft() % 20 == 0) {
            getBossBar().setTitle(ChatColor.DARK_AQUA + "Cycling to " + ChatColor.AQUA + TGM.get().getMatchManager().getNextMap().getMapInfo().getName()
                    + ChatColor.DARK_AQUA + " in " + ChatColor.DARK_RED + getTimeLeftSeconds()
                    + ChatColor.DARK_AQUA + " second" + (getTimeLeftSeconds() > 1 ? "s" : ""));

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
