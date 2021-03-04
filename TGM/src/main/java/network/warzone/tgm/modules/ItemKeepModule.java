package network.warzone.tgm.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.player.event.TGMPlayerRespawnEvent;
import network.warzone.tgm.util.Strings;
import network.warzone.tgm.util.itemstack.ItemFilter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by Jorge on 03/22/2020
 *
 * Keep items after death.
 *
 * To keep all items, use the keepInventory game rule ({@link GameRuleModule}).
 */
public class ItemKeepModule extends MatchModule implements Listener, ItemFilter {

    private final Set<Material> materials = new HashSet<>();

    private final Map<Player, List<ItemStack>> queued = new HashMap<>();

    @Override
    public void load(Match match) {
        JsonObject mapInfo = match.getMapContainer().getMapInfo().getJsonObject();
        if (mapInfo.has("itemkeep")) {
            for (JsonElement jsonElement : mapInfo.getAsJsonArray("itemkeep")) {
                try {
                    if (!jsonElement.isJsonPrimitive()) {
                        continue;
                    }
                    materials.add(Material.valueOf(Strings.getTechnicalName(jsonElement.getAsString())));
                } catch (Exception e) {
                    TGM.get().getPlayerManager().broadcastToAdmins(ChatColor.RED + "[JSON] Unknown material in itemkeep module: \"" + jsonElement.getAsString() + "\"");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDeath(TGMPlayerDeathEvent event) {
        List<ItemStack> queuedItems = new ArrayList<>(event.getDrops());
        filter(event.getDrops());
        queuedItems.removeAll(event.getDrops());
        this.queued.put(event.getVictim(), queuedItems);
    }

    @Override
    public void filter(List<ItemStack> items) {
        List<ItemStack> queuedItems = new ArrayList<>();
        for (ItemStack drop : items) {
            if (drop == null) continue;
            if (materials.contains(drop.getType())) queuedItems.add(drop);
        }
        items.removeAll(queuedItems);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRespawn(TGMPlayerRespawnEvent event) {
        Player player = event.getPlayer();
        List<ItemStack> queued = this.queued.get(player);
        if (queued != null) {
            Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
                for (ItemStack itemStack : queued) {
                    player.getInventory().addItem(itemStack);
                }
                this.queued.remove(player);
            }, 1L); // Delay by 1 tick to prevent missing armor points bug
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.queued.remove(event.getPlayer());
    }
}
