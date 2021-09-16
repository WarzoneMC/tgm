package network.warzone.tgm.map.spawnpoints;

import network.warzone.tgm.modules.region.Region;
import org.bukkit.Location;

public class AbsoluteRegionSpawnPoint extends RegionSpawnPoint {
    private final float yaw;
    private final float pitch;

    public AbsoluteRegionSpawnPoint(Region region, float yaw, float pitch) {
        this.region = region;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public Location getLocation() {
        Location location = region.getRandomLocation();
        location.setYaw(yaw);
        location.setPitch(pitch);
        return location;
    }
}
