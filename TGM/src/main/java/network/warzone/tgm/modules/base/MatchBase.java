package network.warzone.tgm.modules.base;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchStatus;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A base represents an area where a PlayerRedeemable can be redeemed
 * Created by yikes on 12/15/2019
 */
public class MatchBase implements Listener {
    private Location baseLocation;
    private Match match;
    private List<PlayerRedeemable> playerRedeemables = new ArrayList<>();
    private List<ItemRedeemable> itemRedeemables = new ArrayList<>();

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

    private List<ItemRedeemable> hasItemRedeemables(Player player) {
        List<ItemRedeemable> eligibleRedeemables = new ArrayList<>();
        for (ItemRedeemable itemRedeemable : itemRedeemables) {
            if (itemRedeemable.hasRedeemable(player)) eligibleRedeemables.add(itemRedeemable);
        }
        return eligibleRedeemables;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (match.getMatchStatus() != MatchStatus.MID || event.getFrom().distanceSquared(baseLocation) > 1) return;
        else {
            List<PlayerRedeemable> eligiblePlayerRedeemables = hasPlayerRedeemables(event.getPlayer());
            if (eligiblePlayerRedeemables.size() == 0) return;
            redeemPlayerRedeemables(eligiblePlayerRedeemables, event.getPlayer());
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (match.getMatchStatus() != MatchStatus.MID || event.getItemDrop().getLocation().distanceSquared(baseLocation) > 1) return;
        else {
            List<ItemRedeemable> eligibleItemRedeemables = hasItemRedeemables(event.getPlayer());
            if (eligibleItemRedeemables.size() == 0) return;
            redeemItemRedeemables(eligibleItemRedeemables, event.getPlayer(), event.getItemDrop());
        }
    }

    private void redeemPlayerRedeemables(List<PlayerRedeemable> filteredPlayerRedeemables, Player player) {
        for (PlayerRedeemable playerRedeemable : filteredPlayerRedeemables) playerRedeemable.redeem(player);
    }

    private void redeemItemRedeemables(List<ItemRedeemable> filteredItemRedeemables, Player player, Item item) {
        for (ItemRedeemable itemRedeemable : filteredItemRedeemables) itemRedeemable.redeem(player, item);
    }

    public void unload() {
        TGM.unregisterEvents(this);
    }
}
