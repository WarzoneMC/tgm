package com.minehut.tgm.map;

import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
public interface MapRotation {
    MapContainer cycle(boolean initial);
    MapContainer getNext();
    void refresh();
    List<MapContainer> getMaps();
}
