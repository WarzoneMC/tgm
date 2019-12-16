package network.warzone.tgm.modules.base;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Redeemable redeemed by presence of an item
 * Created by yikes on 12/15/2019
 */
public abstract class ItemRedeemable extends Redeemable {
    private ItemStack redeemableItem;

    /**
     * Does the item in question match the item specified by the redeemable?
     * @param item Item in question
     * @return The answer
     */
    public boolean matchesRedeemable(Item item) {
        return item.getItemStack().isSimilar(redeemableItem);
    }


    /**
     * Called when the redeemable is redeemed, with dropped item
     * Criteria of player should be invalidated when this method is called
     *
     * @param player Player who has redeemed
     */
    public abstract void redeem(Player player);
}
