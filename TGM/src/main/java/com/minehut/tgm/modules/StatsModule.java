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
import com.minehut.tgm.player.event.PlayerLevelUpEvent;
import com.minehut.tgm.player.event.PlayerXPEvent;
import com.minehut.tgm.user.PlayerContext;
import com.minehut.tgm.util.Levels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
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
public class StatsModule extends MatchModule implements Listener{

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

    @EventHandler
    public void onPlayerXP(PlayerXPEvent event) {
        if (Levels.calculateLevel(event.getFromXP()) < Levels.calculateLevel(event.getToXP())){
            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(event.getPlayerContext(), event.getPlayerContext().getUserProfile().getLevel() - 1, event.getPlayerContext().getUserProfile().getLevel()));
        }
    }

    @EventHandler
    public void onPlayerLevelUp(PlayerLevelUpEvent event) {
        Player player = event.getPlayerContext().getPlayer();
        player.sendMessage(ChatColor.AQUA + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
        player.sendMessage(ChatColor.GREEN +  "" +  ChatColor.BOLD + " Level up!" + ChatColor.GREEN + " You are now level " + ChatColor.RED + event.getToLevel());
        player.sendMessage(ChatColor.AQUA + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

}
