package network.warzone.tgm.modules.respawn;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.player.event.TGMPlayerRespawnEvent;
import network.warzone.tgm.util.Parser;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RespawnModule extends MatchModule implements Listener {

    private final RespawnRule DEFAULT_RULE = new RespawnRule(null, 3000, false, true, true);

    private List<Player> spectators;
    private Map<UUID, Integer> spectatorTime;
    private List<Player> confirmed;
    private List<Player> frozen;
    private TeamManagerModule teamManagerModule;

    private List<RespawnRule> respawnRules;

    private BukkitTask task;

    public void load(Match match) {
        spectators = new ArrayList<>();
        teamManagerModule = match.getModule(TeamManagerModule.class);
        spectatorTime = new HashMap<>();
        respawnRules = new ArrayList<>();
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
                    int delay = 3000;
                    if (rule.has("delay")) delay = rule.get("delay").getAsInt();
                    boolean freeze = false;
                    if (rule.has("freeze")) freeze = rule.get("freeze").getAsBoolean();
                    boolean blindness = true;
                    if (rule.has("blindness")) blindness = rule.get("blindness").getAsBoolean();
                    boolean confirm = true;
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
        if (spectators.contains(event.getPlayer()) &&
                (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) &&
                getRule(team).isConfirm()) {
            confirmed.add(event.getPlayer());
        }
    }

    private void updateTitle() {
        List<Player> toRemove = new ArrayList<>();
        for (Player spectator : spectators) {
            int timeLeft = (spectatorTime.get(spectator.getUniqueId()));
            if (timeLeft <= 0) {
                spectator.sendTitle(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "RESPAWN",
                        ChatColor.GRAY + "Punch to respawn", 0, 2, 0);
                if (confirmed.contains(spectator)) toRemove.add(spectator);
            } else {
                spectator.sendTitle(
                        ChatColor.RED.toString() + ChatColor.BOLD.toString() + "YOU DIED",
                        ChatColor.GRAY + "Respawning in " + ChatColor.YELLOW + String.format("%.1f", timeLeft / 1000.0) + 's',
                        0, 2, 0);
                spectatorTime.replace(spectator.getUniqueId(), timeLeft - 50);
            }
        }
        toRemove.forEach(this::stopSpectating);
    }

    private void startSpectating(Player player, int respawnDelay, TGMPlayerDeathEvent event) {
        spectators.add(player);
        spectatorTime.put(player.getUniqueId(), respawnDelay);

        RespawnRule rule = getRule(teamManagerModule.getTeam(player));

        player.sendTitle(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "YOU DIED", "", 0, 3, 0);

        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            if (event.getKiller() != null) {
                player.teleport(event.getKiller().getLocation(), PlayerTeleportEvent.TeleportCause.SPECTATE);
            } else {
                player.teleport(teamManagerModule.getSpectators().getSpawnPoints().get(0).getLocation(), PlayerTeleportEvent.TeleportCause.SPECTATE);
            }
        }

        if (rule.isBlindness()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 3, true));
        }
        if (rule.isFreeze()) {
            frozen.add(player);
        }

        player.setAllowFlight(true);
        player.setFlying(true);
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            player.setVelocity(player.getVelocity().setY(0.95));
        } else {
            player.setVelocity(player.getVelocity().setY(0));
        }
        player.setGameMode(GameMode.SPECTATOR);
        if (!rule.isConfirm()) {
            Bukkit.getScheduler().runTaskLater(TGM.get(), () -> stopSpectating(event.getVictim()), respawnDelay * 20);
        }
    }

    private void stopSpectating(Player player) {
        spectators.remove(player);
        frozen.remove(player);
        confirmed.remove(player);
        spectatorTime.remove(player.getUniqueId());

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
        return DEFAULT_RULE;
    }

}