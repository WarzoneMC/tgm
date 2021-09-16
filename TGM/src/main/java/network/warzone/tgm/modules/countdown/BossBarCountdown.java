package network.warzone.tgm.modules.countdown;

import lombok.Getter;
import network.warzone.tgm.join.MatchJoinEvent;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class BossBarCountdown extends Countdown implements Listener {
    @Getter protected BossBar bossBar;

    public abstract BossBar initBossBar();

    @EventHandler
    public void onPlayerJoinMatch(MatchJoinEvent event) {
        bossBar.addPlayer(event.getPlayerContext().getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        bossBar.removePlayer(event.getPlayer());
    }

    @Override
    public void unload() {
        bossBar.removeAll();
    }
}
