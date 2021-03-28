package network.warzone.tgm.modules.kit;

import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.event.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.itemstack.ItemFactory;
import network.warzone.tgm.util.menu.KitEditorMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

@ModuleData(load = ModuleLoadTime.LATER)
public class KitEditorModule extends MatchModule implements Listener {

    @Getter private final ItemStack kitEditorItem;

    @Getter @Setter private static boolean enabled = true;
    @Getter @Setter private static boolean isKitEditable = true;

    @Getter private HashMap<UUID, KitEditorMenu> editorMenus;

    public KitEditorModule() {
        kitEditorItem = ItemFactory.createItem(Material.CHEST, ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "Custom Layouts");
    }

    public void load() {
        editorMenus = new HashMap<>();
    }

    @Override
    public void load(Match match) {
        load();
    }

    @Override
    public void unload() {
        for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
            if (!matchTeam.isSpectator()) continue;
            for (PlayerContext player : matchTeam.getMembers()) {
                player.getPlayer().getInventory().clear(6);
            }
        }

        for (KitEditorMenu menu : editorMenus.values()) {
            menu.disable();
        }

        editorMenus.clear();
    }

    public void applyItem() {
        for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
            if (!matchTeam.isSpectator()) continue;
            for (PlayerContext player : matchTeam.getMembers()) {
                player.getPlayer().getInventory().setItem(6, kitEditorItem);
            }
        }
    }

    @EventHandler
    public void onChange(TeamChangeEvent event) {
        if (!event.getTeam().isSpectator() || !enabled || !isKitEditable) return;

        applyItem();
    }
}
