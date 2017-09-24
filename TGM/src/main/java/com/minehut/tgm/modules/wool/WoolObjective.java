package com.minehut.tgm.modules.wool;

import com.minehut.tgm.TGM;
import com.minehut.tgm.modules.TimeModule;
import com.minehut.tgm.modules.region.Region;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamChangeEvent;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.util.ColorConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Where the players need to place the wool.
 *
 * Should be used as the actual objective.
 */

public class WoolObjective implements Listener {
    @Getter private final String name;
    @Getter private final byte color;
    @Getter private final MatchTeam owner;
    @Getter private final Region podium; //where players place wool to complete objective.
    @Getter private final ChatColor chatColor;

    @Getter
    private final HashMap<UUID, Double> touches = new HashMap<>(); //saves match time

    @Getter @Setter private boolean completed = false;

    @Getter private final List<WoolObjectiveService> services = new ArrayList<>();

    public WoolObjective(String name, DyeColor color, MatchTeam owner, Region podium) {
        this.name = name;
        this.color = color.getWoolData();
        this.owner = owner;
        this.podium = podium;
        this.chatColor = ColorConverter.convertDyeColorToChatColor(color);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == Material.WOOL && event.getBlockPlaced().getState().getData().getData() == color) {
            if (!completed) {

                if (!podium.contains(event.getBlockPlaced().getLocation())) {
                    return;
                }

                if (!owner.containsPlayer(event.getPlayer())) {
                    return;
                }

                event.setCancelled(false); //override filter
                setCompleted(true);

                TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
                MatchTeam matchTeam = teamManagerModule.getTeam(event.getPlayer());

                for (WoolObjectiveService woolObjectiveService : services) {
                    woolObjectiveService.place(event.getPlayer(), matchTeam, event.getBlock());
                }
            }
        }
        else {
            if (podium.contains(event.getBlockPlaced().getLocation())){
                event.setCancelled(true);
            }
        }
    }

    public void load() {
        TGM.registerEvents(this);
    }

    public void unload() {
        HandlerList.unregisterAll(this);
    }

    private void handleWoolPickup(Player player) {
        if (completed) return;

        if (!owner.containsPlayer(player)) {
            return;
        }

        if (touches.containsKey(player.getUniqueId())) {
            return;
        }
        boolean firstTouch = touches.isEmpty();
        touches.put(player.getUniqueId(), TGM.get().getModule(TimeModule.class).getTimeElapsed());

        TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        MatchTeam matchTeam = teamManagerModule.getTeam(player);

        for (WoolObjectiveService woolObjectiveService : services) {
            woolObjectiveService.pickup(player, matchTeam, firstTouch);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getItem() != null && event.getItem().getItemStack().getType() == Material.WOOL) {
                if (event.getItem().getItemStack().getData().getData() == color) {
                    handleWoolPickup(((Player) event.getEntity()).getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onCollect(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.WOOL) {
            if (event.getCurrentItem().getData().getData() == color) {
                handleWoolPickup((Player) event.getWhoClicked());
            }
        }
    }

    private void handleWoolDrop(Player player) {
        if (completed) return;

        if (!owner.containsPlayer(player)) {
            return;
        }

        if (!touches.containsKey(player.getUniqueId())) {
            return;
        }
        touches.remove(player.getUniqueId());
        boolean broadcast = touches.isEmpty();

        TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        MatchTeam matchTeam = teamManagerModule.getTeam(player);

        for (WoolObjectiveService woolObjectiveService : services) {
            woolObjectiveService.drop(player, matchTeam, broadcast);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        handleWoolDrop(event.getEntity());
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event){
        handleWoolDrop(event.getPlayerContext().getPlayer());
        if (touches.containsKey(event.getPlayerContext().getPlayer().getUniqueId())) {
            touches.remove(event.getPlayerContext().getPlayer().getUniqueId());
        }
        for (WoolObjectiveService woolObjectiveService : services) {
            woolObjectiveService.drop(event.getPlayerContext().getPlayer(), event.getOldTeam(), false);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event){
        handleWoolDrop(event.getPlayer());
    }

    public WoolStatus getStatus() {
        if (touches.isEmpty()) {
            return WoolStatus.UNTOUCHED;
        }

        if (completed) {
            return WoolStatus.COMPLETED;
        } else {
            return WoolStatus.TOUCHED;
        }
    }

    public void addService(WoolObjectiveService woolObjectiveService) {
        this.services.add(woolObjectiveService);
    }
}
