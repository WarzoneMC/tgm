package network.warzone.tgm.util.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Created by katie on 2/14/17.
 */
public interface MenuAction {
    void run(Player player, InventoryClickEvent event);
}
