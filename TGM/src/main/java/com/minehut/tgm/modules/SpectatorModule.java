package com.minehut.tgm.modules;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.ModuleData;
import com.minehut.tgm.match.ModuleLoadTime;
import com.minehut.tgm.team.MatchTeam;
import com.minehut.tgm.team.TeamChangeEvent;
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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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

    @Getter private final int teamSelectionRunnable;

    public SpectatorModule() {
        this.teamSelectionMenu = new PublicMenu(TGM.getTgm(), ChatColor.UNDERLINE + "Team Selection", 9);

        compassItem = ItemFactory.createItem(Material.COMPASS, ChatColor.YELLOW + "Teleport Tool");
        teamSelectionItem = ItemFactory.createItem(Material.LEATHER_HELMET, ChatColor.YELLOW + "Team Selection");

        /**
         * Only assign the menu actions once. No need to update these every second.
         */
        teamSelectionMenu.setItem(0, null, new MenuAction() {
            @Override
            public void run(Player player) {
                player.performCommand("join");
            }
        });

        int slot = 1;
        for (MatchTeam matchTeam : TGM.getTgm().getTeamManager().getTeams()) {
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

        /**
         * Update the item values every second to keep player counts accurate.
         */
        teamSelectionRunnable = Bukkit.getScheduler().scheduleSyncRepeatingTask(TGM.getTgm(), new Runnable() {
            @Override
            public void run() {
                int totalMatchSize = 0;
                int totalMatchMaxSize = 0;

                int i = 1;
                for (MatchTeam matchTeam : TGM.getTgm().getTeamManager().getTeams()) {
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
                                + "/" + matchTeam.getMax() + " playing.", "", ChatColor.LIGHT_PURPLE +  "Only premium users can choose their team.", ChatColor.LIGHT_PURPLE + "Everyone can use " + ChatColor.WHITE + "Auto Join " + ChatColor.LIGHT_PURPLE + "to play."));
                        itemStack.setItemMeta(leatherArmorMeta);
                        teamSelectionMenu.setItem(i, itemStack);
                    }
                    i++;
                }

                ItemStack autoJoinHelmet = ItemFactory.createItem(Material.CHAINMAIL_HELMET, ChatColor.WHITE + "Auto Join",
                        Arrays.asList(ChatColor.WHITE.toString() + totalMatchSize + ChatColor.GRAY.toString() + "/" + totalMatchMaxSize + " playing."));
                ItemMeta autoJoinHelmetMeta = autoJoinHelmet.getItemMeta();
                autoJoinHelmetMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                autoJoinHelmetMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                autoJoinHelmet.setItemMeta(autoJoinHelmetMeta);
                teamSelectionMenu.setItem(0, autoJoinHelmet);
            }
        }, 0L, 20L);
    }

    @Override
    public void load(Match match) {
        this.spectators = TGM.getTgm().getTeamManager().getSpectators();
    }

    private void applySpectatorKit(PlayerContext playerContext) {
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
        if (spectators.containsPlayer(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (event.getTeam() == spectators) {
            applySpectatorKit(event.getPlayerContext());
        }
    }

    @Override
    public void unload() {
        Bukkit.getScheduler().cancelTask(teamSelectionRunnable);
        teamSelectionMenu.disable();
    }
}
