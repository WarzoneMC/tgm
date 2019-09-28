package network.warzone.tgm.modules.kit.legacy_kits;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;
import network.warzone.tgm.modules.kit.legacy_kits.abilities.AbilityManager;
import network.warzone.tgm.modules.kit.legacy_kits.abilities.PhoenixAbility;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.itemstack.ItemFactory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by yikes on 09/27/19
 */
@ModuleData(load = ModuleLoadTime.EARLIER)
public class LegacyKitModule extends MatchModule {
    public static String DEFAULT_KIT = "tank";

    public enum LegacyKitStore {
        PHOENIX(PhoenixKit.class, ItemFactory.createItem(Material.BLAZE_POWDER, ChatColor.GOLD + "Phoenix", Arrays.asList(ChatColor.YELLOW + "it fiery af")), 50);

        @Getter private Class hostKit;
        @Getter private ItemStack menuItem;
        @Getter private int cost;

        LegacyKitStore(Class hostKit, ItemStack menuItem, int cost) {
            this.hostKit = hostKit;
            this.menuItem = menuItem;
            this.cost = cost;
        }
    }

    @Getter private HashMap<UUID, String> kitSwitches = new HashMap<>();
    private AbilityManager abilityManager;
    private TeamManagerModule teamManagerModule;
    private Set<LegacyKit> legacyKitSet = new HashSet<>();

    @Override
    public void load(Match match) {
        abilityManager = new AbilityManager();
        teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        legacyKitSet.add(new PhoenixKit(abilityManager.getAbility(PhoenixAbility.class)));
    }

    @Override
    public void unload() {
        abilityManager.destroyAbilities();
        legacyKitSet = null;
    }

    public void addSwitchKitRequest(Player p, String desiredKit) {
        MatchTeam team = teamManagerModule.getTeam(p);
        if(team == null || team.isSpectator()) {
            TGM.get().getPlayerManager().getPlayerContext(p).setCurrentLegacyKit(desiredKit);
        } else {
            p.sendMessage(ChatColor.GREEN + "Kit will be applied when you respawn!");
            kitSwitches.put(p.getUniqueId(), desiredKit);
        }
    }

    public void performSwitch(PlayerContext playerContext) {
        if (!kitSwitches.containsKey(playerContext.getPlayer().getUniqueId())) return;
        LegacyKit legacyKit = getLegacyKit(playerContext.getCurrentLegacyKit());
        legacyKit.removeFromAbilityCaches(playerContext.getPlayer());
        playerContext.setCurrentLegacyKit(kitSwitches.get(playerContext.getPlayer().getUniqueId()));
        LegacyKit newLegacyKit = getLegacyKit(playerContext.getCurrentLegacyKit());
        if (newLegacyKit != null) newLegacyKit.addToAbilityCaches(playerContext.getPlayer());
    }

    @SuppressWarnings("unchecked")
    public <T extends LegacyKit> T getLegacyKit(String desiredKit) {
        return (T) getLegacyKit(LegacyKitStore.valueOf(enumFriendlyString(desiredKit)).getHostKit());
    }

    @SuppressWarnings("unchecked")
    public <T extends LegacyKit> T getLegacyKit(Class<T> clazz) {
        for(LegacyKit legacyKit : legacyKitSet) {
            if (clazz.isInstance(legacyKit)) return ((T) legacyKit);
        }
        return null;
    }

    private static String enumFriendlyString(String inString) {
        return inString.toUpperCase().replace(" ", "_");
    }
}
