package network.warzone.tgm.modules.base;

import org.bukkit.entity.Player;

/**
 * A redeemable is an object a player redeems at a base
 *
 * Created by yikes on 12/15/2019
 */
public abstract class Redeemable {
    /**
     * Checks if player has the redeemable
     * @param player Player in question
     * @return Whether the player has it
     */
    public abstract boolean hasRedeemable(Player player);
}
