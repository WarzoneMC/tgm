package network.warzone.tgm.match;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.map.MapContainer;
import network.warzone.tgm.match.event.MatchLoadEvent;
import network.warzone.tgm.match.event.MatchPostLoadEvent;
import network.warzone.tgm.match.event.MatchPostUnloadEvent;
import network.warzone.tgm.match.event.MatchUnloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.*;

/**
 * Created by luke on 4/27/17.
 */
@Getter
public class Match {
    private final UUID uuid;
    private final MatchManifest matchManifest;
    private final Set<MatchModule> modules = new HashSet<>();
    private final World world;
    private final MapContainer mapContainer;
    @Setter private MatchStatus matchStatus = MatchStatus.PRE;

    private long startedTime;
    private long finishedTime;

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
        modules.addAll(matchManifest.allocateCoreModules(mapContainer.getMapInfo().getJsonObject()));
        modules.addAll(matchManifest.allocateGameModules());

        /*
         * Core managers initialize off of MatchLoadEvent
         * so we call it first.
         */
        try {
            Bukkit.getPluginManager().callEvent(new MatchLoadEvent(this));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        //now load all the modules.
        int listenerCount = 0;
        for (ModuleLoadTime moduleLoadTime : ModuleLoadTime.values()) {
            for (MatchModule matchModule : getModules(moduleLoadTime)) {
                try {
                    matchModule.load(this);
                } catch (Exception e) {
                    e.printStackTrace();
                    TGM.get().getPlayerManager().broadcastToAdmins(ChatColor.RED + "[JSON] Failed to parse module: " + matchModule.getClass().getSimpleName());

                    try {
                        matchModule.unload();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }

                //automatically register modules that implement listener.
                if (matchModule instanceof Listener) {
                    TGM.registerEvents((Listener) matchModule);
                    listenerCount++;
                }
            }
        }

        Bukkit.getLogger().info("Loaded " + modules.size() + " modules (" + listenerCount + " listeners)");

        try {
            Bukkit.getPluginManager().callEvent(new MatchPostLoadEvent(this));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the match starts.
     */
    public void enable() {
        setMatchStatus(MatchStatus.MID);
        startedTime = System.currentTimeMillis();

        for (MatchModule module : modules) {
            module.enable();
        }
    }


    /**
     * Called when the match ends.
     */
    public void disable() {
        setMatchStatus(MatchStatus.POST);
        finishedTime = System.currentTimeMillis();

        for (MatchModule module : modules) {
            module.disable();
        }
    }

    /**
     * Called after all players have left the match.
     * This is the last call before the world is unloaded.
     */
    public void unload() {
        try {
            Bukkit.getPluginManager().callEvent(new MatchUnloadEvent(this));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        for (MatchModule module : modules) {
            if (module instanceof Listener) {
                HandlerList.unregisterAll((Listener) module);
            }

            module.unload();
        }
        try {
            Bukkit.getPluginManager().callEvent(new MatchPostUnloadEvent(this));
        } catch (IllegalStateException e) {
            e.printStackTrace();
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
        List<T> results = new ArrayList<>();
        for (MatchModule module : modules) {
            if (clazz.isInstance(module)) results.add((T) module);
        }
        return results;
    }

    public List<MatchModule> getModules(ModuleLoadTime moduleLoadTime) {
        List<MatchModule> selected = new ArrayList<>();
        for (MatchModule matchModule : modules) {
            if (matchModule.getClass().isAnnotationPresent(ModuleData.class)) {
                if (matchModule.getClass().getAnnotation(ModuleData.class).load() == moduleLoadTime) {
                    selected.add(matchModule);
                }
            } else if (moduleLoadTime == ModuleLoadTime.NORMAL) {
                selected.add(matchModule);
            }
        }
        return selected;
    }
}
