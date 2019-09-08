package network.warzone.tgm.modules;

import lombok.NoArgsConstructor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.util.itemstack.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

@NoArgsConstructor
public class DeathModule extends MatchModule implements Listener {

    private HashMap<UUID, DeathInfo> players = new HashMap<>();
    private TeamManagerModule teamManagerModule;

    public void load(Match match) {
        teamManagerModule = match.getModule(TeamManagerModule.class);
    }

    public void unload() {
        players = null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        players.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            DeathInfo deathInfo = getPlayer((Player) event.getEntity());

            deathInfo.playerName = deathInfo.player.getName();
            deathInfo.playerTeam = teamManagerModule.getTeam(deathInfo.player);
            deathInfo.playerLocation = deathInfo.player.getLocation();
            deathInfo.cause = event.getCause();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            DeathInfo deathInfo = getPlayer((Player) event.getEntity());

            Player damager = null;

            if (event.getDamager() instanceof Player) damager = (Player) event.getDamager();
            if (event.getDamager() instanceof Arrow) damager = (Player) ((Arrow) event.getDamager()).getShooter();

            if (damager != null && teamManagerModule.getTeam(damager).isSpectator()) return;

            deathInfo.killer = damager;
            deathInfo.item = event.getDamager() instanceof Arrow ? ItemFactory.createItem(Material.BOW) : damager == null ? ItemFactory.createItem(Material.AIR) : damager.getInventory().getItemInMainHand();

            deathInfo.killerName = damager == null ? null : damager.getName();
            deathInfo.stampKill = damager == null ? -1 : System.currentTimeMillis();
            deathInfo.killerTeam = damager == null ? null : teamManagerModule.getTeam(damager);
            deathInfo.killerLocation = damager == null ? null : damager.getLocation();
        }
    }

    public DeathInfo getPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!players.containsKey(playerUUID)) {
            players.put(playerUUID, new DeathInfo(player));
        }

        return players.get(playerUUID);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        DeathInfo deathInfo = getPlayer(event.getEntity());
        if(deathInfo.stampKill > 0 && System.currentTimeMillis() - deathInfo.stampKill >= 1000 * 30) deathInfo.killer = null;
        Bukkit.getPluginManager().callEvent(new TGMPlayerDeathEvent(deathInfo.player, deathInfo.killer, deathInfo.cause, deathInfo.item));

        Bukkit.getScheduler().runTaskLater(TGM.get(), () -> event.getEntity().spigot().respawn(), 1L);
    }

}
