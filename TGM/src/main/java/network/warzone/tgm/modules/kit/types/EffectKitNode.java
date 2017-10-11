package network.warzone.tgm.modules.kit.types;

import network.warzone.tgm.modules.kit.KitNode;
import network.warzone.tgm.modules.team.MatchTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

@AllArgsConstructor @Getter
public class EffectKitNode implements KitNode {

    private final PotionEffect potionEffect;

    @Override
    public void apply(Player player, MatchTeam matchTeam) {
        player.addPotionEffect(potionEffect, true);
    }
}
