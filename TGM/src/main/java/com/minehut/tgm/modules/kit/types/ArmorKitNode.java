package com.minehut.tgm.modules.kit.types;

import com.minehut.tgm.modules.kit.KitNode;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.util.ColorConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

@AllArgsConstructor
public class ArmorKitNode implements KitNode {
    @Getter private final ArmorType armorType;
    @Getter private final ItemStack itemStack;

    @Override
    public void apply(Player player, MatchTeam matchTeam) {

        if (itemStack.getType().toString().contains("LEATHER_")) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
            leatherArmorMeta.setColor(ColorConverter.getColor(matchTeam.getColor()));
            itemStack.setItemMeta(leatherArmorMeta);
        }

        switch (armorType) {
            case HELMET:
                player.getInventory().setHelmet(itemStack);
                break;
            case CHESTPLATE:
                player.getInventory().setChestplate(itemStack);
                break;
            case LEGGINGS:
                player.getInventory().setLeggings(itemStack);
                break;
            case BOOTS:
                player.getInventory().setBoots(itemStack);
                break;
        }
    }
}
