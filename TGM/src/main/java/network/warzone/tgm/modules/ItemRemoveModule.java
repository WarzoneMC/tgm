package network.warzone.tgm.modules;

import com.google.gson.JsonElement;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.util.Strings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Removes item entities that match a material type on spawn.
 */
public class ItemRemoveModule extends MatchModule implements Listener {

    private final Set<ItemRemoveInfo> removed = new HashSet<>();

    private boolean removeAll = false;

    public void add(ItemRemoveInfo info) {
        this.removed.add(info);
    }

    public void add(Material material) {
        add(new ItemRemoveInfo(material));
    }

    public void addAll(Collection<Material> materials) {
        removed.addAll(materials.stream().map(ItemRemoveInfo::new).collect(Collectors.toSet()));
    }

    public void remove(Material material) {
        ItemRemoveInfo info = hasMaterial(material);
        if (info != null) removed.remove(info);
    }

    public ItemRemoveInfo hasMaterial(Material material) {
        for (ItemRemoveInfo info : removed) {
            if (info.material == material) return info;
        }
        return null;
    }

    @Override
    public void load(Match match) {
        if (match.getMapContainer().getMapInfo().getJsonObject().has("itemremove")) {
            if (match.getMapContainer().getMapInfo().getJsonObject().get("itemremove").isJsonPrimitive()) {
                String itemVal = match.getMapContainer().getMapInfo().getJsonObject().get("itemremove").getAsString();
                if ("*".equals(itemVal)) removeAll = true;
                return;
            }
            for (JsonElement itemElement : match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("itemremove")) {
                try {
                    // 1.13 temp fix
                    if (Strings.getTechnicalName(itemElement.getAsString()).equalsIgnoreCase("WOOL")) {
                        add(Material.WHITE_WOOL);
                        add(Material.BLACK_WOOL);
                        add(Material.BLUE_WOOL);
                        add(Material.BROWN_WOOL);
                        add(Material.CYAN_WOOL);
                        add(Material.GRAY_WOOL);
                        add(Material.GREEN_WOOL);
                        add(Material.LIGHT_BLUE_WOOL);
                        add(Material.LIGHT_GRAY_WOOL);
                        add(Material.LIME_WOOL);
                        add(Material.MAGENTA_WOOL);
                        add(Material.ORANGE_WOOL);
                        return;
                    }
                    add(Material.valueOf(Strings.getTechnicalName(itemElement.getAsString())));
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(TGMPlayerDeathEvent event) {
        if (removeAll) {
            event.getDrops().clear();
            return;
        }
        List<ItemStack> toRemove = new ArrayList<>();
        for (ItemStack itemStack : event.getDrops()) {
            if (itemStack != null) {
                ItemRemoveInfo info = hasMaterial(itemStack.getType());
                if (info == null || !info.isPreventingDeathDrop()) continue;
                toRemove.add(itemStack);
            }
        }

        for (ItemStack itemStack : toRemove) {
            event.getDrops().remove(itemStack);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (removeAll) {
            event.getItemDrop().remove();
            return;
        }
        ItemRemoveInfo info = hasMaterial(event.getItemDrop().getItemStack().getType());
        if (info != null && info.isPreventingPlayerDrop()) event.getItemDrop().remove();
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (removeAll) {
            event.setCancelled(true);
            return;
        }
        ItemRemoveInfo info = hasMaterial(event.getEntity().getItemStack().getType());
        if (info != null && info.isPreventingItemSpawn()) event.setCancelled(true);
    }

    @Getter
    public static class ItemRemoveInfo {
        private final Material material;
        private boolean preventingDeathDrop = true;
        private boolean preventingPlayerDrop = true;
        private boolean preventingItemSpawn = true;

        public ItemRemoveInfo(Material material) {
            this.material = material;
        }

        public ItemRemoveInfo setPreventingDeathDrop(boolean preventingDeathDrop) {
            this.preventingDeathDrop = preventingDeathDrop;
            return this;
        }

        public ItemRemoveInfo setPreventingPlayerDrop(boolean preventingPlayerDrop) {
            this.preventingPlayerDrop = preventingPlayerDrop;
            return this;
        }

        public ItemRemoveInfo setPreventingItemSpawn(boolean preventingItemSpawn) {
            this.preventingItemSpawn = preventingItemSpawn;
            return this;
        }
    }
}
