package network.warzone.tgm.map;

import java.io.File;
import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
public interface MapLoader {
    List<MapContainer> loadMaps(File folder);
}
