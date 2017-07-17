package com.minehut.tgm.modules.monument;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.MatchStatus;
import com.minehut.tgm.modules.region.Region;
import com.minehut.tgm.modules.team.MatchTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class Monument implements Listener {
    @Getter private String name;

    @Getter
    private final List<MatchTeam> owners;

    @Getter
    private final Region region;

    @Getter
    private final List<Material> materials;

    @Getter private int health;
    @Getter private int maxHealth;

    @Getter
    private final List<MonumentService> services = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (region.contains(event.getBlock().getLocation())) {
            if (materials == null || materials.contains(event.getBlock().getType())) {
                if (canDamage(event.getPlayer())) {
                    if (TGM.get().getMatchManager().getMatch().getMatchStatus().equals(MatchStatus.MID)) {
                        event.setCancelled(false); //override filters
                        event.getBlock().getDrops().clear();

                        health--;

                        if (health < 0) {
                            event.getPlayer().sendMessage(ChatColor.RED + "This monument is already destroyed.");
                        } else if (health == 0) {
                            for (MonumentService monumentService : services) {
                                monumentService.destroy(event.getPlayer(), event.getBlock());
                            }
                        } else {
                            for (MonumentService monumentService : services) {
                                monumentService.damage(event.getPlayer(), event.getBlock());
                            }
                        }

                    } else {
                        event.getPlayer().sendMessage(ChatColor.RED + "You cannot damage a monument you own.");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    public boolean isAlive() {
        return this.health > 0;
    }

    public int getHealthPercentage() {
        return Math.min(100, Math.max(0, (health * 100) / maxHealth));
    }

    public void load() {
        TGM.registerEvents(this);
    }

    public void unload() {
        HandlerList.unregisterAll(this);
    }


    public boolean canDamage(Player player) {
        for (MatchTeam matchTeam : owners) {
            if (matchTeam.containsPlayer(player)) {
                return false;
            }
        }
        return true;
    }

    public void addService(MonumentService monumentService) {
        this.services.add(monumentService);
    }
}
