package com.minehut.tgm.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

/**
 * Serves as the "anchor" for maps.
 * This allows map information to be easily reloaded
 * during runtime.
 */
@AllArgsConstructor
public class MapContainer {
    @Getter private File sourceFolder;
    @Getter @Setter private MapInfo mapInfo;
}
