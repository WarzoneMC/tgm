package network.warzone.tgm.util.menu;

import network.warzone.tgm.util.itemstack.ItemFactory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Jorge on 08/24/2019
 */
public class ConfirmMenu extends PlayerMenu {

    public ConfirmMenu(Player player, ItemStack displayItem, MenuAction yes, MenuAction no) {
        super(ChatColor.UNDERLINE + "Confirm?", 9*5, player);
        setItem(13, displayItem);
        setItem(30, ItemFactory.createItem(Material.LIME_TERRACOTTA, ChatColor.GREEN + "Confirm"), yes);
        setItem(32, ItemFactory.createItem(Material.RED_TERRACOTTA, ChatColor.RED + "Cancel"), no);
    }
}
