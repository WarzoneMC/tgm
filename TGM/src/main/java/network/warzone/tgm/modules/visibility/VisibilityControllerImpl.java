package network.warzone.tgm.modules.visibility;

import lombok.AllArgsConstructor;
import network.warzone.tgm.modules.SpectatorModule;
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
            return spectatorModule.getSpectators().containsPlayer(eyes);
        } else {
            return true;
        }
    }
}
