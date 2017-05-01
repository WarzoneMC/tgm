package com.minehut.tgm.modules.kit.types;

import com.minehut.tgm.modules.kit.KitNode;
import com.minehut.tgm.modules.team.MatchTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class ItemKitNode implements KitNode {
    @Getter private final int slot;
    @Getter private final ItemStack itemStack;

    @Override
    public void apply(Player player, MatchTeam matchTeam) {
        player.getInventory().setItem(slot, itemStack);
    }
}
