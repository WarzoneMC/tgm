package network.warzone.tgm.modules.base;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchStatus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A base represents an area where a Redeemable can be redeemed
 * Created by yikes on 12/15/2019
 */
public class MatchBase implements Listener {
    private Location baseLocation;
    private Match match;
    private List<PlayerRedeemable> playerRedeemables = new ArrayList<>();
    private List<ItemRedeemable> itemRedeemables = new ArrayList<>();
    private Map<Item, BukkitTask> itemTasks = new HashMap<>();

    public MatchBase(Location baseLocation, List<? extends Redeemable> redeemables) {
        this.baseLocation = baseLocation;
        this.match = TGM.get().getMatchManager().getMatch();
        for (Redeemable redeemable : redeemables) {
            if (redeemable instanceof PlayerRedeemable) {
                playerRedeemables.add((PlayerRedeemable) redeemable);
            } else if (redeemable instanceof ItemRedeemable) {
                itemRedeemables.add((ItemRedeemable) redeemable);
            }
        }
        TGM.registerEvents(this);
    }

    private List<PlayerRedeemable> hasPlayerRedeemables(Player player) {
        List<PlayerRedeemable> eligibleRedeemables = new ArrayList<>();
        for (PlayerRedeemable playerRedeemable : playerRedeemables) {
            if (playerRedeemable.hasRedeemable(player)) eligibleRedeemables.add(playerRedeemable);
        }
        return eligibleRedeemables;
    }

    private List<ItemRedeemable> applicableItemRedeemables(Item item) {
        List<ItemRedeemable> eligibleRedeemables = new ArrayList<>();
        for (ItemRedeemable itemRedeemable : itemRedeemables) {
            if (itemRedeemable.matchesRedeemable(item)) eligibleRedeemables.add(itemRedeemable);
        }
        return eligibleRedeemables;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (match.getMatchStatus() != MatchStatus.MID ||
            playerRedeemables == null ||
            playerRedeemables.size() == 0 ||
            event.getFrom().distanceSquared(baseLocation) > 1) return;
        else {
            List<PlayerRedeemable> eligiblePlayerRedeemables = hasPlayerRedeemables(event.getPlayer());
            if (eligiblePlayerRedeemables.size() == 0) return;
            redeemPlayerRedeemables(eligiblePlayerRedeemables, event.getPlayer());
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (match.getMatchStatus() != MatchStatus.MID ||
            itemRedeemables == null ||
            itemRedeemables.size() == 0 ||
            event.getPlayer().getLocation().distanceSquared(baseLocation) > 25) return;
        else {
            List<ItemRedeemable> eligibleItemRedeemables = applicableItemRedeemables(event.getItemDrop());
            if (eligibleItemRedeemables.size() == 0) return;
            Player playerInQuestion = event.getPlayer();
            Item itemInQuestion = event.getItemDrop();
            itemTasks.put(itemInQuestion, Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
                if (itemInQuestion.isDead()) {
                    itemTasks.get(itemInQuestion).cancel();
                    itemTasks.remove(itemInQuestion);
                } else if (itemInQuestion.getVelocity().getY() == 0 && itemInQuestion.isOnGround()) {
                    if (itemInQuestion.getLocation().distanceSquared(baseLocation) <= 1) {
                        itemInQuestion.remove();
                        redeemItemRedeemables(eligibleItemRedeemables, playerInQuestion);
                    } else {
                        itemTasks.get(itemInQuestion).cancel();
                        itemTasks.remove(itemInQuestion);
                    }
                }
            }, 2L, 2L));
        }
    }

    private void redeemPlayerRedeemables(List<PlayerRedeemable> filteredPlayerRedeemables, Player player) {
        for (PlayerRedeemable playerRedeemable : filteredPlayerRedeemables) playerRedeemable.redeem(player);
    }

    private void redeemItemRedeemables(List<ItemRedeemable> filteredItemRedeemables, Player player) {
        for (ItemRedeemable itemRedeemable : filteredItemRedeemables) itemRedeemable.redeem(player);
    }

    public void unload() {
        for (BukkitTask task : itemTasks.values()) task.cancel();
        itemTasks = null;
        playerRedeemables = null;
        itemRedeemables = null;
        TGM.unregisterEvents(this);
    }
}
