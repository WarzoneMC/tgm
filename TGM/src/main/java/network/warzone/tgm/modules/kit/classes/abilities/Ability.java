package network.warzone.tgm.modules.kit.classes.abilities;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.damage.DamageInfo;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.util.TimeUnitPair;
import network.warzone.tgm.util.itemstack.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by yikes on 09/27/19
 */
@Getter
public abstract class Ability implements Listener {
    protected Set<UUID> registeredPlayers = new HashSet<>();
    protected ItemStack abilityItem;

    // store name, display name, and material
    private String abilityName;
    private Material abilityMaterial;
    private String abilityDisplayName;

    private int runnableID;
    private HashMap<UUID, Integer> cooldowns = new HashMap<>();
    private int cooldown;

    protected TeamManagerModule teamManagerModule;

    public Ability(String abilityName, int cooldown, Material abilityMaterial, String abilityDisplayName, String... abilityLore) {
        this.abilityName = abilityName;
        this.cooldown = cooldown;
        this.abilityItem = ItemFactory.createItem(abilityMaterial, abilityDisplayName, Arrays.asList(abilityLore));
        this.abilityMaterial = abilityMaterial;
        this.abilityDisplayName = abilityDisplayName;

        this.teamManagerModule = TGM.get().getModule(TeamManagerModule.class);

        setupCooldownRunnable();

        TGM.registerEvents(this);
    }

    private void setupCooldownRunnable() {
        this.runnableID = Bukkit.getScheduler().scheduleSyncRepeatingTask(TGM.get(), () -> {
            if (cooldowns.isEmpty()) return;

            ArrayList<UUID> remove = new ArrayList<UUID>();
            for (UUID uuid : cooldowns.keySet()) {
                cooldowns.put(uuid, cooldowns.get(uuid) - 1);

                if (cooldowns.get(uuid) <= 0) {
                    remove.add(uuid);
                }
            }

            for (UUID uuid : remove) {
                cooldowns.remove(uuid);
                Player player = Bukkit.getServer().getPlayer(uuid);
                if(player != null) offCooldownMessage(player);
            }

        }, 0L, 0L);
    }

    // raw event handlers

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if(!passesGeneralAbilityConditions(event.getItemDrop().getItemStack(), event.getPlayer(), false)) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "You cannot drop ability items!");
    }


    @EventHandler
    public void onAbilityInteractEvent(PlayerInteractEvent event) {
        Player mainPlayer = event.getPlayer();
        if(!passesGeneralAbilityConditions(event.getPlayer().getInventory().getItemInMainHand(), mainPlayer, true)) return;
        /* Air Clicks */
        if (event.getAction() == Action.LEFT_CLICK_AIR) {
            this.onClick(mainPlayer);
            this.onLeftClick(mainPlayer);
            this.onLeftClickAir(mainPlayer);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            this.onClick(mainPlayer);
            this.onRightClick(mainPlayer);
            this.onRightClickAir(mainPlayer);
        }
        /* Block Clicks */
        else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            this.onClick(mainPlayer);
            this.onLeftClick(mainPlayer);
            this.onLeftClickBlock(mainPlayer, event.getClickedBlock());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            this.onClick(mainPlayer);
            this.onRightClick(mainPlayer);
            this.onRightClickBlock(mainPlayer, event.getClickedBlock());
        }
    }

    @EventHandler
    public void onAbilityInteractWithEntity(PlayerInteractAtEntityEvent event) {
        Player mainPlayer = event.getPlayer();
        if(!passesGeneralAbilityConditions(event.getPlayer().getInventory().getItemInMainHand(), mainPlayer, true)) return;
        this.onClick(mainPlayer);
        this.onRightClick(mainPlayer);
        if (!(event.getRightClicked() instanceof Player)) return;
        Player targetPlayer = (Player) event.getRightClicked();
        if (teamManagerModule.getTeam(targetPlayer).isSpectator()) return;
        this.onRightClickPlayer(mainPlayer, targetPlayer);
    }


    @EventHandler
    public void onAbilityDamage(EntityDamageEvent event) {
        DamageInfo damageInfo = new DamageInfo(event);
        if(damageInfo.getDamagerPlayer() == null) return;
        ItemStack item = damageInfo.getDamagerPlayer().getInventory().getItemInMainHand();
        if(!passesGeneralAbilityConditions(item, damageInfo.getDamagerPlayer(), true)) return;
        if (damageInfo.getHurtPlayer() != null) {
            if (teamManagerModule.getTeam(damageInfo.getHurtPlayer()).isSpectator()) return;
            this.onHitPlayer(damageInfo.getDamagerPlayer(), damageInfo.getHurtPlayer());
        } else this.onHitEntity(damageInfo.getDamagerPlayer(), damageInfo.getHurtEntity());
    }

    @EventHandler
    public void onAbilityBlockPlace(BlockPlaceEvent event) {
        if(!passesGeneralAbilityConditions(event.getItemInHand(), event.getPlayer(), true)) return;
        this.onClick(event.getPlayer());
        this.onPlaceBlock(event.getPlayer(), event.getBlockPlaced().getLocation(), event);
    }



    // called when ability is applied to player
    public void apply(Player player) {}

    /*
     * Event interface abilities can hook into
     * Guarantees:
     * - Player is user of kit
     * - Player is holding ability item
     * - Player is not in cooldown
     */

    protected void onClick(Player player) {}
    protected void onRightClick(Player player) {}
    protected void onRightClickAir(Player player) {}
    protected void onLeftClick(Player player) {}
    protected void onLeftClickAir(Player player) {}
    protected void onLeftClickBlock(Player player, Block block) {}
    protected void onRightClickBlock(Player player, Block block) {}

    protected void onHitPlayer(Player player, Player hurt) {}
    protected void onHitEntity(Player player, LivingEntity hurt) {}

    protected void onPlaceBlock(Player player, Location location, BlockPlaceEvent event) {}

    // Guarantees clicked player is NOT spectator
    protected void onRightClickPlayer(Player player, Player target) {}



    // implementation details

    protected void putOnCooldown(Player player) {
        this.cooldowns.put(player.getUniqueId(), this.cooldown);
    }

    private boolean passesGeneralAbilityConditions(ItemStack item, Player player, boolean checkCooldown) {
        if (!isAbilityItem(item)) return false;
        if (!registeredPlayers.contains(player.getUniqueId())) return false;
        if(!isOffCooldown(player) && checkCooldown) {
            informPlayerOfCooldown(player);
            return false;
        }
        return true;
    }

    private void informPlayerOfCooldown(Player player) {
        player.sendMessage(ChatColor.BLUE + "Ability> " + ChatColor.YELLOW + this.abilityName + ChatColor.GRAY + " is usable in " + ChatColor.GREEN + TimeUnitPair.formatToSeconds(this.getCooldown(player)));
    }

    private int getCooldown(Player player) {
        if (!this.isOffCooldown(player)) return this.cooldowns.get(player.getUniqueId());
        return 0;
    }

    private boolean isOffCooldown(Player player) {
        return !this.cooldowns.containsKey(player.getUniqueId());
    }

    private boolean isAbilityItem(ItemStack item) {
        if (item == null) return false;
        return item.getType() == this.abilityMaterial && item.getItemMeta().getDisplayName().equals(this.abilityDisplayName);
    }

    private void offCooldownMessage(Player player) {
        player.sendMessage(ChatColor.YELLOW + this.abilityName + ChatColor.GRAY + " is now usable");
    }

    public void terminate() {
        registeredPlayers = null;
        Bukkit.getScheduler().cancelTask(this.runnableID);
        cooldowns = null;
        HandlerList.unregisterAll(this);
    }
}
