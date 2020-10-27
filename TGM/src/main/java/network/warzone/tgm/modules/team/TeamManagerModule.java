package network.warzone.tgm.modules.team;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
        teams.add(new MatchTeam("spectators", "Spectators", ChatColor.AQUA, GameMode.ADVENTURE, true,  Integer.MAX_VALUE, 0, false));

        for (ParsedTeam parsedTeam : match.getMapContainer().getMapInfo().getTeams()) {
            teams.add(new MatchTeam(
                    parsedTeam.getId(),
                    parsedTeam.getAlias(),
                    parsedTeam.getTeamColor(),
                    parsedTeam.getTeamGamemode(),
                    false,
                    parsedTeam.getMax(),
                    parsedTeam.getMin(),
                    parsedTeam.isFriendlyFire()
            ));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();
        MatchTeam team = getTeam(player);
        if (!team.isFriendlyFire() && team.equals(getTeam(damager))) {
            event.setCancelled(true);
            event.setDamage(0);
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
        joinTeam(event.getPlayerContext(), matchTeam, true, false);
    }

    public void joinTeam(PlayerContext playerContext, MatchTeam matchTeam, boolean forced) {
        joinTeam(playerContext, matchTeam, forced, false);
    }

    public void joinTeam(PlayerContext playerContext, MatchTeam matchTeam, boolean forced, boolean silent) {
        MatchTeam oldTeam = getTeam(playerContext.getPlayer());
        if (oldTeam != null) oldTeam.removePlayer(playerContext);

        matchTeam.addPlayer(playerContext);

        TeamChangeEvent event = new TeamChangeEvent(playerContext, matchTeam, oldTeam, false, forced, silent);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            matchTeam.removePlayer(playerContext);
            if (oldTeam != null) oldTeam.addPlayer(playerContext);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
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

    public List<MatchTeam> getTeams(JsonArray jsonArray) {
        if (jsonArray == null) return new ArrayList<>();
        List<MatchTeam> teams = new ArrayList<>();
        for (JsonElement e : jsonArray) {
            if (!e.isJsonPrimitive()) continue;
            MatchTeam team = getTeamFromInput(e.getAsString());
            if (team != null) teams.add(team);
        }
        return teams;
    }
}
