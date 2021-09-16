package network.warzone.tgm.modules;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.gametype.GameType;
import network.warzone.tgm.join.MatchJoinEvent;
import network.warzone.tgm.map.MapInfo;
import network.warzone.tgm.match.*;
import network.warzone.tgm.modules.kit.KitEditorModule;
import network.warzone.tgm.modules.respawn.RespawnModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.event.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.team.event.TeamUpdateEvent;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.ColorConverter;
import network.warzone.tgm.util.InventoryUtil;
import network.warzone.tgm.util.Players;
import network.warzone.tgm.util.itemstack.ItemFactory;
import network.warzone.tgm.util.menu.KitEditorMenu;
import network.warzone.tgm.util.menu.Menu;
import network.warzone.tgm.util.menu.PlayerMenu;
import network.warzone.tgm.util.menu.PublicMenu;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.ref.WeakReference;
import java.util.*;

@ModuleData(load = ModuleLoadTime.EARLIER) @Getter
public class SpectatorModule extends MatchModule implements Listener {

    private WeakReference<Match> match;
    private TeamManagerModule teamManagerModule;

    private MatchTeam spectators;
    private PublicMenu teamSelectionMenu;

    private final ItemStack compassItem;
    private final ItemStack teamSelectionItem;
    private final ItemStack teleportMenuItem;

    private final ItemStack leatherHelmet;

    private int afkTimerRunnable;

    private final Map<UUID, Long> lastMovement = new HashMap<>();

    private RespawnModule respawnModule;
    private KitEditorModule kitEditorModule;

    public SpectatorModule() {

        compassItem = ItemFactory.createItem(Material.COMPASS, ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "Teleport Tool");
        teamSelectionItem = ItemFactory.createItem(Material.LEATHER_HELMET, ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Team Selection");
        teleportMenuItem = ItemFactory.createItem(Material.CLOCK, ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + "Player Teleport");

        leatherHelmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta leatherHelmetMeta = (LeatherArmorMeta) leatherHelmet.getItemMeta();
        leatherHelmetMeta.setColor(Color.fromRGB(85, 255, 255));
        leatherHelmet.setItemMeta(leatherHelmetMeta);
    }

    @Override
    public void load(Match match) {
        this.match = new WeakReference<Match>(match);
        this.teamManagerModule = match.getModule(TeamManagerModule.class);
        this.respawnModule = match.getModule(RespawnModule.class);
        this.spectators = teamManagerModule.getSpectators();
        this.kitEditorModule = match.getModule(KitEditorModule.class);

        this.teamSelectionMenu = new PublicMenu(
                ChatColor.UNDERLINE + "Team Selection",
                InventoryUtil.calculateSize(
                        5,
                        this.teamManagerModule.getTeamsParticipating().size()
                )
        );

        /**
         * Only assign the menu actions once. No need to update these every second.
         */
        teamSelectionMenu.setItem(0, null, (player, event) -> performTeamSelectionMenuCommand(player, "join"));

        int count = 0;
        for (MatchTeam matchTeam : teamManagerModule.getTeams()) {
            if (matchTeam.isSpectator()) {
                teamSelectionMenu.setItem(8, null, (player, event) -> performTeamSelectionMenuCommand(player, "join spectators"));
            } else {
                teamSelectionMenu.setItem(InventoryUtil.calculateSlot(5, count++), null, (player, event) -> performTeamSelectionMenuCommand(player, "join " + matchTeam.getId()));
            }
        }

        teamSelectionMenu.setItem(0, null, (player, event) -> performTeamSelectionMenuCommand(player, "join"));

        /**
         * Update the item values every second to keep player counts accurate.
         */
        updateMenu();

        if (this.match.get().getMapContainer().getMapInfo().getGametype() == GameType.Infected) return;
        afkTimerRunnable = Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isSpectating(player) || !lastMovement.containsKey(player.getUniqueId())) continue;
                long moved = lastMovement.get(player.getUniqueId());
                if (moved == 0) continue;
                if (System.currentTimeMillis() > moved + (5 * 60 * 1000)) {
                    teamManagerModule.joinTeam(TGM.get().getPlayerManager().getPlayerContext(player), this.spectators, true, false);
                    lastMovement.remove(player.getUniqueId());
                }
            }
        }, 10 * 20, 10 * 20).getTaskId();
    }

    private static void performTeamSelectionMenuCommand(Player player, String command) {
        TGM.get().getLogger().info(player.getName() + " issued command through team selection menu: /" + command);
        player.performCommand(command);
    }

    public void applySpectatorKit(PlayerContext playerContext) {
        playerContext.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100000, 100, false, false));
        playerContext.getPlayer().setGameMode(GameMode.ADVENTURE);
        playerContext.getPlayer().setAllowFlight(true);
        playerContext.getPlayer().setFlying(true);

        playerContext.getPlayer().setInvulnerable(true);
        playerContext.getPlayer().setCanPickupItems(false);
        playerContext.getPlayer().setCollidable(false);

        playerContext.getPlayer().getInventory().setHelmet(leatherHelmet);
        playerContext.getPlayer().getInventory().setItem(0, compassItem);
        playerContext.getPlayer().getInventory().setItem(2, teamSelectionItem);
        playerContext.getPlayer().getInventory().setItem(8, teleportMenuItem);
        // Inventory slot 6 is reserved for the kitEditorItem in KitLoaderModule

        playerContext.getPlayer().getInventory().setHeldItemSlot(2);
    }

    private void updateTeamMenuItem(MatchTeam matchTeam, int i) {
        ItemStack itemStack = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
        leatherArmorMeta.setDisplayName(matchTeam.getColor() + ChatColor.BOLD.toString() + matchTeam.getAlias());
        leatherArmorMeta.setColor(ColorConverter.getColor(matchTeam.getColor()));
        leatherArmorMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        leatherArmorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        leatherArmorMeta.setLore(Collections.singletonList(ChatColor.WHITE.toString() + matchTeam.getMembers().size() + ChatColor.GRAY.toString()
                + "/" + matchTeam.getMax() + " playing."));
        itemStack.setItemMeta(leatherArmorMeta);
        teamSelectionMenu.setItem(i, itemStack);
    }

    private void updateSpectatorMenuItem(MatchTeam matchTeam) {
        ItemStack itemStack = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
        leatherArmorMeta.setDisplayName(matchTeam.getColor() + ChatColor.BOLD.toString() + matchTeam.getAlias());
//                        leatherArmorMeta.setColor(ColorConverter.getColor(matchTeam.getColor()));
        leatherArmorMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        leatherArmorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        leatherArmorMeta.setLore(Arrays.asList(ChatColor.WHITE + "Spectate the match.", "", ChatColor.WHITE.toString() + matchTeam.getMembers().size() + ChatColor.GRAY.toString() + " spectating."));
        itemStack.setItemMeta(leatherArmorMeta);
        teamSelectionMenu.setItem(8, itemStack);
    }

    private void updateMenu() {
        int totalMatchSize = 0;
        int totalMatchMaxSize = 0;
        int i = 0;
        for (MatchTeam matchTeam : teamManagerModule.getTeams()) {
            if (!matchTeam.isSpectator()) {
                totalMatchSize += matchTeam.getMembers().size();
                totalMatchMaxSize += matchTeam.getMax();
                updateTeamMenuItem(matchTeam, InventoryUtil.calculateSlot(5, i++));
            } else {
                updateSpectatorMenuItem(matchTeam);
            }
        }
        ItemStack autoJoinHelmet = ItemFactory.createItem(
                Material.CHAINMAIL_HELMET,
                ChatColor.WHITE + ChatColor.BOLD.toString() + "Auto Join",
                Arrays.asList(
                        "",
                        ChatColor.WHITE.toString() + totalMatchSize + ChatColor.GRAY.toString() + "/" + totalMatchMaxSize + " playing."
                )
        );
        ItemMeta autoJoinHelmetMeta = autoJoinHelmet.getItemMeta();
        autoJoinHelmetMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        autoJoinHelmetMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        autoJoinHelmet.setItemMeta(autoJoinHelmetMeta);
        teamSelectionMenu.setItem(0, autoJoinHelmet);
    }

    /**
     * Players who are on an actual team during pre/post
     * should still be counted as spectating.
     *
     * We also may need to count players as spectators
     * when we implement a death timer system.
     *
     */
    public boolean isSpectating(Player player) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        return matchStatus != MatchStatus.MID || spectators.containsPlayer(player) || respawnModule != null && respawnModule.isDead(player);
    }

    @EventHandler
    public void onTeamJoin(TeamChangeEvent event) {
        if (event.isCancelled()) return;
        updateMenu();
    }

    @EventHandler
    public void onTeamUpdate(TeamUpdateEvent event) {
        updateMenu();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (isSpectating(player)) {
            if (event.getTo().getY() <= -5.0) {
                player.setAllowFlight(true);
                player.setVelocity(player.getVelocity().setY(4.0)); // Get out of that void!
                player.setFlying(true);
            }
        } else if (this.match.get().getMapContainer().getMapInfo().getGametype() != GameType.Infected &&
                event.getFrom().distanceSquared(event.getTo()) > 0) {
            lastMovement.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (isSpectating(player)) {
                event.setCancelled(true);

                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    player.setAllowFlight(true);
                    player.setVelocity(player.getVelocity().setY(4.0)); // Get out of that void!
                    player.setFlying(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onManipulateArmorStand(PlayerArmorStandManipulateEvent event) {
        if (isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (teamManagerModule.getTeam(event.getPlayer()).isSpectator()) {
            event.setCancelled(true);
        }
        if (TGM.get().getMatchManager().getMatch().getMatchStatus() == MatchStatus.POST && event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (teamManagerModule.getTeam(event.getPlayer()).isSpectator()) {
            event.setCancelled(true);
        }
        if (TGM.get().getMatchManager().getMatch().getMatchStatus() == MatchStatus.POST && event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            event.setInstaBreak(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event instanceof Player && isSpectating((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (event.getInitiator().getHolder() instanceof Player && isSpectating((Player) event.getInitiator().getHolder())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player && isSpectating((Player) event.getWhoClicked())) {
            Player player = (Player) event.getWhoClicked();

            if (!kitEditorModule.getEditorMenus().containsKey(player.getUniqueId()) || !kitEditorModule.getEditorMenus().get(player.getUniqueId()).getInventory().equals(event.getInventory())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickupArrow(PlayerPickupArrowEvent event) {
        if (isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (match.get().getMatchStatus() == MatchStatus.PRE || teamManagerModule.getTeam(event.getPlayer()).isSpectator()) {
            event.setCancelled(true);
            if (event.getItem() == null) return;
            if (event.getItem().getType() == Material.COMPASS) {
                if (event.getAction().name().contains("LEFT")) {
                    Players.findFreePosition(event.getPlayer());
                    return;
                } else if (event.getAction().name().contains("RIGHT")) {
                    Players.passThroughForwardWall(event.getPlayer());
                    return;
                }
            }
            if (event.getItem().isSimilar(teamSelectionItem)) {
                teamSelectionMenu.open(event.getPlayer());
            } else if (event.getItem().isSimilar(teleportMenuItem)) {
                MatchTeam spectators = teamManagerModule.getSpectators();
                Map<Player, ChatColor> players = new LinkedHashMap<>();
                for (MatchTeam team : teamManagerModule.getTeams()) {
                    if (team.equals(spectators)) continue;
                    for (PlayerContext context : team.getMembers()) players.put(context.getPlayer(), team.getColor());
                }
                if (players.size() <= 0) {
                    event.getPlayer().sendMessage(ChatColor.RED + "There are no players to teleport to!");
                    return;
                }
                int size = players.size();
                if (size % 9 != 0) {
                    size -= size % 9;
                    size += 9;
                }
                Menu teleportMenu = new PlayerMenu(ChatColor.UNDERLINE + "Teleport", size, event.getPlayer());
                int i = 0;
                for (Map.Entry<Player, ChatColor> entry : players.entrySet()) {
                    Player player = entry.getKey();
                    ChatColor teamColor = entry.getValue();
                    teleportMenu.setItem(i, ItemFactory.getPlayerSkull(player.getName(), teamColor + player.getName(), " ", "&fClick to teleport to " + player.getName()),
                            (clicker, clickEvent) -> {
                                if (player.isOnline()) clicker.teleport(player.getLocation());
                            });
                    i++;
                    if (i >= size) break;
                }
                teleportMenu.open(event.getPlayer());
                players.clear();
            } else if (event.getItem().isSimilar(kitEditorModule.getKitEditorItem())) {
                if (KitEditorModule.isEnabled() && KitEditorModule.isKitEditable()) {
                    KitEditorMenu kitEditor;
                    if (kitEditorModule.getEditorMenus().containsKey(event.getPlayer().getUniqueId())) {
                        kitEditor = kitEditorModule.getEditorMenus().get(event.getPlayer().getUniqueId());
                    } else {
                        List<MatchTeam> matchTeams = teamManagerModule.getTeams(); // Team doesn't matter because this code only runs if there are no team specific kits.
                        kitEditor = new KitEditorMenu(matchTeams.get(1).getKits(), match.get().getMapContainer().getMapInfo().getName());
                        kitEditorModule.getEditorMenus().put(event.getPlayer().getUniqueId(), kitEditor);
                    }
                    kitEditor.open(event.getPlayer());
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "Kit editing has been disabled.");
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && isSpectating((Player) event.getDamager()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingDestroy(HangingBreakByEntityEvent event) { // Item Frames and Paintings
        if (event.getRemover() != null && event.getRemover() instanceof Player
                && isSpectating((Player) event.getRemover())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getAttacker() != null && event.getAttacker() instanceof Player
                && isSpectating((Player) event.getAttacker())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getAttacker() != null && event.getAttacker() instanceof Player
                && isSpectating((Player) event.getAttacker())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (isSpectating((Player) event.getEntity())) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if  (event.getTarget() instanceof Player && isSpectating((Player) event.getTarget())) {
            event.setTarget(null);
        }
    }

    @EventHandler
    public void onMatchJoin(MatchJoinEvent event) {
        Player player = event.getPlayerContext().getPlayer();
        lastMovement.put(player.getUniqueId(), System.currentTimeMillis());
        Bukkit.getScheduler().runTaskLater(TGM.get(), () -> this.printObjective(player), 10L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastMovement.remove(event.getPlayer().getUniqueId());
        updateMenu();
    }

    @EventHandler
    public void onAEC(AreaEffectCloudApplyEvent event) {
        for (Entity entity : event.getAffectedEntities()) {
            if (entity instanceof Player && isSpectating((Player) entity)) {
                event.getAffectedEntities().remove(entity);
            }
        }
    }

    @EventHandler
    public void onPotion(PotionSplashEvent event) {
        for (Entity entity : event.getAffectedEntities()) {
            if (entity instanceof Player && isSpectating((Player) entity)) {
                event.getAffectedEntities().remove(entity);
            }
        }
    }

    @Override
    public void unload() {
        if (this.match.get().getMapContainer().getMapInfo().getGametype() != GameType.Infected)
            Bukkit.getScheduler().cancelTask(afkTimerRunnable);
        lastMovement.clear();
        teamSelectionMenu.disable();
    }

    public void printObjective(Player player) {
        MapInfo mapInfo = this.match.get().getMapContainer().getMapInfo();
        String objective;
        if (mapInfo.getObjective() != null) {
            objective = mapInfo.getObjective();
        } else {
            objective = mapInfo.getGametype().getObjective();
        }
        if (objective != null && !objective.isEmpty()) {
            String[] lines = objective.split("\n");
            String gamemode = mapInfo.getGametype().getName();
            player.sendMessage(
                    ChatColor.BLUE.toString() +
                            ChatColor.BOLD +
                            "---------- " +
                            ChatColor.WHITE +
                            ChatColor.BOLD +
                            gamemode +
                            ChatColor.BLUE.toString() +
                            ChatColor.BOLD +
                            " ----------"
            );
            for (String line : lines) {
                player.sendMessage(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', line));
            }
            StringBuilder footer = new StringBuilder(ChatColor.BLUE.toString() + ChatColor.BOLD);
            for (int i = 0; i < gamemode.length() + 20; i++) {
                footer.append("-");
            }
            player.sendMessage(footer.toString());
        }
    }
}
