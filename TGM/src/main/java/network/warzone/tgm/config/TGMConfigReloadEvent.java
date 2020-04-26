package network.warzone.tgm.config;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Jorge on 03/31/2020
 */
public class TGMConfigReloadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
