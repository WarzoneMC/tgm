package network.warzone.tgm.util;

import com.google.common.base.Preconditions;
import network.warzone.tgm.TGM;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import javax.annotation.Nonnull;

public class FireworkUtil {
    public static @Nonnull Firework spawnFirework(@Nonnull Location location, @Nonnull FireworkEffect effect, int power) {
        Preconditions.checkNotNull(location, "location");
        Preconditions.checkNotNull(effect, "firework effect");
        Preconditions.checkArgument(power >= 0, "power must be positive");

        FireworkMeta meta = (FireworkMeta) Bukkit.getItemFactory().getItemMeta(Material.FIREWORK_ROCKET);
        meta.setPower(power > 0 ? (power - 1) : power);
        meta.addEffect(effect);

        Firework firework = (Firework) location.getWorld().spawnEntity(location.add(0.5, 0.0, 0.5), EntityType.FIREWORK);
        firework.setFireworkMeta(meta);
        if (power == 0) Bukkit.getScheduler().runTaskLater(TGM.get(), firework::detonate, 1L);

        return firework;
    }

}
