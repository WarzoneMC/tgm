package network.warzone.tgm.modules.visibility;

import network.warzone.tgm.modules.SpectatorModule;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

/**
 * A better implementation should be made once
 * we have a setting system available.
 *
 * /toggle obs
 */

@AllArgsConstructor
public class VisibilityControllerImpl implements VisibilityController {
    private final SpectatorModule spectatorModule;

    @Override
    public boolean canSee(Player eyes, Player target) {

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
