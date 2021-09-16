package network.warzone.tgm.modules.killstreak;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Set;

@AllArgsConstructor
class EffectKillstreakAction implements KillstreakAction {

    private Set<PotionEffect> potionEffects;

    @Override
    public void apply(Player killer) {
        for (PotionEffect potionEffect : potionEffects) {
            killer.addPotionEffect(new PotionEffect(potionEffect.getType(), potionEffect.getDuration() * 20, potionEffect.getAmplifier(), potionEffect.isAmbient(), potionEffect.hasParticles(), false));
        }
    }
}
