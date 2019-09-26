package network.warzone.tgm.modules.stats;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchResultEvent;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.warzoneapi.models.UserProfile;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MatchStatsModule extends MatchModule implements Listener {
    private HashMap<UUID, PlayerStatsCache> playerStatsMap = new HashMap<>();

    @Override
    public void enable() {
        List<MatchTeam> teams = TGM.get().getMatchManager().getMatch().getModule(TeamManagerModule.class).getTeams();
        for(MatchTeam matchTeam : teams) {
            if (matchTeam.isSpectator()) continue;
            for(PlayerContext ctx : matchTeam.getMembers()) {
                UserProfile userProfile = ctx.getUserProfile();
                playerStatsMap.put(ctx.getPlayer().getUniqueId(), new PlayerStatsCache(userProfile.getKills(), userProfile.getDeaths(), userProfile.getXP()));
            }
        }
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if(!event.getTeam().isSpectator() && !playerStatsMap.containsKey(event.getPlayerContext().getPlayer().getUniqueId())) {
            UserProfile userProfile = event.getPlayerContext().getUserProfile();
            playerStatsMap.put(event.getPlayerContext().getPlayer().getUniqueId(), new PlayerStatsCache(userProfile.getKills(), userProfile.getDeaths(), userProfile.getXP()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMatchResult(MatchResultEvent event) {
        List<MatchTeam> teams = event.getMatch().getModule(TeamManagerModule.class).getTeams();
        for (MatchTeam matchTeam : teams) {
            if (matchTeam.isSpectator()) continue;
            for(PlayerContext ctx : matchTeam.getMembers()) {
                UserProfile userProfile = ctx.getUserProfile();
                PlayerStatsCache delta = calculateStatsDelta(new PlayerStatsCache(userProfile.getKills(), userProfile.getDeaths(), userProfile.getXP()), playerStatsMap.get(ctx.getPlayer().getUniqueId()));
                ctx.getPlayer().sendMessage(ChatColor.BLUE + "-------------------------------");
                ctx.getPlayer().sendMessage("   " + ChatColor.GREEN + delta.getKills() + " Kills");
                ctx.getPlayer().sendMessage("   " + ChatColor.RED + delta.getDeaths() + " Deaths");
                ctx.getPlayer().sendMessage("   " + ChatColor.AQUA + delta.getXp() + " XP");
                ctx.getPlayer().sendMessage(ChatColor.BLUE + "-------------------------------");
            }
        }
    }

    private static PlayerStatsCache calculateStatsDelta(PlayerStatsCache newData, PlayerStatsCache oldData) {
        if(oldData == null) return new PlayerStatsCache(0, 0, 0);
        return new PlayerStatsCache(newData.getKills() - oldData.getKills(), newData.getDeaths() - oldData.getDeaths(), newData.getXp() - oldData.getXp());
    }


    @Override
    public void unload() {
        playerStatsMap = null;
    }
}
