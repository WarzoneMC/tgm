package com.minehut.tgm.modules.visibility;

import com.minehut.tgm.TGM;
import com.minehut.tgm.join.MatchJoinEvent;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.SpectatorModule;
import com.minehut.tgm.team.TeamChangeEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class VisibilityModule extends MatchModule implements Listener {
    @Getter private VisibilityController visibilityController;

    public VisibilityModule() {

    }

    @Override
    public void load(Match match) {
        visibilityController = new VisibilityControllerImpl(match.getModule(SpectatorModule.class));
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        refreshPlayer(event.getPlayerContext().getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMatchJoin(MatchJoinEvent event) {
        refreshPlayer(event.getPlayerContext().getPlayer());
    }

    public void refreshPlayer(Player player) {
        if(player == null) return;

        //update who can see the player.
        for (Player eyes : Bukkit.getOnlinePlayers()) {
            if(player == eyes) continue;

            boolean canSee = visibilityController.canSee(eyes, player);
            if (canSee) {
                eyes.showPlayer(player);
            } else {
                eyes.hidePlayer(player);
            }
        }

        //update who the player can see.
        for (Player target : Bukkit.getOnlinePlayers()) {
            if(player == target) continue;

            boolean canSee = visibilityController.canSee(player, target);
            if (canSee) {
                player.showPlayer(target);
            } else {
                player.hidePlayer(target);
            }
        }
    }

    public void refreshAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshPlayer(player);
        }
    }
}
