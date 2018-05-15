package network.warzone.tgm.modules.team;

import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.join.MatchJoinEvent;
import network.warzone.tgm.map.ParsedTeam;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;
import network.warzone.tgm.user.PlayerContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ModuleData(load = ModuleLoadTime.EARLIEST) @Getter @Setter
public class TeamManagerModule extends MatchModule implements Listener {

    private List<MatchTeam> teams = new ArrayList<>();

    private TeamJoinController teamJoinController;

    public TeamManagerModule() {
        teamJoinController = new TeamJoinControllerImpl(this);
    }

    @Override
    public void load(Match match) {
        teams.add(new MatchTeam("spectators", "Spectators", ChatColor.AQUA, true, Integer.MAX_VALUE, 0));

        for (ParsedTeam parsedTeam : match.getMapContainer().getMapInfo().getTeams()) {
            teams.add(new MatchTeam(parsedTeam.getId(), parsedTeam.getAlias(), parsedTeam.getTeamColor(), false, parsedTeam.getMax(), parsedTeam.getMin()));
        }
    }

    @Override
    public void unload() {
        teams.clear();
    }

    public void addTeam(MatchTeam team) {
        teams.add(team);
    }

    @EventHandler
    public void onMatchJoin(MatchJoinEvent event) {
        MatchTeam matchTeam = teamJoinController.determineTeam(event.getPlayerContext());
        joinTeam(event.getPlayerContext(), matchTeam);
    }

    public void joinTeam(PlayerContext playerContext, MatchTeam matchTeam) {
        MatchTeam oldTeam = getTeam(playerContext.getPlayer());
        if (oldTeam != null) {
            oldTeam.removePlayer(playerContext);
        }

        matchTeam.addPlayer(playerContext);
        Bukkit.getPluginManager().callEvent(new TeamChangeEvent(playerContext, matchTeam, oldTeam));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        handleQuit(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        handleQuit(event.getPlayer());
    }

    private void handleQuit(Player player) {
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(player);
        MatchTeam matchTeam = getTeam(player);
        if (matchTeam != null) {
            matchTeam.removePlayer(playerContext);
        }
    }

    public MatchTeam getTeamById(String id) {
        for (MatchTeam matchTeam : teams) {
            if (matchTeam.getId().equalsIgnoreCase(id)) {
                return matchTeam;
            }
        }
        return null;
    }

    public MatchTeam getTeamByAlias(String alias) {
        for (MatchTeam matchTeam : teams) {
            if (matchTeam.getId().equalsIgnoreCase(alias)) {
                return matchTeam;
            }
        }
        return null;
    }

    /**
     * Method designed to be used when teams are specified in a command.
     *
     * Example: A user could join the "Blue Team" with any of the following.
     * 1. /join blue
     * 2. /join blue team
     * 3. /join b
     */
    public MatchTeam getTeamFromInput(String input) {
        MatchTeam found = getTeamById(input);
        if (found == null) {
            found = getTeamByAlias(input);
        } else {
            return found;
        }

        if (found == null) {
            for (MatchTeam matchTeam : teams) {
                if (matchTeam.getId().startsWith(input)) {
                    return matchTeam;
                }
            }

            for (MatchTeam matchTeam : teams) {
                if (matchTeam.getAlias().startsWith(input)) {
                    return matchTeam;
                }
            }
        } else {
            return found;
        }
        return found;
    }

    public MatchTeam getTeam(Player player) {
        for (MatchTeam matchTeam : teams) {
            if (matchTeam.containsPlayer(player)) {
                return matchTeam;
            }
        }
        return null;
    }

    public MatchTeam getSpectators() {
        for (MatchTeam matchTeam : teams) {
            if (matchTeam.isSpectator()) {
                return matchTeam;
            }
        }
        return null;
    }

    public MatchTeam getSmallestTeam() {
        MatchTeam smallest = null;

        for (MatchTeam matchTeam : teams) {
            if (!matchTeam.isSpectator()) {
                if (smallest == null) {
                    smallest = matchTeam;
                    continue;
                }
                double teamSize = ((double) matchTeam.getMembers().size()) / matchTeam.getMax();
                double smallestTeamSize = ((double) smallest.getMembers().size()) / smallest.getMax();
                if (teamSize < smallestTeamSize) {
                    smallest = matchTeam;
                    continue;
                }
                if (teamSize == smallestTeamSize && new Random().nextBoolean()) smallest = matchTeam;
            }
        }

        return smallest;
    }

    public int getAmountParticipating() {
        int amount = 0;
        for (MatchTeam matchTeam : teams) {
            if (!matchTeam.isSpectator()) {
                amount += matchTeam.getMembers().size();
            }
        }
        return amount;
    }
}
