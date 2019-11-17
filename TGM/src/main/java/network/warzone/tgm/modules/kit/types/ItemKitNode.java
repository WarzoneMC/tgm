package network.warzone.tgm.modules.kit.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.modules.kit.KitNode;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.util.ColorConverter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

@AllArgsConstructor @Getter
public class ItemKitNode implements KitNode {

    private final int slot;
    private final ItemStack itemStack;
    private final boolean hasColor;

    @Override
    public void apply(Player player, MatchTeam matchTeam) {
        // Set leather armor in armor slots to team color if not set initially
        if (slot >= 100 && !hasColor && itemStack.getType().toString().contains("LEATHER_")) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
            leatherArmorMeta.setColor(ColorConverter.getColor(matchTeam.getColor()));
            itemStack.setItemMeta(leatherArmorMeta);
        }

        if (slot == 100) player.getInventory().setBoots(itemStack);
        else if (slot == 101) player.getInventory().setLeggings(itemStack);
        else if (slot == 102) player.getInventory().setChestplate(itemStack);
        else if (slot == 103) player.getInventory().setHelmet(itemStack);
        else if (slot == -106) player.getInventory().setItemInOffHand(itemStack);
        else player.getInventory().setItem(slot, itemStack);
    }
}
