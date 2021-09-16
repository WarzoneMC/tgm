package network.warzone.tgm.modules.kit.classes.abilities;

import network.warzone.tgm.util.FireworkUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BuilderAbility extends Ability {
    public BuilderAbility() {
        super("Super Breaker", 20 * 10, Material.FEATHER, ChatColor.RED + "Super Breaker");
    }

    @Override
    public void onClick(final Player player) {
        player.sendMessage(ChatColor.GRAY + "You activated " + ChatColor.GREEN + "Super Breaker" + ChatColor.GRAY + " (5 seconds)");

        FireworkUtil.spawnFirework(player.getLocation(), FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.YELLOW).build(), 0);

        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20 * 5, 1));
        super.putOnCooldown(player);
    }
}
