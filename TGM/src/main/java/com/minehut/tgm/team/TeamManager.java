package com.minehut.tgm.team;

import com.minehut.tgm.TGM;
import com.minehut.tgm.join.LoginService;
import com.minehut.tgm.join.MatchJoinEvent;
import com.minehut.tgm.map.ParsedTeam;
import com.minehut.tgm.match.MatchLoadEvent;
import com.minehut.tgm.user.PlayerContext;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class TeamManager implements Listener {
    @Getter
    private List<MatchTeam> teams = new ArrayList<>();

    @Getter @Setter private TeamJoinController teamJoinController;

    public TeamManager() {
        teamJoinController = new TeamJoinControllerImpl(this);
        TGM.registerEvents(this);
    }

    @EventHandler
    public void onMatchLoad(MatchLoadEvent event) {
        teams.clear();

        teams.add(new MatchTeam("spectators", "Spectators", ChatColor.AQUA, true, Integer.MAX_VALUE, 0));
        for (ParsedTeam parsedTeam : event.getMatch().getMapContainer().getMapInfo().getTeams()) {
            teams.add(new MatchTeam(parsedTeam.getId(), parsedTeam.getAlias(), parsedTeam.getTeamColor(), false, parsedTeam.getMax(), parsedTeam.getMin()));
        }
    }


    @EventHandler
    public void onMatchJoin(MatchJoinEvent event) {
        MatchTeam oldTeam = getTeam(event.getPlayerContext().getPlayer());
        if (oldTeam != null) {
            oldTeam.removePlayer(event.getPlayerContext());
        }

        MatchTeam matchTeam = teamJoinController.determineTeam(event.getPlayerContext());
        matchTeam.addPlayer(event.getPlayerContext());
        Bukkit.getPluginManager().callEvent(new TeamJoinEvent(event.getPlayerContext(), matchTeam));
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
            if (matchTeam.isObserver()) {
                return matchTeam;
            }
        }
        return null;
    }
}
