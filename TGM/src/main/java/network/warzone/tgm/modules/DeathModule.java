package network.warzone.tgm.modules;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.util.itemstack.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

@NoArgsConstructor
public class DeathModule extends MatchModule implements Listener {

    private HashMap<UUID, DeathModule> players = new HashMap<>();
    private TeamManagerModule teamManagerModule;

    @Getter @Setter private Player player, killer;
    @Getter @Setter private ItemStack item;
    @Getter @Setter private EntityDamageEvent.DamageCause cause;
    @Getter @Setter private String playerName, killerName;
    @Getter @Setter private MatchTeam playerTeam, killerTeam;
    @Getter @Setter private Location playerLocation, killerLocation;
    @Getter @Setter private long stampKill;

    public DeathModule(Player player) {
        this.player = player;
    }

    public void load(Match match) {
        teamManagerModule = match.getModule(TeamManagerModule.class);
    }

    public void unload() {
        players.clear();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        players.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            DeathModule module = getPlayer(((Player) event.getEntity()).getPlayer());

            module.setPlayer(module.getPlayer());
            module.setPlayerName(module.getPlayer().getName());
            module.setPlayerTeam(teamManagerModule.getTeam(module.getPlayer()));
            module.setPlayerLocation(module.getPlayer().getLocation());
            module.setCause(event.getCause());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            DeathModule module = getPlayer((Player) event.getEntity());

            Player damager = null;

            if (event.getDamager() instanceof Player) damager = (Player) event.getDamager();
            if (event.getDamager() instanceof Arrow) damager = (Player) ((Arrow) event.getDamager()).getShooter();

            if (damager != null && teamManagerModule.getTeam(damager).isSpectator()) return;

            module.setKiller(damager);
            module.setItem(event.getDamager() instanceof Arrow ? ItemFactory.createItem(Material.BOW) : damager == null ? ItemFactory.createItem(Material.AIR) : damager.getInventory().getItemInMainHand());

            module.setKillerName(damager == null ? null : damager.getName());
            module.setStampKill(damager == null ? -1 : System.currentTimeMillis());
            module.setKillerTeam(damager == null ? null : teamManagerModule.getTeam(damager));
            module.setKillerLocation(damager == null ? null : damager.getLocation());
        }
    }

    public DeathModule getPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!players.containsKey(playerUUID)) {
            players.put(playerUUID, new DeathModule(player));
        }

        return players.get(playerUUID);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        DeathModule module = getPlayer(event.getEntity());
        if(module.getStampKill() > 0 && System.currentTimeMillis() - module.getStampKill() >= 1000 * 30) module.setKiller(null);
        Bukkit.getPluginManager().callEvent(new TGMPlayerDeathEvent(module.getPlayer(), module.getKiller(), module.getCause(), module.getItem()));

        Bukkit.getScheduler().runTaskLater(TGM.get(), () -> event.getEntity().spigot().respawn(), 1L);
    }

}
