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


}
