package com.minehut.tgm.modules;

import com.minehut.teamapi.models.UserProfile;
import com.minehut.tgm.TGM;
import com.minehut.tgm.gametype.GameType;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.MatchResultEvent;
import com.minehut.tgm.modules.dtm.DTMModule;
import com.minehut.tgm.modules.monument.Monument;
import com.minehut.tgm.modules.monument.MonumentService;
import com.minehut.tgm.modules.tdm.TDMModule;
import com.minehut.tgm.user.PlayerContext;
import com.minehut.tgm.util.Levels;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitTask;

/**
 * Created by Jorge on 10/6/2017.
 */
public class StatsModule extends MatchModule {

    private Match match;

    private int xpBarTaskID;

    @Override
    public void load(Match match) {
        this.match = match;
        xpBarTaskID = Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setLevel(TGM.get().getPlayerManager().getPlayerContext(player).getUserProfile().getLevel());
                player.setExp((float) Levels.getLevelProgress(player) / 100);
            }
        }, 2, 2).getTaskId();
    }

    @Override
    public void unload() {
        this.match = null;
        Bukkit.getScheduler().cancelTask(xpBarTaskID);
    }

    // TODO: Check when a player levels up to send a message and sound.

}
