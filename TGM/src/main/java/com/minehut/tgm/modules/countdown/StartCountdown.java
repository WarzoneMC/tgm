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

public class StartCountdown extends BossBarCountdown {
    public static int START_TIME = 20;

    @Override
    public void load(Match match) {
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
        if(isCancelled()) return;

        getBossBar().setProgress((getTimeMax() - getTimeLeft()) / getTimeMax());

        if (getTimeLeft() % 20 == 0) {
            getBossBar().setTitle(ChatColor.GREEN + "Match starting in " + ChatColor.DARK_RED + getTimeLeftSeconds() +
                    ChatColor.GREEN + " second" + (getTimeLeftSeconds() > 1 ? "s" : ""));

            if (getTimeLeftSeconds() <= 3) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
                    if (!TGM.get().getModule(TeamManagerModule.class).getSpectators().containsPlayer(player)) {
                        player.sendTitle(ChatColor.YELLOW.toString() + getTimeLeftSeconds(), "", 0, 5, 15);
                    }
                }
            }
        }
    }

    @Override
    protected void onFinish() {
        getBossBar().setVisible(false);
        TGM.get().getMatchManager().startMatch();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 2);
            if (!TGM.get().getModule(TeamManagerModule.class).getSpectators().containsPlayer(player)) {
                player.sendTitle(ChatColor.GREEN + "GO!", "", 0, 5, 15);
            }
        }
    }

    @Override
    protected void onCancel() {
        getBossBar().setVisible(false);
    }
}
