package network.warzone.tgm.modules;

import com.sk89q.minecraft.util.commands.ChatColor;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.damage.tracker.event.PlayerDamageEvent;
import network.warzone.tgm.match.*;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.ColorConverter;
import network.warzone.tgm.util.itemstack.ItemFactory;
import network.warzone.tgm.util.menu.PublicMenu;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
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

import java.util.Arrays;
import java.util.Collections;

@ModuleData(load = ModuleLoadTime.EARLIER) @Getter
public class SpectatorModule extends MatchModule implements Listener {

    private MatchTeam spectators;
    private PublicMenu teamSelectionMenu;

    private final ItemStack compassItem;
    private final ItemStack teamSelectionItem;

    private int teamSelectionRunnable;

    public SpectatorModule() {
        this.teamSelectionMenu = new PublicMenu(TGM.get(), ChatColor.UNDERLINE + "Team Selection", 9);

        compassItem = ItemFactory.createItem(Material.COMPASS, ChatColor.YELLOW + "Teleport Tool");
        teamSelectionItem = ItemFactory.createItem(Material.LEATHER_HELMET, ChatColor.YELLOW + "Team Selection");
    }

    @Override
    public void load(Match match) {
        this.spectators = match.getModule(TeamManagerModule.class).getSpectators();

        /**
         * Only assign the menu actions once. No need to update these every second.
         */
        teamSelectionMenu.setItem(0, null, player -> player.performCommand("join"));

        int slot = 2;
        for (MatchTeam matchTeam : match.getModule(TeamManagerModule.class).getTeams()) {
            if (matchTeam.isSpectator()) {
                teamSelectionMenu.setItem(8, null, player -> player.performCommand("join spectators"));
            } else {
                teamSelectionMenu.setItem(slot++, null, player -> player.performCommand("join " + matchTeam.getId()));
            }
        }

        teamSelectionMenu.setItem(0, null, player -> player.performCommand("join"));

        /**
         * Update the item values every second to keep player counts accurate.
         */
        teamSelectionRunnable = Bukkit.getScheduler().scheduleSyncRepeatingTask(TGM.get(), () -> {
            int totalMatchSize = 0;
            int totalMatchMaxSize = 0;

            int i = 2;
            for (MatchTeam matchTeam : match.getModule(TeamManagerModule.class).getTeams()) {
                if (matchTeam.isSpectator()) {
                    ItemStack itemStack = new ItemStack(Material.LEATHER_BOOTS);
                    LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
                    leatherArmorMeta.setDisplayName(matchTeam.getColor() + ChatColor.BOLD.toString() + matchTeam.getAlias());
//                        leatherArmorMeta.setColor(ColorConverter.getColor(matchTeam.getColor()));
                    leatherArmorMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    leatherArmorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    leatherArmorMeta.setLore(Arrays.asList(ChatColor.WHITE + "Spectate the match.", "", ChatColor.WHITE.toString() + matchTeam.getMembers().size() + ChatColor.GRAY.toString() + " spectating."));
                    itemStack.setItemMeta(leatherArmorMeta);
                    teamSelectionMenu.setItem(8, itemStack);
                } else {
                    totalMatchSize += matchTeam.getMembers().size();
                    totalMatchMaxSize += matchTeam.getMax();

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

                    i++;
                }
            }

            ItemStack autoJoinHelmet = ItemFactory.createItem(Material.CHAINMAIL_HELMET, ChatColor.WHITE + ChatColor.BOLD.toString() + "Auto Join",
                    Arrays.asList("", ChatColor.WHITE.toString() + totalMatchSize + ChatColor.GRAY.toString() + "/" + totalMatchMaxSize + " playing."));
            ItemMeta autoJoinHelmetMeta = autoJoinHelmet.getItemMeta();
            autoJoinHelmetMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            autoJoinHelmetMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            autoJoinHelmet.setItemMeta(autoJoinHelmetMeta);
            teamSelectionMenu.setItem(0, autoJoinHelmet);
        }, 0L, 20L);
    }

    public void applySpectatorKit(PlayerContext playerContext) {

        playerContext.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100000, 255, false, false));
        playerContext.getPlayer().setGameMode(GameMode.ADVENTURE);
        playerContext.getPlayer().setAllowFlight(true);
        playerContext.getPlayer().setFlying(true);

        playerContext.getPlayer().setInvulnerable(true);
        playerContext.getPlayer().setCanPickupItems(false);
        playerContext.getPlayer().setCollidable(false);

        playerContext.getPlayer().getInventory().setItem(0, compassItem);
        playerContext.getPlayer().getInventory().setItem(2, teamSelectionItem);
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
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (isSpectating(player) && event.getTo().getY() <= -5.0) {
            player.setAllowFlight(true);
            player.setVelocity(player.getVelocity().setY(4.0)); // Get out of that void!
            player.setFlying(true);
        }
    }

    @EventHandler
    public void onDamage(PlayerDamageEvent event) {
        if (isSpectating(event.getEntity())) {
            event.setCancelled(true);
        } else if (event.getInfo().getResolvedDamager() instanceof Player) {
            if (isSpectating((Player) event.getInfo().getResolvedDamager())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVoidDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (isSpectating(player) && event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                event.setCancelled(true);

                player.setAllowFlight(true);
                player.setVelocity(player.getVelocity().setY(4.0)); // Get out of that void!
                player.setFlying(true);
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

            if (event.getItem() != null && event.getItem().isSimilar(teamSelectionItem)) {
                teamSelectionMenu.open(event.getPlayer());
                Bukkit.getScheduler().runTaskLater(TGM.get(), () -> event.getPlayer().updateInventory(), 1L); //client side glitch shows hat on head until this is called.
            }
        }
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

    @Override
    public void unload() {
        Bukkit.getScheduler().cancelTask(teamSelectionRunnable);
        teamSelectionMenu.disable();
    }
}
