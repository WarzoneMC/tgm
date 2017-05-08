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

public class CycleCountdown extends Countdown {
    public static int START_TIME = 20;

    @Override
    public void load(Match match) {

    }

    @Override
    public void disable() {
        start(START_TIME);
    }


    @Override
    protected void onStart() {

    }

    @Override
    protected void onTick() {
        if(isCancelled()) return;

        if (getTimeLeft() % 20 == 0) {
            int timeLeftSeconds = getTimeLeftSeconds();
            if(timeLeftSeconds == 0) return;

            boolean message = false;
            boolean sound = false;
            if (timeLeftSeconds <= 5 && timeLeftSeconds > 0) {
                message = true;
            } else if (timeLeftSeconds % 10 == 0) {
                message = true;
                sound = true;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (message) {
                    player.sendMessage(ChatColor.DARK_AQUA + "Cycling to " + ChatColor.AQUA + TGM.get().getMatchManager().getNextMap().getMapInfo().getName()
                            + ChatColor.DARK_AQUA + " in " + ChatColor.DARK_RED + getTimeLeftSeconds()
                            + ChatColor.DARK_AQUA + " second" + (getTimeLeftSeconds() > 1 ? "s" : ""));
                }

                if (sound) {
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
                }
            }
        }
    }

    @Override
    protected void onFinish() {
        try {
            TGM.get().getMatchManager().cycleNextMatch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCancel() {

    }
}
