package network.warzone.tgm.modules.kit.legacy_kits;

import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;
import network.warzone.tgm.modules.kit.legacy_kits.abilities.AbilityManager;
import network.warzone.tgm.modules.kit.legacy_kits.abilities.NinjaAbility;
import network.warzone.tgm.modules.kit.legacy_kits.abilities.PhoenixAbility;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.Strings;
import network.warzone.tgm.util.itemstack.ItemFactory;
import network.warzone.warzoneapi.client.http.HttpClient;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by yikes on 09/27/19
 */
@ModuleData(load = ModuleLoadTime.EARLIER)
public class LegacyKitModule extends MatchModule implements Listener {
    public static String DEFAULT_KIT = "PHOENIX";

    public enum LegacyKitStore {
        PHOENIX(PhoenixKit.class,
                ItemFactory.createItem(Material.BLAZE_POWDER,
                        ChatColor.GOLD + "Phoenix",
                        Arrays.asList(ChatColor.YELLOW + "it fiery af")), 50),

        NINJA(NinjaKit.class,
                ItemFactory.createItem(Material.FLINT,
                ChatColor.DARK_GRAY + "Ninja",
                    Arrays.asList(ChatColor.YELLOW + "it shoot like arrows i think")), 50);

        @Getter private Class hostKit;
        @Getter private ItemStack menuItem;
        @Getter @Setter private int cost;

        LegacyKitStore(Class hostKit, ItemStack menuItem, int cost) {
            this.hostKit = hostKit;
            this.menuItem = menuItem;
            this.cost = cost;
        }

        public String getDisplayName() {
            if (menuItem.getItemMeta() == null) return Strings.capitalizeString(this.name().toLowerCase());
            return menuItem.getItemMeta().getDisplayName();
        }

        public static void adjustCosts() {
            // set costs to 0 if offline
            if (!(TGM.get().getTeamClient() instanceof HttpClient))
                for(LegacyKitStore legacyKitStore : LegacyKitStore.values()) legacyKitStore.setCost(0);
        }
    }


    @Getter private HashMap<UUID, String> kitSwitches = new HashMap<>();;
    private AbilityManager abilityManager;
    private TeamManagerModule teamManagerModule;
    private Set<LegacyKit> legacyKitSet = new HashSet<>();;

    @Override
    public void load(Match match) {
        abilityManager = new AbilityManager();
        teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        legacyKitSet.add(new PhoenixKit(abilityManager.getAbility(PhoenixAbility.class)));
        legacyKitSet.add(new NinjaKit(abilityManager.getAbility(NinjaAbility.class)));
    }

    @Override
    public void disable() {
        abilityManager.destroyAbilities();
        legacyKitSet = null;
        kitSwitches = null;
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (event.getTeam().isSpectator()) {
            removeLegacyKitForPlayer(event.getPlayerContext());
        } else if (event.getOldTeam().isSpectator()) {
            setupLegacyKitForPlayer(event.getPlayerContext());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeLegacyKitForPlayer(TGM.get().getPlayerManager().getPlayerContext(event.getPlayer()));
    }


    public void addSwitchKitRequest(PlayerContext p, String desiredKit) {
        addSwitchKitRequest(p, desiredKit, true);
    }

    public void addSwitchKitRequest(PlayerContext p, String desiredKit, boolean showMsg) {
        MatchTeam team = teamManagerModule.getTeam(p.getPlayer());
        if(team == null || team.isSpectator()) {
            setLegacyKitForPlayer(p, desiredKit);
        } else {
            if (showMsg) p.getPlayer().sendMessage(ChatColor.GREEN + "Kit will be applied when you respawn!");
            kitSwitches.put(p.getPlayer().getUniqueId(), desiredKit);
        }
    }

    private void setupLegacyKitForPlayer(PlayerContext playerContext) {
        if (playerContext.getCurrentLegacyKit() == null) playerContext.setCurrentLegacyKit(DEFAULT_KIT);
        LegacyKit newLegacyKit = getLegacyKit(playerContext.getCurrentLegacyKit());
        newLegacyKit.addToAbilityCaches(playerContext.getPlayer());

        // forget about respawn switcher kit if it exists
        kitSwitches.remove(playerContext.getPlayer().getUniqueId());
    }

    public void setLegacyKitForPlayer(PlayerContext playerContext, String chosenKitString) {
        LegacyKit oldLegacyKit = getLegacyKit(playerContext.getCurrentLegacyKit());
        if (oldLegacyKit != null) removeLegacyKitForPlayer(playerContext, oldLegacyKit);
        playerContext.setCurrentLegacyKit(chosenKitString);
        playerContext.getPlayer().sendMessage(ChatColor.AQUA + "Switched to kit " + LegacyKitModule.LegacyKitStore.valueOf(chosenKitString).getDisplayName() + "!");
        if(!teamManagerModule.getTeam(playerContext.getPlayer()).isSpectator()) setupLegacyKitForPlayer(playerContext);
    }

    public void performSwitch(PlayerContext playerContext) {
        if (!kitSwitches.containsKey(playerContext.getPlayer().getUniqueId())) return;
        LegacyKit legacyKit = getLegacyKit(playerContext.getCurrentLegacyKit());
        legacyKit.removeFromAbilityCaches(playerContext.getPlayer());
        playerContext.setCurrentLegacyKit(kitSwitches.get(playerContext.getPlayer().getUniqueId()));
        setupLegacyKitForPlayer(playerContext);
    }



    private void removeLegacyKitForPlayer(PlayerContext playerContext) {
        LegacyKit legacyKit = getLegacyKit(playerContext.getCurrentLegacyKit());
        removeLegacyKitForPlayer(playerContext, legacyKit);
    }

    private void removeLegacyKitForPlayer(PlayerContext playerContext, LegacyKit legacyKit) {
        if (legacyKit != null) legacyKit.removeFromAbilityCaches(playerContext.getPlayer());
    }

    @SuppressWarnings("unchecked")
    public <T extends LegacyKit> T getLegacyKit(String desiredKit) {
        if (desiredKit == null) return null;
        return (T) getLegacyKit(LegacyKitStore.valueOf(Strings.getTechnicalName(desiredKit)).getHostKit());
    }

    @SuppressWarnings("unchecked")
    public <T extends LegacyKit> T getLegacyKit(Class<T> clazz) {
        for(LegacyKit legacyKit : legacyKitSet) {
            if (clazz.isInstance(legacyKit)) return ((T) legacyKit);
        }
        return null;
    }
}
