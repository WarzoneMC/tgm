package com.minehut.tgm.modules.countdown;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.sk89q.minecraft.util.commands.ChatColor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class StartCountdown extends Countdown {
    public static final int START_TIME = 20;
    public static final int REQUIRED_PLAYERS = 1;

    @Getter private TeamManagerModule teamManagerModule;

    @Override
    public void load(Match match) {
        teamManagerModule = match.getModule(TeamManagerModule.class);
        start(START_TIME);
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onTick() {
        if(isCancelled()) return;

//        int amountParticipating = teamManagerModule.getAmountParticipating();
//        if (amountParticipating < REQUIRED_PLAYERS) {
//            int needed = REQUIRED_PLAYERS - amountParticipating;
//            getBossBar().setProgress(1);
//            getBossBar().setTitle(ChatColor.RED + "Waiting for " + ChatColor.AQUA + needed +
//                    ChatColor.RED + " more player" + (needed == 1 ? "" : "s") + " to join");
//            getBossBar().setColor(BarColor.RED);
//
//            setTimeLeft(getTimeMax());
//            return;
//        }

        if (getTimeLeft() % 20 == 0) {
            int timeLeftSeconds = getTimeLeftSeconds();
            if(timeLeftSeconds == 0) return;

            boolean message = false;
            boolean sound = false;
            if (timeLeftSeconds <= 5 && timeLeftSeconds > 0) {
                message = true;
                sound = true;
            } else if (timeLeftSeconds % 10 == 0) {
                message = true;
                sound = true;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (message) {
                    player.sendMessage(ChatColor.GREEN + "Match starting in " + ChatColor.DARK_RED + timeLeftSeconds +
                            ChatColor.GREEN + " second" + (timeLeftSeconds > 1 ? "s" : ""));
                }

                if (sound) {
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
                }
            }
        }
    }

    @Override
    protected void onFinish() {
        TGM.get().getMatchManager().startMatch();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 2);
            if (!TGM.get().getModule(TeamManagerModule.class).getSpectators().containsPlayer(player)) {
                player.sendMessage(ChatColor.GREEN + "Match started!");
            }
        }
    }

    @Override
    protected void onCancel() {

    }
}
