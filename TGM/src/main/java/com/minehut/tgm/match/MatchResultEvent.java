package com.minehut.tgm.match;

import com.minehut.tgm.team.MatchTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * Called when a player enters a match.
 *
 * This happens in two cases:
 * 1. A player joins the server.
 * 2. The map cycles.
 */

@AllArgsConstructor
public class MatchResultEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter private Match match;
    @Getter private MatchTeam winningTeam;
    @Getter private List<MatchTeam> losingTeams;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
