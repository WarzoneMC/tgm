package network.warzone.tgm.modules;

import com.google.gson.JsonElement;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.util.Strings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemRemoveModule extends MatchModule implements Listener {

    private final List<Material> removed = new ArrayList<>();

    @Override
    public void load(Match match) {
        if (match.getMapContainer().getMapInfo().getJsonObject().has("itemremove")) {
            for (JsonElement itemElement : match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("itemremove")) {
                try {
                    // 1.13 temp fix
                    if(Strings.getTechnicalName(itemElement.getAsString()).equalsIgnoreCase("WOOL")) {
                        removed.add(Material.WHITE_WOOL);
                        removed.add(Material.BLACK_WOOL);
                        removed.add(Material.BLUE_WOOL);
                        removed.add(Material.BROWN_WOOL);
                        removed.add(Material.CYAN_WOOL);
                        removed.add(Material.GRAY_WOOL);
                        removed.add(Material.GREEN_WOOL);
                        removed.add(Material.LIGHT_BLUE_WOOL);
                        removed.add(Material.LIGHT_GRAY_WOOL);
                        removed.add(Material.LIME_WOOL);
                        removed.add(Material.MAGENTA_WOOL);
                        removed.add(Material.ORANGE_WOOL);
                        return;
                    }
                    removed.add(Material.valueOf(Strings.getTechnicalName(itemElement.getAsString())));
                } catch (Exception e) {
                    TGM.get().getPlayerManager().broadcastToAdmins(ChatColor.RED + "[JSON] Unknown material in itemremove module: \"" + itemElement.getAsString() + "\"");
                }
            }
        }
    }

    @Override
    public void unload() {
        removed.clear();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        List<ItemStack> toRemove = new ArrayList<>();
        for (ItemStack itemStack : event.getDrops()) {
            if (removed.contains(itemStack.getType())) {
                toRemove.add(itemStack);
            }
        }

        for (ItemStack itemStack : toRemove) {
            event.getDrops().remove(itemStack);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (removed.contains(event.getItemDrop().getItemStack().getType())) {
            event.getItemDrop().remove();
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (removed.contains(event.getEntity().getItemStack().getType())) {
            event.setCancelled(true);
        }
    }
}
