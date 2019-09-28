package network.warzone.tgm.modules.kit.legacy_kits.abilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;

public class NinjaAbility extends Ability {

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!registeredPlayers.contains(event.getPlayer().getUniqueId())) return;
        Bukkit.broadcastMessage("got drop from ninja!");
    }

}
