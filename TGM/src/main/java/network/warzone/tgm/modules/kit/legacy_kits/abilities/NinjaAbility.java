package network.warzone.tgm.modules.kit.legacy_kits.abilities;

import network.warzone.tgm.TGM;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

public class NinjaAbility extends Ability {

    public NinjaAbility() {
        super("Star Spin", 20 * 12, Material.FLINT, ChatColor.GRAY + "Ninja Ability");
    }

    @Override
    public void onClick(Player player) {
        Location transform = player.getLocation().clone();
        float startYaw = player.getEyeLocation().getYaw();
        for (int i = 0; i < 9; i++) {
            final int n = i;
            Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
                float yaw = startYaw + ((n * 10) - 40);
                transform.setYaw(yaw);
                Location tp = player.getLocation().clone();
                tp.setYaw(yaw);
                player.teleport(tp);
                Location spawn = transform.clone().add(0, 1.5, 0).add(transform.getDirection());
                Arrow arrow = player.getWorld().spawnArrow(spawn, transform.getDirection().clone().multiply(4.0), 1, 0);
                arrow.setShooter(player);
                if (n == 8) {
                    Location start = player.getLocation().clone();
                    start.setYaw(startYaw);
                    player.teleport(start);
                }
            }, (long)n);
        }
        super.putOnCooldown(player);
    }

}
