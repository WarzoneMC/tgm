package network.warzone.tgm.modules.respawn;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.*;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.player.event.TGMPlayerRespawnEvent;
import network.warzone.tgm.util.Parser;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@ModuleData(load = ModuleLoadTime.EARLIER)
public class RespawnModule extends MatchModule implements Listener {

    @Getter @Setter
    private RespawnRule defaultRule = new RespawnRule(null, 3000, false, true, true);

    private List<Player> spectators;
    private Map<UUID, Integer> spectatorTime;
    private List<Player> confirmed;
    private List<Player> frozen;
    private TeamManagerModule teamManagerModule;

    private List<RespawnRule> respawnRules;

    @Getter private List<RespawnService> respawnServices;

    private BukkitTask task;

    public void load(Match match) {
        spectators = new ArrayList<>();
        teamManagerModule = match.getModule(TeamManagerModule.class);
        spectatorTime = new HashMap<>();
        respawnRules = new ArrayList<>();
        respawnServices = new ArrayList<>();
        confirmed = new ArrayList<>();
        frozen = new ArrayList<>();

        JsonObject settings = match.getMapContainer().getMapInfo().getJsonObject();
        if (settings.has("respawn")) {
            JsonObject respawnSettings = settings.getAsJsonObject("respawn");
            if (respawnSettings.has("rules")) {
                JsonArray rules = respawnSettings.get("rules").getAsJsonArray();
                for (JsonElement element : rules) {
                    JsonObject rule = element.getAsJsonObject();
                    List<MatchTeam> matchTeams = Parser.getTeamsFromElement(teamManagerModule, rule.get("teams"));
                    int delay = defaultRule.getDelay();
                    boolean freeze = defaultRule.isFreeze();
                    boolean blindness = defaultRule.isBlindness();
                    boolean confirm = defaultRule.isConfirm();
                    if (rule.has("delay")) delay = rule.get("delay").getAsInt();
                    if (rule.has("freeze")) freeze = rule.get("freeze").getAsBoolean();
                    if (rule.has("blindness")) blindness = rule.get("blindness").getAsBoolean();
                    if (rule.has("confirm")) confirm = rule.get("confirm").getAsBoolean();
                    respawnRules.add(new RespawnRule(matchTeams, delay, freeze, blindness, confirm));
                }
            }
        }
    }

    public void enable() {
        task = Bukkit.getScheduler().runTaskTimer(TGM.get(), this::updateTitle, 0, 1L);
    }

    public void disable() {
        task.cancel();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (frozen.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(TGMPlayerDeathEvent event) {
        Player victim = event.getVictim();
        MatchTeam matchTeam = teamManagerModule.getTeam(victim);

        if (matchTeam.isSpectator()) return;

        startSpectating(event.getVictim(), getRule(matchTeam).getDelay(), event);
    }

    @EventHandler
    public void onPlayerPunch(PlayerInteractEvent event) {
        MatchTeam team = teamManagerModule.getTeam(event.getPlayer());
        if (shouldRespawn(event.getPlayer()) && (spectators.contains(event.getPlayer()) && (event.getAction().equals(Action.LEFT_CLICK_AIR) ||
            event.getAction().equals(Action.LEFT_CLICK_BLOCK)) && getRule(team).isConfirm())) {
            confirmed.add(event.getPlayer());
        }
    }

    @EventHandler
    public void onProjectile(PlayerReadyArrowEvent event) {
        if (isSpectating(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE && isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeamSwitch(TeamChangeEvent event) {
        if (event.isCancelled()) return;
        if (!event.isForced() && spectators.contains(event.getPlayerContext().getPlayer())) event.setCancelled(true);
        else remove(event.getPlayerContext().getPlayer());
    }

    @EventHandler
    public void onMatchResult(MatchResultEvent event) {
        new ArrayList<>(this.spectators).forEach(this::stopSpectating);
    }

    private void remove(Player player) {
        spectators.remove(player);
        confirmed.remove(player);
        spectatorTime.remove(player.getUniqueId());
        frozen.remove(player);
    }

    private void updateTitle() {
        List<Player> toRemove = new ArrayList<>();
        for (Player spectator : spectators) {
            if (!shouldRespawn(spectator)) continue;
            int timeLeft = (spectatorTime.get(spectator.getUniqueId()));
            if (timeLeft <= 0) {
                spectator.sendTitle(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "RESPAWN",
                        ChatColor.GRAY + "Punch to respawn", 0, 20, 0);
                if (confirmed.contains(spectator)) toRemove.add(spectator);
            } else {
                if (confirmed.contains(spectator)) spectator.sendTitle(
                        ChatColor.RED.toString() + ChatColor.BOLD.toString() + "YOU DIED",
                        ChatColor.GRAY + "Respawning in " + ChatColor.YELLOW + String.format("%.1f", timeLeft / 1000.0) + 's',
                        0, 20, 0);
                else spectator.sendTitle(
                        ChatColor.RED.toString() + ChatColor.BOLD.toString() + "YOU DIED",
                        ChatColor.GRAY + "Punch to respawn in " + ChatColor.YELLOW + String.format("%.1f", timeLeft / 1000.0) + 's',
                        0, 20, 0);
                spectatorTime.replace(spectator.getUniqueId(), timeLeft - 50);
            }
        }
        toRemove.forEach(this::stopSpectating);
    }

    private void startSpectating(Player player, int respawnDelay, TGMPlayerDeathEvent event) {
        spectators.add(player);
        spectatorTime.put(player.getUniqueId(), respawnDelay);
        RespawnRule rule = getRule(teamManagerModule.getTeam(player));
        if (rule.getDelay() > 0 || !shouldRespawn(player)) {
            player.sendTitle(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "YOU DIED", "", 0, 60, 0);

            if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                if (event.getKiller() != null) {
                    player.teleport(event.getKiller().getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                } else {
                    player.teleport(teamManagerModule.getSpectators().getSpawnPoints().get(0).getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }

            if (rule.isBlindness())
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 3, true));
            if (rule.isFreeze())
                frozen.add(player);

            player.setAllowFlight(true);
            player.setFlying(true);
            if (!event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                player.setVelocity(player.getVelocity().setY(0.95));
            } else {
                player.setVelocity(player.getVelocity().setY(0));
            }
            player.setGameMode(GameMode.SPECTATOR);
        }
        if (!rule.isConfirm())
            confirmed.add(player);
    }

    private void stopSpectating(Player player) {
        spectators.remove(player);
        frozen.remove(player);
        confirmed.remove(player);
        spectatorTime.remove(player.getUniqueId());
        player.sendTitle("", "", 0, 0, 0);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 999, 1);
        player.setAllowFlight(false);
        player.setFlying(false);

        Bukkit.getPluginManager().callEvent(new TGMPlayerRespawnEvent(player));
    }

    private RespawnRule getRule(MatchTeam team) {
        for (RespawnRule rule : respawnRules) {
            if (rule.getTeams().contains(team)) {
                return rule;
            }
        }
        return defaultRule;
    }

    public boolean isSpectating(Player player) {
        return this.spectators.contains(player);
    }

    public boolean shouldRespawn(Player player) {
        boolean shouldRespawn = true;
        if (this.respawnServices != null && !this.respawnServices.isEmpty()) {
            for (RespawnService service : this.respawnServices)
                shouldRespawn = shouldRespawn && service.shouldRespawn(player);
        }
        return shouldRespawn;
    }

    public void addRespawnService(RespawnService service) {
        this.respawnServices.add(service);
    }

}
