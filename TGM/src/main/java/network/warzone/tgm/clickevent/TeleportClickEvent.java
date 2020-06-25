package network.warzone.tgm.clickevent;

import lombok.AllArgsConstructor;
import network.warzone.tgm.match.Match;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by Jorge on 10/11/2019
 */
@AllArgsConstructor
public class TeleportClickEvent extends ClickEvent {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public TeleportClickEvent(Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    @Override
    public void run(Match match, Player player) {
        player.teleport(new Location(match.getWorld(), x, y, z, yaw, pitch));
    }
}
