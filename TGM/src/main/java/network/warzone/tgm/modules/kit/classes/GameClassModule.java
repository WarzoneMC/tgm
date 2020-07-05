package network.warzone.tgm.modules.kit.classes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;
import network.warzone.tgm.modules.kit.classes.abilities.*;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.util.Strings;
import network.warzone.tgm.util.itemstack.ItemFactory;
import network.warzone.tgm.util.menu.ClassMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yikes on 09/27/19
 */
@ModuleData(load = ModuleLoadTime.EARLIER)
public class GameClassModule extends MatchModule implements Listener {
    @Getter private String defaultClass;

    private HashMap<UUID, String> playerClasses = new HashMap<>();

    @Getter private HashMap<UUID, String> classSwitches = new HashMap<>();
    private AbilityManager abilityManager;
    private TeamManagerModule teamManagerModule;
    @Getter private Set<GameClass> gameClassSet = new HashSet<>();

    // cache this for checking class violators
    private List<String> rawUsedClasses;

    public String getCurrentClass(Player p) {
        return playerClasses.get(p.getUniqueId());
    }

    public void setCurrentClass(Player p, String className) {
        playerClasses.put(p.getUniqueId(), className);
    }

    public static boolean isUsingClasses(JsonObject mapJson) {
        return (mapJson.has("classes") && ((mapJson.get("classes").isJsonPrimitive() && mapJson.get("classes").getAsJsonPrimitive().isBoolean() && mapJson.get("classes").getAsBoolean()) || mapJson.get("classes").isJsonArray()));
    }

    @Override
    public void load(Match match) {
        teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        boolean usingAllClasses = match.getMapContainer().getMapInfo().getJsonObject().get("classes").isJsonPrimitive() && match.getMapContainer().getMapInfo().getJsonObject().get("classes").getAsJsonPrimitive().isBoolean();
        List<String> usingClasses = new ArrayList<>();
        if (usingAllClasses) usingClasses = Arrays.stream(GameClassStore.values()).map(Enum::name).collect(Collectors.toList());
        else for (JsonElement elem : match.getMapContainer().getMapInfo().getJsonObject().get("classes").getAsJsonArray()) usingClasses.add(Strings.getTechnicalName(elem.getAsString()));
        this.rawUsedClasses = usingClasses;

        defaultClass = usingClasses.get(0);

        abilityManager = new AbilityManager(GameClassStore.abilityClassUsages(usingClasses));

        for (GameClassStore gameClassStore : GameClassStore.values()) {
            if (!usingClasses.contains(gameClassStore.name())) continue;
            Ability[] abilitySet = new Ability[gameClassStore.getHostAbilities().size()];
            for(int x = 0; x < gameClassStore.getHostAbilities().size(); x++) abilitySet[x] = abilityManager.getAbility(gameClassStore.getHostAbilities().get(x));
            try {
                gameClassSet.add((GameClass) gameClassStore.getHostGameClass().getConstructors()[0].newInstance(new Object[] { abilitySet }));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ClassMenu.destroyInstance();
    }

    public boolean classSetHasInstance(Class<? extends GameClass> candidate) {
        boolean hasInstance = false;
        for (GameClass gameClass : gameClassSet) {
            if (candidate.isInstance(gameClass)) {
                hasInstance = true;
                break;
            }
        }
        return hasInstance;
    }

    @Override
    public void disable() {
        abilityManager.destroyAbilities();
        gameClassSet = null;
        classSwitches = null;
    }

    @Override
    public void unload() {
        gameClassSet = null;
        classSwitches = null;
        playerClasses = null;
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (event.getTeam().isSpectator()) {
            removeClassForPlayer(event.getPlayerContext().getPlayer());
        } else if (event.getOldTeam().isSpectator()) {
            setupClassForPlayer(event.getPlayerContext().getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeClassForPlayer(event.getPlayer());
    }

    public void addSwitchClassRequest(Player p, String desiredClass) {
        addSwitchClassRequest(p, desiredClass, true);
    }

    public void addSwitchClassRequest(Player p, String desiredClass, boolean showMsg) {
        MatchTeam team = teamManagerModule.getTeam(p);
        if(team == null || team.isSpectator()) {
            setClassForPlayer(p, desiredClass);
        } else {
            if (showMsg) p.sendMessage(ChatColor.GREEN + "Class will be applied when you respawn!");
            classSwitches.put(p.getUniqueId(), desiredClass);
        }
    }

    private void setupClassForPlayer(Player player) {
        if (getCurrentClass(player) == null || !classIsAllowed(getCurrentClass(player))) setCurrentClass(player, defaultClass);
        GameClass newGameClass = getGameClass(getCurrentClass(player));
        newGameClass.addToAbilityCaches(player);

        // forget about respawn switcher class if it exists
        classSwitches.remove(player.getUniqueId());
    }

    private boolean classIsAllowed(String className) {
        String normalized = Strings.getTechnicalName(className);
        return rawUsedClasses.contains(normalized);
    }

    public void setClassForPlayer(Player player, String chosenClassString) {
        GameClass oldGameClass = getGameClass(getCurrentClass(player));
        if (oldGameClass != null) removeClassForPlayer(player, oldGameClass);
        setCurrentClass(player, chosenClassString);
        player.sendMessage(ChatColor.AQUA + "Switched to class " + GameClassStore.valueOf(chosenClassString).getDisplayName() + "!");
        if(!teamManagerModule.getTeam(player).isSpectator()) setupClassForPlayer(player);
    }

    public void performSwitch(Player player) {
        if (!classSwitches.containsKey(player.getUniqueId())) return;
        GameClass gameClass = getGameClass(getCurrentClass(player));
        gameClass.removeFromAbilityCaches(player);
        setCurrentClass(player, classSwitches.get(player.getUniqueId()));
        setupClassForPlayer(player);
    }

    private void removeClassForPlayer(Player player) {
        GameClass gameClass = getGameClass(getCurrentClass(player));
        removeClassForPlayer(player, gameClass);
    }

    private void removeClassForPlayer(Player player, GameClass gameClass) {
        if (gameClass != null) gameClass.removeFromAbilityCaches(player);
    }

    @SuppressWarnings("unchecked")
    public <T extends GameClass> T getGameClass(String desiredClass) {
        if (desiredClass == null) return null;
        return (T) getGameClass(GameClassStore.valueOf(Strings.getTechnicalName(desiredClass)).getHostGameClass());
    }

    @SuppressWarnings("unchecked")
    public <T extends GameClass> T getGameClass(Class<T> clazz) {
        if (gameClassSet == null) return null;
        for(GameClass gameClass : gameClassSet) {
            if (clazz.isInstance(gameClass)) return ((T) gameClass);
        }
        return null;
    }

    public enum GameClassStore {
        PHOENIX(PhoenixClass.class,
                Arrays.asList(PhoenixAbility.class),
                ItemFactory.createItem(Material.FIRE_CHARGE,
                        ChatColor.GOLD + "Phoenix",
                        Arrays.asList(ChatColor.YELLOW + "Fight with the power of the sun!"))),

        NINJA(NinjaClass.class,
                Arrays.asList(NinjaAbility.class),
                ItemFactory.createItem(Material.FLINT,
                        ChatColor.WHITE + "Ninja",
                        Arrays.asList(ChatColor.YELLOW + "Don't need armor when you can't get hit!"))),

        BUILDER(BuilderClass.class,
                Arrays.asList(BuilderAbility.class),
                ItemFactory.createItem(Material.OAK_STAIRS,
                        ChatColor.YELLOW + "Builder",
                        Arrays.asList(ChatColor.YELLOW + "Extra blocks to help build fortifications."))),

        SPY(SpyClass.class,
                Arrays.asList(SpyAbility.class),
                ItemFactory.createItem(Material.LEATHER_HELMET,
                        ChatColor.LIGHT_PURPLE + "Spy",
                        Arrays.asList(ChatColor.YELLOW + "Keep your friends close, and your enemies closer!")));

        @Getter private Class hostGameClass;
        @Getter private List<Class<? extends Ability>> hostAbilities;
        @Getter private ItemStack menuItem;

        GameClassStore(Class hostGameClass, List<Class<? extends Ability>> hostAbilities, ItemStack menuItem) {
            this.hostGameClass = hostGameClass;
            this.hostAbilities = hostAbilities;
            this.menuItem = menuItem;
        }

        public String getDisplayName() {
            if (menuItem.getItemMeta() == null) return Strings.capitalizeString(this.name().toLowerCase());
            return menuItem.getItemMeta().getDisplayName();
        }

        private static Set<Class<? extends Ability>> abilityClassUsages(List<String> usingClasses) {
            Set<Class<? extends Ability>> currentAbilityClassSet = new HashSet<>();
            for (GameClassStore gameClassStore : GameClassStore.values()) {
                if (!usingClasses.contains(gameClassStore.name())) continue;
                currentAbilityClassSet.addAll(gameClassStore.getHostAbilities());
            }
            return currentAbilityClassSet;
        }
    }

}
