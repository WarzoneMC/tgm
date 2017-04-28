package com.minehut.tgm.match;

import com.minehut.tgm.map.MapContainer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor
public class Match {
    @Getter private final UUID uuid;
    @Getter private List<MatchModule> modules = new ArrayList<>();
    @Getter private final World world;
    @Getter private final MapContainer mapContainer;

    /**
     * Called right after the world has loaded.
     * No players are in the world at this point.
     */
    public void load() {
        for (MatchModule module : modules) {
            module.load();
        }
    }

    /**
     * Called when the match starts.
     */
    public void enable() {
        for (MatchModule module : modules) {
            module.enable();
        }
    }


    /**
     * Called when the match ends.
     */
    public void disable() {
        for (MatchModule module : modules) {
            module.disable();
        }
    }

    /**
     * Called after all players have left the match.
     * This is the last call before the world is unloaded.
     */
    public void unload() {
        for (MatchModule module : modules) {
            module.unload();
        }
    }
}
