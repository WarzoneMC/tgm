package network.warzone.tgm.map.spawnpoints;

import lombok.AllArgsConstructor;
import org.bukkit.Location;

@AllArgsConstructor
public class LocationSpawnPoint implements SpawnPoint {
    private final Location point;

    @Override
    public Location getLocation() {
        return point;
    }
}
