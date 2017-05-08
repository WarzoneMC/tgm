package com.minehut.tgm.modules;

import com.minehut.tgm.TGM;
import com.minehut.tgm.damage.tracker.event.PlayerDamageEvent;
import com.minehut.tgm.match.*;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamChangeEvent;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import com.minehut.tgm.util.ColorConverter;
import com.minehut.tgm.util.Players;
import com.minehut.tgm.util.itemstack.ItemFactory;
import com.minehut.tgm.util.menu.MenuAction;
import com.minehut.tgm.util.menu.PublicMenu;
import com.sk89q.minecraft.util.commands.ChatColor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;

@ModuleData(load = ModuleLoadTime.EARLIER)
public class SpectatorModule extends MatchModule implements Listener {
    @Getter private MatchTeam spectators;
    @Getter private PublicMenu teamSelectionMenu;

    @Getter private final ItemStack compassItem;
    @Getter private final ItemStack teamSelectionItem;

    @Getter private int teamSelectionRunnable;

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
        teamSelectionMenu.setItem(0, null, new MenuAction() {
            @Override
            public void run(Player player) {
                player.performCommand("join");
            }
        });

        int slot = 2;
        for (MatchTeam matchTeam : match.getModule(TeamManagerModule.class).getTeams()) {
            if (matchTeam.isSpectator()) {
                teamSelectionMenu.setItem(8, null, new MenuAction() {
                    @Override
                    public void run(Player player) {
                        player.performCommand("join spectators");
                    }
                });
            } else {
                teamSelectionMenu.setItem(slot, null, new MenuAction() {
                    @Override
                    public void run(Player player) {
                        player.performCommand("join " + matchTeam.getId());
                    }
                });
                slot++;
            }
        }

        teamSelectionMenu.setItem(0, null, new MenuAction() {
            @Override
            public void run(Player player) {
                player.performCommand("join");
            }
        });

        /**
         * Update the item values every second to keep player counts accurate.
         */
        teamSelectionRunnable = Bukkit.getScheduler().scheduleSyncRepeatingTask(TGM.get(), new Runnable() {
            @Override
            public void run() {
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
                        leatherArmorMeta.setLore(Arrays.asList(ChatColor.WHITE.toString() + matchTeam.getMembers().size() + ChatColor.GRAY.toString()
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
            }
        }, 0L, 20L);
    }

    public void applySpectatorKit(PlayerContext playerContext) {
        Players.reset(playerContext.getPlayer(), false);
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
        if (matchStatus == MatchStatus.MID) {
            return spectators.containsPlayer(player);
        } else {
            return true;
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
    public void onPickup(PlayerPickupItemEvent event) {
        if (isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (isSpectating((Player) event.getInitiator().getHolder())) {
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
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event){
        if (isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHotbarClick(PlayerInteractEvent event) {
        if (event.getItem() != null) {
            if (event.getItem().isSimilar(teamSelectionItem)) {
                teamSelectionMenu.open(event.getPlayer());
                event.setCancelled(true);
                event.getPlayer().updateInventory(); //client side glitch shows hat on head until this is called.
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
