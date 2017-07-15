package com.minehut.tgm.modules.wool;

import com.minehut.tgm.TGM;
import com.minehut.tgm.modules.TimeModule;
import com.minehut.tgm.modules.region.Region;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.util.ColorConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.Wool;

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
        if (event.getBlockPlaced().getType() == Material.WOOL) {
            if ((event.getBlockPlaced().getState().getData().getData() == color)) {
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
        }
    }

    public void load() {
        TGM.registerEvents(this);
    }

    public void unload() {
        HandlerList.unregisterAll(this);
    }

    private void handleWoolPickup(Player player) {
        if(completed) return;

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
    public void onPickup(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() == Material.WOOL) {
            if (event.getItem().getItemStack().getData().getData() == color) {
                handleWoolPickup(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onTransfer(InventoryMoveItemEvent event) {
        if (event.getItem().getType() == Material.WOOL) {
            if (event.getItem().getData().getData() == color) {
                handleWoolPickup((Player) event.getInitiator());
            }
        }
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
