package network.warzone.tgm.modules;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.*;
import network.warzone.tgm.modules.tasked.TaskedModule;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.util.InventoryUtil;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jorge on 10/17/2019
 */
@ModuleData(load = ModuleLoadTime.LATE)
public class InventoryPreviewModule extends MatchModule implements Listener, TaskedModule {

    private SpectatorModule spectatorModule;

    private final Map<Inventory, Inventory> inventoryClones = new HashMap<>();
    private final Map<Player, Inventory> playersViewingInventoryClones = new HashMap<>();

    @Getter private boolean enabled = true;

    @Override
    public void load(Match match) {
        super.load(match);
        JsonObject jsonObject = match.getMapContainer().getMapInfo().getJsonObject();
        if (jsonObject.has("inventoryPreview")) {
            this.enabled = !jsonObject.getAsJsonObject("inventoryPreview").has("enabled")|| jsonObject.getAsJsonObject("inventoryPreview").get("enabled").getAsBoolean();
        }
        this.spectatorModule = match.getModule(SpectatorModule.class);
    }

    @Override
    public void tick() {
        inventoryClones.keySet()
                .forEach(this::updateInventory);
    }

    @EventHandler
    public void onInteractBlock(PlayerInteractEvent event) {
        if (!this.isEnabled()) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND || !isSpectating(event.getPlayer())) return;
        if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Container) {
            Container container = (Container) event.getClickedBlock().getState();
            Inventory clone = this.inventoryClones.get(container.getInventory());
            if (clone == null) {
                clone = InventoryUtil.clone(container.getInventory());
                this.inventoryClones.put(container.getInventory(), clone);
            }
            this.playersViewingInventoryClones.put(event.getPlayer(), clone);
            event.getPlayer().openInventory(clone);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!this.isEnabled()) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND || !isSpectating(event.getPlayer())) return;
        if (event.getRightClicked() instanceof Player) {
            Player target = (Player) event.getRightClicked();
            if (isSpectating(target)) return;
            Inventory real = target.getInventory();
            Inventory clone = this.inventoryClones.get(real);
            if (clone == null) {
                clone = InventoryUtil.clonePlayerInventory(target);
                this.inventoryClones.put(real, clone);
            }
            this.playersViewingInventoryClones.put(event.getPlayer(), clone);
            event.getPlayer().openInventory(clone);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!this.isEnabled()) return;
        Player player = (Player) event.getPlayer();
        this.playersViewingInventoryClones.remove(player);
        if (!this.playersViewingInventoryClones.containsValue(event.getInventory())) {
            for (Map.Entry<Inventory, Inventory> entry : this.inventoryClones.entrySet()) {
                if (entry.getValue() == event.getInventory()) {
                    this.inventoryClones.remove(entry.getKey());
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamChange(TeamChangeEvent event) {
        if (!this.isEnabled()) return;
        if (event.isCancelled()) return;
        Inventory real = event.getPlayerContext().getPlayer().getInventory();
        if (!this.inventoryClones.containsKey(real)) return;
        Inventory clone = this.inventoryClones.get(real);
        List<Player> toRemove = new ArrayList<>();
        for (Map.Entry<Player, Inventory> entry : this.playersViewingInventoryClones.entrySet()) {
            if (entry.getValue().equals(clone)) {
                entry.getKey().closeInventory();
                toRemove.add(entry.getKey());
            }
        }
        toRemove.forEach(this.playersViewingInventoryClones::remove);
        this.inventoryClones.remove(real);
    }

    private void updateInventory(Inventory real) {
        if (!this.inventoryClones.containsKey(real)) return;
        Inventory clone = this.inventoryClones.get(real);
        clone.clear();
        if (real instanceof PlayerInventory) {
            Player player = (Player) ((PlayerInventory) real).getHolder();
            if (player != null)
                InventoryUtil.setPlayerInventoryContents(player, clone);
        } else {
            clone.setContents(real.getContents());
        }
    }

    /**
     * Different from SpectatorModule.isSpectating().
     * Respawning players should not be able to preview inventories.
     */
    public boolean isSpectating(Player player) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        return matchStatus != MatchStatus.MID || spectatorModule.getSpectators().containsPlayer(player);
    }
}
