package network.warzone.tgm.modules.kit.classes;

import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;
import network.warzone.tgm.modules.kit.classes.abilities.*;
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
public class GameClassModule extends MatchModule implements Listener {
    public static String DEFAULT_CLASS = "PHOENIX";

    public enum GameClassStore {
        PHOENIX(PhoenixClass.class,
                ItemFactory.createItem(Material.FIRE_CHARGE,
                ChatColor.GOLD + "Phoenix",
                    Arrays.asList(ChatColor.YELLOW + "Fight with the power of the sun!")), 900),

        NINJA(NinjaClass.class,
                ItemFactory.createItem(Material.FLINT,
                ChatColor.WHITE + "Ninja",
                    Arrays.asList(ChatColor.YELLOW + "Don't need armor when you can't get hit!")), 800),

        BUILDER(BuilderClass.class,
                ItemFactory.createItem(Material.OAK_STAIRS,
                ChatColor.YELLOW + "Builder",
                    Arrays.asList(ChatColor.YELLOW + "Extra blocks to help build fortifications.")), 300);

        @Getter private Class hostKit;
        @Getter private ItemStack menuItem;
        @Getter @Setter private int cost;

        GameClassStore(Class hostKit, ItemStack menuItem, int cost) {
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
                for(GameClassStore gameClassStore : GameClassStore.values()) gameClassStore.setCost(0);
        }
    }


    @Getter private HashMap<UUID, String> kitSwitches = new HashMap<>();;
    private AbilityManager abilityManager;
    private TeamManagerModule teamManagerModule;
    private Set<GameClass> gameClassSet = new HashSet<>();

    @Override
    public void load(Match match) {
        abilityManager = new AbilityManager();
        teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        gameClassSet.add(new PhoenixClass(abilityManager.getAbility(PhoenixAbility.class)));
        gameClassSet.add(new NinjaClass(abilityManager.getAbility(NinjaAbility.class)));
        gameClassSet.add(new BuilderClass(abilityManager.getAbility(BuilderAbility.class)));
    }

    @Override
    public void disable() {
        abilityManager.destroyAbilities();
        gameClassSet = null;
        kitSwitches = null;
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (event.getTeam().isSpectator()) {
            removeClassForPlayer(event.getPlayerContext());
        } else if (event.getOldTeam().isSpectator()) {
            setupClassForPlayer(event.getPlayerContext());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeClassForPlayer(TGM.get().getPlayerManager().getPlayerContext(event.getPlayer()));
    }


    public void addSwitchClassRequest(PlayerContext p, String desiredKit) {
        addSwitchClassRequest(p, desiredKit, true);
    }

    public void addSwitchClassRequest(PlayerContext p, String desiredKit, boolean showMsg) {
        MatchTeam team = teamManagerModule.getTeam(p.getPlayer());
        if(team == null || team.isSpectator()) {
            setClassForPlayer(p, desiredKit);
        } else {
            if (showMsg) p.getPlayer().sendMessage(ChatColor.GREEN + "Kit will be applied when you respawn!");
            kitSwitches.put(p.getPlayer().getUniqueId(), desiredKit);
        }
    }

    private void setupClassForPlayer(PlayerContext playerContext) {
        if (playerContext.getCurrentClass() == null) playerContext.setCurrentClass(DEFAULT_CLASS);
        GameClass newGameClass = getGameClass(playerContext.getCurrentClass());
        newGameClass.addToAbilityCaches(playerContext.getPlayer());

        // forget about respawn switcher kit if it exists
        kitSwitches.remove(playerContext.getPlayer().getUniqueId());
    }

    public void setClassForPlayer(PlayerContext playerContext, String chosenKitString) {
        GameClass oldGameClass = getGameClass(playerContext.getCurrentClass());
        if (oldGameClass != null) removeClassForPlayer(playerContext, oldGameClass);
        playerContext.setCurrentClass(chosenKitString);
        playerContext.getPlayer().sendMessage(ChatColor.AQUA + "Switched to class " + GameClassStore.valueOf(chosenKitString).getDisplayName() + "!");
        if(!teamManagerModule.getTeam(playerContext.getPlayer()).isSpectator()) setupClassForPlayer(playerContext);
    }

    public void performSwitch(PlayerContext playerContext) {
        if (!kitSwitches.containsKey(playerContext.getPlayer().getUniqueId())) return;
        GameClass gameClass = getGameClass(playerContext.getCurrentClass());
        gameClass.removeFromAbilityCaches(playerContext.getPlayer());
        playerContext.setCurrentClass(kitSwitches.get(playerContext.getPlayer().getUniqueId()));
        setupClassForPlayer(playerContext);
    }



    private void removeClassForPlayer(PlayerContext playerContext) {
        GameClass gameClass = getGameClass(playerContext.getCurrentClass());
        removeClassForPlayer(playerContext, gameClass);
    }

    private void removeClassForPlayer(PlayerContext playerContext, GameClass gameClass) {
        if (gameClass != null) gameClass.removeFromAbilityCaches(playerContext.getPlayer());
    }

    @SuppressWarnings("unchecked")
    public <T extends GameClass> T getGameClass(String desiredKit) {
        if (desiredKit == null) return null;
        return (T) getGameClass(GameClassStore.valueOf(Strings.getTechnicalName(desiredKit)).getHostKit());
    }

    @SuppressWarnings("unchecked")
    public <T extends GameClass> T getGameClass(Class<T> clazz) {
        for(GameClass gameClass : gameClassSet) {
            if (clazz.isInstance(gameClass)) return ((T) gameClass);
        }
        return null;
    }
}
