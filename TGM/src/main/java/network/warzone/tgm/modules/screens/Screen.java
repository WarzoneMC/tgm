package network.warzone.tgm.modules.screens;

import com.google.common.base.Preconditions;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jorge on 10/11/2019
 */
@Getter
public class Screen implements Listener {

    private Match match;
    private String title;
    private int size;
    private List<Button> buttons;

    private final Map<Player, Inventory> inventories = new ConcurrentHashMap<>();

    public Screen(Match match, String title, int size, List<Button> buttons) {
        Preconditions.checkArgument(title != null, "Screen must have a title");
        Preconditions.checkArgument(size % 9 == 0, "Screen size must be a multiple of 9.");
        this.match = match;
        this.title = title;
        this.size = size;
        this.buttons = buttons;
        TGM.registerEvents(this);
    }

    public Inventory openInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(player, size, ChatColor.translateAlternateColorCodes('&', title));
        for (Button button : buttons) {
            MatchTeam team = this.match.getModule(TeamManagerModule.class).getTeam(player);
            if (button.getTeams().isEmpty() || button.getTeams().contains(team)) {
                inventory.setItem(button.getSlot(), button.getItem());
            }
        }
        inventories.put(player, inventory);
        player.openInventory(inventory);
        return inventory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        for (Map.Entry<Player, Inventory> entry : inventories.entrySet()) {
            if (event.getInventory() == entry.getValue()) {
                event.setCancelled(true);
                for (Button button : buttons) {
                    if (event.getSlot() != button.getSlot()) continue;
                    MatchTeam team = this.match.getModule(TeamManagerModule.class).getTeam(player);
                    if (button.getTeams().isEmpty() || button.getTeams().contains(team)) {
                        button.getClickEvents().stream().filter(Objects::nonNull).forEach(clickEvent -> clickEvent.run(this.match, player));
                    }
                }
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        for (Map.Entry<Player, Inventory> entry : inventories.entrySet()) {
            if (event.getInventory() == entry.getValue())
                inventories.remove(entry.getKey());
        }
    }

    public void disable() {
        this.inventories.forEach((player, inventory) -> player.closeInventory());
        HandlerList.unregisterAll(this);
    }
}
