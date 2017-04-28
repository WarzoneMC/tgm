package com.minehut.tgm.match;

import com.minehut.tgm.TGM;
import com.minehut.tgm.map.MapContainer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by luke on 4/27/17.
 */
public class Match {
    @Getter private final UUID uuid;
    @Getter private final MatchManifest matchManifest;
    @Getter private final List<MatchModule> modules = new ArrayList<>();
    @Getter private final World world;
    @Getter private final MapContainer mapContainer;
    @Getter private MatchStatus matchStatus = MatchStatus.PRE;

    public Match(UUID uuid, MatchManifest matchManifest, World world, MapContainer mapContainer) {
        this.uuid = uuid;
        this.matchManifest = matchManifest;
        this.world = world;
        this.mapContainer = mapContainer;
    }

    /**
     * Called right after the world has loaded.
     * No players are in the world at this point.
     */
    public void load() {
        for (MatchModule module : matchManifest.allocateCoreModules()) {
            modules.add(module);
        }
        for (MatchModule module : matchManifest.allocateGameModules()) {
            modules.add(module);
        }

        /**
         * Core managers initialize off of MatchLoadEvent
         * so we call it first.
         */
        Bukkit.getPluginManager().callEvent(new MatchLoadEvent(this));

        //now load all the modules.
        for (MatchModule module : modules) {
            module.load();

            //automatically register modules that implement listener.
            if (module instanceof Listener) {
                TGM.registerEvents((Listener) module);
            }
        }

        Bukkit.getLogger().info("Loaded " + modules.size() + " modules.");
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

            if (module instanceof Listener) {
                HandlerList.unregisterAll((Listener) module);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends MatchModule> T getModule(Class<T> clazz) {
        for (MatchModule module : modules) {
            if (clazz.isInstance(module)) return ((T) module);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends MatchModule> List<T> getModules(Class<T> clazz) {
        List<T> results = new ArrayList<T>();
        for (MatchModule module : modules) {
            if (clazz.isInstance(module)) results.add((T) module);
        }
        return results;
    }
}
