package network.warzone.tgm.modules.scoreboard;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.user.PlayerContext;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;

/**
 * Initializes and keeps track of player scoreboards.
 *
 * Game specific modules should tap into ScoreboardInitEvent and
 * direct access to SimpleScoreboard objects through TGM.get().getModule(ScoreboardManagerModule.class)
 * to control scoreboards as needed.
 */
@ModuleData(load = ModuleLoadTime.EARLIEST) @Getter
public class ScoreboardManagerModule extends MatchModule implements Listener {

    private HashMap<Player, SimpleScoreboard> scoreboards = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamChange(TeamChangeEvent event) {
        for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
            for (PlayerContext playerContext : matchTeam.getMembers()) {
                SimpleScoreboard simpleScoreboard = getScoreboard(playerContext.getPlayer());

                if (simpleScoreboard == null) {
                    simpleScoreboard = initScoreboard(playerContext);
                }

                if (event.getOldTeam() != null) {
                    Team old = simpleScoreboard.getScoreboard().getTeam(event.getOldTeam().getId());
                    old.removeEntry(event.getPlayerContext().getPlayer().getName());
                }

                Team to = simpleScoreboard.getScoreboard().getTeam(event.getTeam().getId());
                to.addEntry(event.getPlayerContext().getPlayer().getName());
            }
        }
    }

    /*
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onScoreboard(ScoreboardInitEvent event) {
        StringBuilder builder = new StringBuilder();
        event.getSimpleScoreboard().getScoreboard().getTeams().forEach(team -> builder.append(team.getName()).append(", "));


        Bukkit.broadcastMessage(ColorConverter.filterString("&aScoreboard:\n&r" + event.getPlayer().getName() + " - &7[&a" + builder.toString().substring(0, builder.length() - 2) + "&r&7]"));
    }
    */

    private SimpleScoreboard initScoreboard(PlayerContext playerContext) {
        SimpleScoreboard simpleScoreboard = new SimpleScoreboard(ChatColor.AQUA + "Objectives");

        for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
            registerScoreboardTeam(simpleScoreboard, matchTeam, playerContext);
        }

        Bukkit.getPluginManager().callEvent(new ScoreboardInitEvent(playerContext.getPlayer(), simpleScoreboard));

        simpleScoreboard.send(playerContext.getPlayer());
        scoreboards.put(playerContext.getPlayer(), simpleScoreboard);

        simpleScoreboard.update();

        return simpleScoreboard;
    }

    public Team registerScoreboardTeam(SimpleScoreboard simpleScoreboard, MatchTeam matchTeam, PlayerContext playerContext) {
        Team team = simpleScoreboard.getScoreboard().registerNewTeam(matchTeam.getId());
        team.setPrefix(matchTeam.getColor().toString());
        team.setCanSeeFriendlyInvisibles(false); // Fixes anti cheat entity visible when it shouldn't be
        team.setAllowFriendlyFire(false);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

        for (PlayerContext player : matchTeam.getMembers()) {
            team.addEntry(player.getPlayer().getName());
        }
        return team;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.scoreboards.remove(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        this.scoreboards.remove(event.getPlayer());
    }

    public SimpleScoreboard getScoreboard(Player player) {
        return scoreboards.get(player);
    }

    @Override
    public void unload() {
        scoreboards.clear();
    }

}
