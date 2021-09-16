package network.warzone.tgm.map.spawnpoints;

import network.warzone.tgm.modules.region.Region;
import org.bukkit.Location;

public class RelativeRegionSpawnPoint extends RegionSpawnPoint {
    private final Location faceCoordinates;

    public RelativeRegionSpawnPoint(Region region, Location faceCoordinates) {
        this.region = region;
        this.faceCoordinates = faceCoordinates;
    }

    @Override
    public Location getLocation() {
        Location location = region.getRandomLocation();

        // Trigonometry to determine yaw and pitch
        double dX = faceCoordinates.getX() - location.getX();
        double dY = faceCoordinates.getY() - location.getY() - 1.62; // Subtract eye height
        double dZ = faceCoordinates.getZ() - location.getZ();

        double distance = Math.sqrt(dX * dX + dZ * dZ);
        float yaw = (float) (Math.atan2(dZ, dX) * 180 / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(dY, distance) * 180 / Math.PI);

        location.setYaw(yaw);
        location.setPitch(pitch);

        return location;
    }
}
