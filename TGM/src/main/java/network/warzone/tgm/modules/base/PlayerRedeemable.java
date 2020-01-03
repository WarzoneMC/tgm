package network.warzone.tgm.modules.base;

import org.bukkit.entity.Player;

/**
 * Redeemable redeemed by presence of a player
 * Created by yikes on 12/15/2019
 */
public abstract class PlayerRedeemable extends Redeemable {
    /**
     * Called when the redeemable is redeemed
     * Criteria of player should be invalidated when this method is called
     *
     * @param player Player who has redeemed
     */
    public abstract void redeem(Player player);

}
