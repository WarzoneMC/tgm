package network.warzone.tgm.modules;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.join.MatchJoinEvent;
import network.warzone.tgm.match.*;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.ColorConverter;
import network.warzone.tgm.util.itemstack.ItemFactory;
import network.warzone.tgm.util.menu.Menu;
import network.warzone.tgm.util.menu.PlayerMenu;
import network.warzone.tgm.util.menu.PublicMenu;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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

import java.util.*;

@ModuleData(load = ModuleLoadTime.EARLIER) @Getter
public class SpectatorModule extends MatchModule implements Listener {

    private TeamManagerModule teamManagerModule;

    private MatchTeam spectators;
    private PublicMenu teamSelectionMenu;

    private final ItemStack compassItem;
    private final ItemStack teamSelectionItem;
    private final ItemStack teleportMenuItem;

    private final ItemStack leatherHelmet;

    private int afkTimerRunnable;

    private final Map<UUID, Long> lastMovement = new HashMap<>();

    public SpectatorModule() {
        this.teamSelectionMenu = new PublicMenu(ChatColor.UNDERLINE + "Team Selection", 9);

        compassItem = ItemFactory.createItem(Material.COMPASS, ChatColor.YELLOW + "Teleport Tool");
        teamSelectionItem = ItemFactory.createItem(Material.NETHER_STAR, ChatColor.YELLOW + "Team Selection");
        teleportMenuItem = ItemFactory.createItem(Material.CLOCK, ChatColor.YELLOW + "Player Teleport");

        leatherHelmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta leatherHelmetMeta = (LeatherArmorMeta) leatherHelmet.getItemMeta();
        leatherHelmetMeta.setColor(Color.fromRGB(85, 255, 255));
        leatherHelmet.setItemMeta(leatherHelmetMeta);
    }

    @Override
    public void load(Match match) {
        teamManagerModule = match.getModule(TeamManagerModule.class);

        this.spectators = teamManagerModule.getSpectators();

        /**
         * Only assign the menu actions once. No need to update these every second.
         */
        teamSelectionMenu.setItem(0, null, (player, event) -> player.performCommand("join"));

        int slot = 2;
        for (MatchTeam matchTeam : teamManagerModule.getTeams()) {
            if (matchTeam.isSpectator()) {
                teamSelectionMenu.setItem(8, null, (player, event) -> player.performCommand("join spectators"));
            } else {
                teamSelectionMenu.setItem(slot++, null, (player, event) -> player.performCommand("join " + matchTeam.getId()));
            }
        }

        teamSelectionMenu.setItem(0, null, (player, event) -> player.performCommand("join"));

        /**
         * Update the item values every second to keep player counts accurate.
         */
        updateMenu();

        afkTimerRunnable = Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isSpectating(player) || !lastMovement.containsKey(player.getUniqueId())) continue;
                long moved = lastMovement.get(player.getUniqueId());
                if (moved == 0) continue;
                if (System.currentTimeMillis() > moved + (5 * 60 * 1000)) {
                    teamManagerModule.joinTeam(TGM.get().getPlayerManager().getPlayerContext(player), this.spectators, true);
                    lastMovement.remove(player.getUniqueId());
                }
            }
        }, 10 * 20, 10 * 20).getTaskId();
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
        playerContext.getPlayer().getInventory().setItem(2, compassItem);
        playerContext.getPlayer().getInventory().setItem(4, teamSelectionItem);
        playerContext.getPlayer().getInventory().setItem(6, teleportMenuItem);
    }

    void updateTeamMenuItem(MatchTeam matchTeam, int i) {
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

    void updateSpectatorMenuItem(MatchTeam matchTeam) {
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

    void updateMenu() {
        int totalMatchSize = 0;
        int totalMatchMaxSize = 0;
        int i = 2;
        for (MatchTeam matchTeam : teamManagerModule.getTeams()) {
            if (!matchTeam.isSpectator()) {
                totalMatchSize += matchTeam.getMembers().size();
                totalMatchMaxSize += matchTeam.getMax();
                updateTeamMenuItem(matchTeam, i++);
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
        return matchStatus != MatchStatus.MID || spectators.containsPlayer(player);
    }

    @EventHandler
    public void onTeamJoin(TeamChangeEvent event) {
        if (event.isCancelled()) return;
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
        } else if (event.getFrom().distance(event.getTo()) > 0) {
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
    public void onBreak(BlockBreakEvent event) {
        if (isSpectating(event.getPlayer())) {
            event.setCancelled(true);
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
            event.setCancelled(true);
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
        if (isSpectating(event.getPlayer())) {
            event.setCancelled(true);
            if (event.getItem() == null) return;
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
                        if (player.isOnline()) clicker.teleport(player.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    });
                    i++;
                    if (i >= size) break;
                }
                teleportMenu.open(event.getPlayer());
                players.clear();
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
        if (event.getRemover() != null && event.getRemover() instanceof Player) {
            if (isSpectating((Player) event.getRemover())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getAttacker() != null && event.getAttacker() instanceof Player) {
            if (isSpectating((Player) event.getAttacker())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getAttacker() != null && event.getAttacker() instanceof Player) {
            if (isSpectating((Player) event.getAttacker())) {
                event.setCancelled(true);
            }
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
        lastMovement.put(event.getPlayerContext().getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastMovement.remove(event.getPlayer().getUniqueId());
        updateMenu();
    }

    @Override
    public void unload() {
        Bukkit.getScheduler().cancelTask(afkTimerRunnable);
        lastMovement.clear();
        teamSelectionMenu.disable();
    }
}
