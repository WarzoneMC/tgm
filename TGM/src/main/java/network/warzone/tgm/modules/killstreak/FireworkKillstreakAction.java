package network.warzone.tgm.modules.killstreak;

import lombok.AllArgsConstructor;
import network.warzone.tgm.util.FireworkUtil;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@AllArgsConstructor
class FireworkKillstreakAction implements KillstreakAction {
    private Location locationOffset;
    private FireworkEffect fireworkEffect;
    private int power;
    @Override
    public void apply(Player killer) {
        FireworkUtil.spawnFirework(killer.getLocation().clone().add(locationOffset), fireworkEffect, power);
    }
}
