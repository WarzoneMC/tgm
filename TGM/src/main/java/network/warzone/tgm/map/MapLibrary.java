package network.warzone.tgm.map;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
public class MapLibrary {
    @Getter private final List<MapContainer> maps = new ArrayList<>();
    @Getter private final List<File> sources = new ArrayList<>();
    @Getter private final MapLoader mapLoader;

    public MapLibrary(FileConfiguration fileConfiguration, MapLoader mapLoader) {
        for (String s : fileConfiguration.getConfigurationSection("map").getStringList("sources")) {
            sources.add(new File(s));
            Bukkit.getLogger().info("Added map source: " + s);
        }

        this.mapLoader = mapLoader;
    }

    public void refreshMaps() {
        maps.clear();
        for (File source : sources) {
            List<MapContainer> loaded = mapLoader.loadMaps(source);
            Bukkit.getLogger().info("Found " + loaded.size() + " maps in source " + source);
            for (MapContainer mapContainer : loaded) {
                maps.add(mapContainer);
                Bukkit.getLogger().info(ChatColor.AQUA + "Loaded " + mapContainer.getMapInfo().getName() + " (" + mapContainer.getMapInfo().getVersion() + ")");
            }
        }
    }
}
