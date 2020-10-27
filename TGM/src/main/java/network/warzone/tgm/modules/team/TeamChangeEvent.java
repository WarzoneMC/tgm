package network.warzone.tgm.modules.team;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.user.PlayerContext;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player enters a match.
 *
 * This happens in two cases:
 * 1. A player joins the server.
 * 2. The map cycles.
 */

@AllArgsConstructor @Getter
public class TeamChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private PlayerContext playerContext;
    private MatchTeam team;
    private MatchTeam oldTeam;

    @Setter private boolean cancelled;
    @Getter @Setter private boolean forced;
    @Getter @Setter private boolean silent;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return !isForced() && this.cancelled;
    }
}
