package network.warzone.tgm.clickevent;

import network.warzone.tgm.match.Match;
import network.warzone.tgm.util.itemstack.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by Jorge on 10/11/2019
 */
public class TradeClickEvent extends ClickEvent {

    private List<ItemStack> give;
    private List<ItemStack> take;

    public TradeClickEvent(List<ItemStack> give, List<ItemStack> take) {
        this.give = give;
        this.take = take;
    }

    @Override
    public void run(Match match, Player player) {
        boolean canTrade = true;
        for (ItemStack itemStack : take) {
            if (!player.getInventory().containsAtLeast(itemStack, itemStack.getAmount())) {
                player.sendMessage(ChatColor.RED + "You do not have enough " + ItemUtils.itemToString(itemStack) + ".");
                canTrade = false;
            }
        }
        if (canTrade) {
            take.forEach(item -> player.getInventory().removeItem(item));
            give.forEach(item -> player.getInventory().addItem(item));
        }
    }
}
