package com.minehut.tgm.modules.countdown;

import com.minehut.tgm.join.MatchJoinEvent;
import lombok.Getter;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class BossBarCountdown extends Countdown implements Listener {
    @Getter private BossBar bossBar;

    public BossBarCountdown() {
        this.bossBar = initBossBar();
    }

    public abstract BossBar initBossBar();

    @EventHandler
    public void onBossBarCountdownMatchJoin(MatchJoinEvent event) {
        bossBar.addPlayer(event.getPlayerContext().getPlayer());
    }

    @EventHandler
    public void onBossBarCountdownQuit(PlayerQuitEvent event) {
        bossBar.removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onBossBarCountdownKick(PlayerKickEvent event) {
        bossBar.removePlayer(event.getPlayer());
    }

    @Override
    public void unload() {
        bossBar.removeAll();
    }
}
