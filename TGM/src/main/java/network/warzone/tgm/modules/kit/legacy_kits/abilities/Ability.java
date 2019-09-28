package network.warzone.tgm.modules.kit.legacy_kits.abilities;

import lombok.Getter;
import network.warzone.tgm.TGM;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by yikes on 09/27/19
 */
public abstract class Ability implements Listener {
    @Getter private Set<UUID> registeredPlayers = new HashSet<>();
    public Ability() {
        TGM.registerEvents(this);
    }

    public void terminate() {
        HandlerList.unregisterAll(this);
    }
}
