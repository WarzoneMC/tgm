package network.warzone.tgm.player.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class TGMPlayerRespawnEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

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
