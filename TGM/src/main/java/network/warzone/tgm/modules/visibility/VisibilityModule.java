package network.warzone.tgm.modules.visibility;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.SpectatorModule;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

@Getter
public class VisibilityModule extends MatchModule implements Listener {

    private VisibilityController visibilityController;

    @Override
    public void load(Match match) {
        visibilityController = new VisibilityControllerImpl(match.getModule(SpectatorModule.class));
        refreshAllPlayers();
    }

    @EventHandler
    public void onDeath(TGMPlayerDeathEvent event) {
        refreshPlayer(event.getVictim());
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (event.getOldTeam() != null) refreshPlayer(event.getPlayerContext().getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        refreshPlayer(event.getPlayer());
    }

    @EventHandler
    public void onWorldSwitch(PlayerChangedWorldEvent event) {
        refreshPlayer(event.getPlayer());
    }

    public void refreshPlayer(Player player) {
        if (player == null) return;

        // Update who can see who
        Bukkit.getOnlinePlayers().stream().filter(looker -> !looker.equals(player)).forEach(looker -> {
            if (visibilityController.canSee(looker, player)) {
                looker.showPlayer(TGM.get(), player);
            } else {
                looker.hidePlayer(TGM.get(), player);
            }

            if (visibilityController.canSee(player, looker)) {
                player.showPlayer(TGM.get(), looker);
            } else {
                player.hidePlayer(TGM.get(), looker);
            }
        });
    }

    public void refreshAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshPlayer(player);
        }
    }
}