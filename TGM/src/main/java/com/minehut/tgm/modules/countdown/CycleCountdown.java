package com.minehut.tgm.modules.countdown;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.sk89q.minecraft.util.commands.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.io.IOException;

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
        try {
            TGM.get().getMatchManager().cycleNextMatch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCancel() {
        getBossBar().setVisible(false);
    }
}
