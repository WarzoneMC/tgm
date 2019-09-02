package network.warzone.tgm.modules.killstreak;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

@AllArgsConstructor
class ItemKillstreakAction implements KillstreakAction {
    private Set<ItemStack> items;
    @Override
    public void apply(Player killer) {
        for(ItemStack item : items) killer.getInventory().addItem(item);
        killer.updateInventory();
    }
}
