package network.warzone.tgm.modules.visibility;

import lombok.Getter;
import network.warzone.tgm.modules.SpectatorModule;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * A better implementation should be made once
 * we have a setting system available.
 *
 * /toggle obs
 */

public class VisibilityControllerImpl implements VisibilityController {
    private final SpectatorModule spectatorModule;
    @Getter private final ArrayList<Player> vanished = new ArrayList<>();
    public VisibilityControllerImpl(SpectatorModule spectatorModule) {
        this.spectatorModule = spectatorModule;
    }
    public void addVanishedPlayer(Player vanisher) {
        vanished.add(vanisher);
    }
    public void removeVanishedPlayer(Player unvanisher) {
        vanished.remove(unvanisher);
    }

    @Override
    public boolean canSee(Player eyes, Player target) {
        if(vanished.contains(target)) return false;

        if (spectatorModule.getSpectators().containsPlayer(target)) {

            // eyes = spectator & target = spectator
            if (spectatorModule.getSpectators().containsPlayer(eyes)) {
                return true;
            }
            // eyes = player & target = spectator
            else {
                return false;
            }
        } else {

            //target = player
            return true;
        }
    }
}
