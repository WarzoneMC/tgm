package network.warzone.tgm.player.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class TGMPlayerRespawnEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Setter private boolean cancelled;

    private Player player;

    public TGMPlayerRespawnEvent(Player player) {
        super();

        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}