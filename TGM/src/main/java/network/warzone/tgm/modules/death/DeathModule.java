package network.warzone.tgm.modules.death;

import lombok.NoArgsConstructor;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.player.event.TGMPlayerRespawnEvent;
import network.warzone.tgm.util.itemstack.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
public class DeathModule extends MatchModule implements Listener {

    private WeakReference<Match> match;

    private HashMap<UUID, DeathInfo> players = new HashMap<>();
    private HashMap<UUID, Boolean> dead = new HashMap<>();
    private TeamManagerModule teamManagerModule;

    public void load(Match match) {
        this.match = new WeakReference<Match>(match);
        teamManagerModule = match.getModule(TeamManagerModule.class);
    }

    public void unload() {
        players = null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        players.remove(event.getPlayer().getUniqueId());
        notDead(event.getPlayer());
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        notDead(event.getPlayerContext().getPlayer());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (this.match.get().getMatchStatus() == MatchStatus.POST) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Player) {
            if (this.dead.containsKey(event.getEntity().getUniqueId())) {
                event.setCancelled(true);
                return;
            }
            Player p = (Player) event.getEntity();
            DeathInfo deathInfo = getPlayer((Player) event.getEntity());

            deathInfo.playerName = deathInfo.player.getName();
            deathInfo.playerTeam = teamManagerModule.getTeam(deathInfo.player);
            deathInfo.playerLocation = deathInfo.player.getLocation();
            deathInfo.cause = event.getCause();

            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent byEntityEvent = (EntityDamageByEntityEvent) event;
                Player damager = null;

                if (byEntityEvent.getDamager() instanceof Player) damager = (Player) byEntityEvent.getDamager();
                else if (byEntityEvent.getDamager() instanceof Projectile) {
                    Projectile projectile = (Projectile) byEntityEvent.getDamager();
                    if (projectile.getShooter() instanceof Player) damager = (Player) projectile.getShooter();
                }

                if (damager != null && teamManagerModule.getTeam(damager).isSpectator()) return;

                deathInfo.killer = damager;
                deathInfo.item = determineItemFromDamager(byEntityEvent.getDamager(), damager);

                deathInfo.killerName = damager == null ? null : damager.getName();
                deathInfo.stampKill = damager == null ? -1 : System.currentTimeMillis();
                deathInfo.killerTeam = damager == null ? null : teamManagerModule.getTeam(damager);
                deathInfo.killerLocation = damager == null ? null : damager.getLocation();
            }

            if (p.getHealth() - event.getFinalDamage() <= 0 || event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                event.setDamage(0);
                onDeath(p, deathInfo);
            }
        }
    }

    private static ItemStack determineItemFromDamager(Entity e, Player p) {
        if (e instanceof Arrow) {
            return ItemFactory.createItem(Material.BOW);
        } else if (e instanceof Trident) {
            return ItemFactory.createItem(Material.TRIDENT);
        } else if (p == null) {
            return ItemFactory.createItem(Material.AIR);
        } else return p.getInventory().getItemInMainHand();
    }

    public DeathInfo getPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!players.containsKey(playerUUID)) {
            players.put(playerUUID, new DeathInfo(player));
        }

        return players.get(playerUUID);
    }

    private void onDeath(Player player, DeathInfo deathInfo) {
        if (deathInfo.stampKill > 0 && System.currentTimeMillis() - deathInfo.stampKill >= 1000 * 30) deathInfo.killer = null;
        setDead(player);
        Bukkit.getPluginManager().callEvent(new TGMPlayerDeathEvent(
                deathInfo.player,
                deathInfo.playerLocation,
                deathInfo.killer,
                deathInfo.cause,
                deathInfo.item,
                Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).collect(Collectors.toList()),
                deathInfo
        ));
        deathInfo.killer = null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerDeath(TGMPlayerDeathEvent event) {
        if (match.get().getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) return;
        for (ItemStack stack : event.getDrops()) {
            if (stack != null) {
                event.getVictim().getWorld().dropItemNaturally(event.getDeathLocation(), stack);
            }
        }
    }

    @EventHandler
    private void onRespawn(TGMPlayerRespawnEvent event) {
        notDead(event.getPlayer());
    }

    private void setDead(Player player) {
        this.dead.put(player.getUniqueId(), true);
    }

    private void notDead(Player player) {
        this.dead.remove(player.getUniqueId());
    }

    public boolean isDead(Player player) {
        return this.dead.containsKey(player.getUniqueId());
    }

}
