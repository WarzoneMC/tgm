package network.warzone.tgm.modules.visibility;

import network.warzone.tgm.TGM;
import network.warzone.tgm.damage.grave.event.PlayerDeathEvent;
import network.warzone.tgm.join.MatchJoinEvent;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.ChatModule;
import network.warzone.tgm.modules.SpectatorModule;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VisibilityModule extends MatchModule implements Listener {
    @Getter private VisibilityController visibilityController;

    public VisibilityModule() {

    }

    @Override
    public void load(Match match) {
        visibilityController = new VisibilityControllerImpl(match.getModule(SpectatorModule.class));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        refreshPlayer(event.getPlayer());
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        refreshPlayer(event.getPlayerContext().getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMatchJoin(MatchJoinEvent event) {
        if (!TGM.get().getModule(ChatModule.class).getTeamManagerModule().getTeam(event.getPlayerContext().getPlayer()).isSpectator()) refreshPlayer(event.getPlayerContext().getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(TGM.get(), () -> refreshPlayer(event.getPlayer()), 20L);
    }

    public void refreshPlayer(Player player) {
        if (player == null) return;

        //update who can see the player.
        for (Player eyes : Bukkit.getOnlinePlayers()) {
            if (player == eyes) continue;

            boolean canSee = visibilityController.canSee(eyes, player);
            if (canSee) {
                eyes.showPlayer(player);
            } else {
                eyes.hidePlayer(player);
            }
        }

        //update who the player can see.
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player == target) continue;

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
