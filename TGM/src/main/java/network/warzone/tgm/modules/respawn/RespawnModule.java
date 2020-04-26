package network.warzone.tgm.modules.respawn;

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.*;
import network.warzone.tgm.modules.death.DeathInfo;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.player.event.TGMPlayerRespawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

import static network.warzone.tgm.util.ColorConverter.format;

@ModuleData(load = ModuleLoadTime.EARLIER)
public class RespawnModule extends MatchModule implements Listener {

    @Getter @Setter
    private RespawnRule defaultRule = new RespawnRule(null, 3000, false, true, true);

    private List<RespawnRule> respawnRules;
    private Map<Player, RespawnGoal> respawning;
    @Getter private List<RespawnService> respawnServices;


    private BukkitTask task;

    public void load(Match match) {
        this.respawnRules = new ArrayList<>();
        this.respawning = new HashMap<>();
        this.respawnServices = new ArrayList<>();
        TeamManagerModule teamManagerModule = match.getModule(TeamManagerModule.class);

        JsonObject mapInfo = match.getMapContainer().getMapInfo().getJsonObject();
        if (mapInfo.has("respawn")) {
            JsonObject respawnObject = mapInfo.getAsJsonObject("respawn");
            if (respawnObject.has("rules")) {
                JsonArray rules = respawnObject.get("rules").getAsJsonArray();
                for (JsonElement element : rules) {
                    JsonObject rule = element.getAsJsonObject();
                    List<MatchTeam> matchTeams = new ArrayList<>();
                    int delay = getDefaultRule().getDelay();
                    boolean freeze = getDefaultRule().isFreeze();
                    boolean blindness = getDefaultRule().isBlindness();
                    boolean confirm = getDefaultRule().isConfirm();
                    if (rule.has("teams")) matchTeams = teamManagerModule.getTeams(rule.get("teams").getAsJsonArray());
                    if (rule.has("delay")) delay = rule.get("delay").getAsInt();
                    if (rule.has("freeze")) freeze = rule.get("freeze").getAsBoolean();
                    if (rule.has("blindness")) blindness = rule.get("blindness").getAsBoolean();
                    if (rule.has("confirm")) confirm = rule.get("confirm").getAsBoolean();
                    this.respawnRules.add(new RespawnRule(matchTeams, delay, freeze, blindness, confirm));
                }
            }
        }
    }

    @EventHandler
    public void onDeath(TGMPlayerDeathEvent event) {
        if (event.getDeathInfo().playerTeam.isSpectator()) return;
        RespawnRule rule = getRule(event.getDeathInfo().playerTeam);
        setDead(event.getVictim(), rule, event.getDeathInfo());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        RespawnGoal goal = this.respawning.get(event.getPlayer());
        if (goal == null || !goal.getRule().isFreeze()) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        RespawnGoal goal = this.respawning.get(event.getPlayer());
        if (goal == null || !goal.getRule().isConfirm()) return;
        event.setCancelled(true);
        goal.setConfirmed(true);
    }

    @EventHandler
    public void onProjectile(PlayerReadyArrowEvent event) {
        if (isDead(event.getPlayer())) {
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
        if (!event.isForced() && isDead(event.getPlayerContext().getPlayer())) event.setCancelled(true);
        else remove(event.getPlayerContext().getPlayer());
    }

    @EventHandler
    public void onMatchResult(MatchResultEvent event) {
        new ArrayList<>(this.respawning.keySet()).forEach(p -> this.respawn(p, true));
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (isDead(event.getPlayer()) && event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            event.setCancelled(true);
        }
    }

    private void setDead(Player player, RespawnRule rule, DeathInfo deathInfo) {
        this.respawning.put(player, new RespawnGoal(player, rule, now() + rule.getDelay(), !rule.isConfirm()));
        boolean hasDelay = rule.getDelay() > 0;
        if (rule.isConfirm() || hasDelay || !shouldRespawn(player)) { // Don't apply effects if not needed
            if (rule.isBlindness()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 3, true));
            }
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setGameMode(GameMode.SPECTATOR);
            player.setVelocity(new Vector());
            if (deathInfo.killer != null) {
                player.teleport(deathInfo.killerLocation);
            }
        }
    }

    private void remove(Player player) {
        this.respawning.remove(player);
    }

    public void respawn(Player player, boolean reset) {
        if (!this.respawning.containsKey(player)) return;
        remove(player);
        if (reset) resetPlayer(player);
        Bukkit.getPluginManager().callEvent(new TGMPlayerRespawnEvent(player));
    }

    public void enable() {
        task = Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
            List<Player> toRespawn = new ArrayList<>();
            for (Map.Entry<Player, RespawnGoal> respawnEntry : this.respawning.entrySet()) {
                try {
                    Player player = respawnEntry.getKey();
                    RespawnGoal goal = respawnEntry.getValue();

                    boolean shouldRespawn = shouldRespawn(player);
                    sendTitle(player, goal, shouldRespawn);
                    if (getTimeLeft(goal) > 0 || !shouldRespawn || (goal.getRule().isConfirm() && !goal.isConfirmed())) {
                        continue;
                    }
                    toRespawn.add(player);
                } catch (Exception ignored) {
                }
            }
            for (Player player : toRespawn) {
                respawn(player, true);
            }
        }, 0L, 1L);
    }

    private void sendTitle(Player player, RespawnGoal goal, boolean shouldRespawn) {
        long timeLeft = getTimeLeft(goal);
        if (!shouldRespawn) {
            if (timeLeft >= -40 && timeLeft <= 0) player.sendTitle(format("&c&lYOU DIED"),
                    "",
                    0, 20, 0);
            return;
        }
        if (goal.getRule().isConfirm() && !goal.isConfirmed()) {
            if (timeLeft > 0) {
                player.sendTitle(format("&c&lYOU DIED"),
                        format("&7Punch to respawn in &e" + String.format("%.1f", timeLeft / 1000.0) + 's'),
                        0, 20, 0);
            } else {
                player.sendTitle(format("&c&lRESPAWN"), format("&7Punch to respawn"), 0, 20, 0);
            }
            return;
        }
        player.sendTitle(format("&c&lYOU DIED"),
                format("&7Respawning in &e" + String.format("%.1f", timeLeft / 1000.0) + 's'),
                0, 20, 0);

    }

    public void disable() {
        task.cancel();
    }

    private RespawnRule getRule(MatchTeam team) {
        for (RespawnRule rule : respawnRules) {
            if (rule.getTeams() == null || rule.getTeams().isEmpty() || rule.getTeams().contains(team)) {
                return rule;
            }
        }
        return defaultRule;
    }

    public boolean isDead(Player player) {
        return this.respawning.containsKey(player);
    }

    private boolean shouldRespawn(Player player) {
        if (this.respawnServices != null && !this.respawnServices.isEmpty()) {
            for (RespawnService service : this.respawnServices) {
                if (!service.shouldRespawn(player)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void resetPlayer(Player player) {
        player.sendTitle("", "", 0, 0, 0);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 999, 1);
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    public void addRespawnService(RespawnService service) {
        this.respawnServices.add(service);
    }

    private long now() {
        return new Date().getTime();
    }

    private long getTimeLeft(RespawnGoal goal) {
        return goal.getUntil() - now();
    }

    @AllArgsConstructor @Getter
    private static class RespawnGoal {
        private Player player;
        private RespawnRule rule;
        private long until;
        @Setter private boolean confirmed;
    }

}
