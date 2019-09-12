package network.warzone.tgm.modules.team;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.user.PlayerContext;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player enters a match.
 *
 * This happens in two cases:
 * 1. A player joins the server.
 * 2. The map cycles.
 */

@Getter
public class TeamChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private PlayerContext playerContext;
    private MatchTeam team;
    private MatchTeam oldTeam;
    private boolean teleportEnabled;

    public TeamChangeEvent(PlayerContext playerContext, MatchTeam team, MatchTeam oldTeam) {
        this.playerContext = playerContext;
        this.team = team;
        this.oldTeam = oldTeam;
        this.teleportEnabled = true;
    }
    public TeamChangeEvent(PlayerContext playerContext, MatchTeam team, MatchTeam oldTeam, boolean shouldTeleport) {
        this.playerContext = playerContext;
        this.team = team;
        this.oldTeam = oldTeam;
        this.teleportEnabled = shouldTeleport;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
